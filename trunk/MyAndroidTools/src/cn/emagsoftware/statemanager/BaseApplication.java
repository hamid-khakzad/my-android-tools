package cn.emagsoftware.statemanager;

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
        if (!isServiceReceiverProviderInOtherProcess())
            throw new UnsupportedOperationException("should set service,receiver and provider in other process when you use BaseApplication.");
        ActivityManager aMgr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> apps = aMgr.getRunningAppProcesses();
        if (apps != null && apps.size() > 0)
        {
            int expectId = apps.get(0).pid;
            int myPid = Process.myPid();
            if (expectId == myPid)
            {
                // Pid��ͬʱ����ʾ���̴�������Activity�����ģ���ʱ��һ��Task�϶��������ǵ�ǰTask
                onInitGlobalState(aMgr.getRunningTasks(1).get(0).topActivity.getClassName());
                return;
            }
        }
        // Pid����ͬʱ����ʾ���̴�������Service,Receiver��Provider�����ģ���ʱ�������������Activity���ڲ�ͬ�Ľ��̣���ֻ���ʼ����ڵ�ȫ��״̬��
        // �����������Activity������ͬ�Ľ��̣������ں��Ѿ�ȷ��λ��topActivityName���޷���ʼ�������ȫ��״̬�������һ��ʼ��ͨ���׳��쳣�ķ�ʽ�Ը�֪�û�
        onInitGlobalState(null);
    }

    protected abstract boolean isServiceReceiverProviderInOtherProcess();

    /**
     * <p>��ʼ��ȫ��״̬�Ļص�����
     * 
     * @param topActivityName ��ǰTask�����Activity���������Service,Receiver��Provider�����Ľ��̴������ò���Ϊnull�������Ѿ�Լ����Activity���ڲ�ͬ�Ľ��̣���ʱֻ���ʼ����ڵ�ȫ��״̬
     */
    protected abstract void onInitGlobalState(String topActivityName);

}
