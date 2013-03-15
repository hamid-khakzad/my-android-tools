package cn.emagsoftware.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * @deprecated 对ConfigurationChanged尤其是涉及到界面重构的处理现在首推交给系统结束并重启Activity，这种方式与Activity被回收后的恢复方式是一致的，从而降低开发和维护代码的成本
 * @author Wendell
 * 
 */
public abstract class ConfigChangeHandlingActivity extends Activity
{

    protected int  curContentViewResID = View.NO_ID;
    protected View curContentView      = null;

    @Override
    public void setContentView(int layoutResID)
    {
        // TODO Auto-generated method stub
        View newView = getLayoutInflater().inflate(layoutResID, null);
        super.setContentView(newView);
        curContentViewResID = layoutResID;
        View prevContentView = curContentView;
        curContentView = newView;
        onSetContentView(prevContentView, getResources().getConfiguration().orientation);
    }

    @Override
    public void setContentView(View view)
    {
        // TODO Auto-generated method stub
        super.setContentView(view);
        curContentViewResID = View.NO_ID;
        View prevContentView = curContentView;
        curContentView = view;
        onSetContentView(prevContentView, getResources().getConfiguration().orientation);
    }

    @Override
    public void setContentView(View view, LayoutParams params)
    {
        // TODO Auto-generated method stub
        super.setContentView(view, params);
        curContentViewResID = View.NO_ID;
        View prevContentView = curContentView;
        curContentView = view;
        onSetContentView(prevContentView, getResources().getConfiguration().orientation);
    }

    public boolean resetContentView()
    {
        if (curContentViewResID != View.NO_ID)
        {
            View newContentView = getLayoutInflater().inflate(curContentViewResID, null);
            super.setContentView(newContentView);
            View prevContentView = curContentView;
            curContentView = newContentView;
            onSetContentView(prevContentView, getResources().getConfiguration().orientation);
            return true;
        }
        return false;
    }

    @Override
    public final void onConfigurationChanged(Configuration newConfig)
    {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (configurationChangedCallback(newConfig))
            resetContentView();
    }

    protected abstract boolean configurationChangedCallback(Configuration newConfig);

    protected abstract void onSetContentView(View prevContentView, int orientation);

}
