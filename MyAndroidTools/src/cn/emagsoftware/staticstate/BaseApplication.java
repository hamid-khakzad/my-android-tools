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
        if (!isServiceReceiverProviderNoOrInOtherProcess())
            throw new UnsupportedOperationException("should not have service,receiver and provider or set them in other process when you use BaseApplication.");
        ActivityManager aMgr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> apps = aMgr.getRunningAppProcesses();
        if (apps != null && apps.size() > 0)
        {
            int expectId = apps.get(0).pid;
            int myPid = Process.myPid();
            if (expectId == myPid)
            {
                // Pid相同时，表示进程创建是由Activity触发的，此时第一个Task肯定存在且是当前Task
                onInitStaticState(aMgr.getRunningTasks(1).get(0).topActivity.getClassName());
                return;
            }
        }
        // Pid不相同时，表示进程创建是由Service,Receiver或Provider触发的，此时，如果这三者与Activity处于不同的进程，将只需初始化入口的静态状态；
        // 如果这三者与Activity处于相同的进程，将由于很难精确定位到topActivityName而无法初始化额外的静态状态，该情况一开始便通过抛出异常的方式以告知用户
        onInitStaticState(null);
    }

    protected abstract boolean isServiceReceiverProviderNoOrInOtherProcess();

    /**
     * <p>初始化静态状态的回调方法
     * 
     * @param topActivityName 当前Task最顶部的Activity。如果是由Service,Receiver或Provider触发的进程创建，该参数为null，由于已经约定与Activity处于不同的进程，此时只需初始化入口的静态状态
     */
    protected abstract void onInitStaticState(String topActivityName);

}
