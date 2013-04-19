package cn.emagsoftware.statemanager;

import android.app.Application;

public abstract class BaseApplication extends Application
{

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        onInitGlobalState();
    }

    protected abstract void onInitGlobalState();

}
