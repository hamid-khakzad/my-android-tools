package cn.emagsoftware.staticstate;

import java.util.List;

import android.app.ActivityManager;
import android.app.Application;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.Process;

public abstract class BaseApplication extends Application
{

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        if (!isServiceReceiverProviderNoOrInOtherProcess()) // �����ͬһ���̣���ǰ���̵Ĵ�����һ������Activity�����ģ���ʱ�����б��е�һ��Task��һ���ǵ�ǰTask����������¼�ʹ���������б�Ҳ���ѻ�õ�ǰTask�����ݲ�֧��
            throw new UnsupportedOperationException("should not have service,receiver and provider or set them in other process when you use BaseApplication.");
        String[] activityProcessNames = getActivityProcessNames();
        if (activityProcessNames != null && activityProcessNames.length > 0)
        {
            String curProcessName = null;
            ActivityManager aMgr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> apps = aMgr.getRunningAppProcesses();
            int myPid = Process.myPid();
            for (RunningAppProcessInfo app : apps)
            {
                if (app.pid == myPid)
                {
                    curProcessName = app.processName;
                    break;
                }
            }
            boolean isActivityProcess = false;
            for (String processName : activityProcessNames)
            {
                if (curProcessName.equals(processName))
                {
                    isActivityProcess = true;
                    break;
                }
            }
            if (isActivityProcess)
            {
                // �����ǰ���̵Ĵ�����Activity�������������б��е�һ��Task�϶��������ǵ�ǰTask
                onInitStaticState(aMgr.getRunningTasks(1).get(0).topActivity.getClassName());
                return;
            }
        }
        onInitStaticState(null);
    }

    protected abstract boolean isServiceReceiverProviderNoOrInOtherProcess();

    /**
     * <p>��ȡActivity���ڽ��̵����ƣ����û��Activity���̣����Է���null��һ������Ϊ0������
     * 
     * @return
     */
    protected abstract String[] getActivityProcessNames();

    /**
     * <p>��ʼ����̬״̬�ķ���
     * 
     * @param topActivityName ��ǰTask�����Activity��������̴�������Service,Receiver��Provider�����ģ��ò���Ϊnull
     */
    protected abstract void onInitStaticState(String topActivityName);

}
