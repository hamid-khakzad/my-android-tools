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
        if (!isServiceReceiverProviderNoOrInOtherProcess()) // 如果在同一进程，则当前进程的创建不一定是由Activity触发的，此时任务列表中第一个Task不一定是当前Task，这种情况下即使遍历任务列表也很难获得当前Task，故暂不支持
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
                // 如果当前进程的创建由Activity触发，则任务列表中第一个Task肯定存在且是当前Task
                onInitStaticState(aMgr.getRunningTasks(1).get(0).topActivity.getClassName());
                return;
            }
        }
        onInitStaticState(null);
    }

    protected abstract boolean isServiceReceiverProviderNoOrInOtherProcess();

    /**
     * <p>获取Activity所在进程的名称，如果没有Activity进程，可以返回null或一个长度为0的数组
     * 
     * @return
     */
    protected abstract String[] getActivityProcessNames();

    /**
     * <p>初始化静态状态的方法
     * 
     * @param topActivityName 当前Task最顶部的Activity。如果进程创建是由Service,Receiver或Provider触发的，该参数为null
     */
    protected abstract void onInitStaticState(String topActivityName);

}
