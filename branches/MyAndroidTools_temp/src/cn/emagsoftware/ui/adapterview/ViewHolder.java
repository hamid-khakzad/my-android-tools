package cn.emagsoftware.ui.adapterview;

import android.view.View;

public class ViewHolder
{

    protected View[] mParams = null;
    protected Object mTag    = null;

    public ViewHolder()
    {
    }

    public ViewHolder(View... params)
    {
        mParams = params;
    }

    public void setParams(View... params)
    {
        mParams = params;
    }

    public View[] getParams()
    {
        return mParams;
    }

    public void setTag(Object tag)
    {
        mTag = tag;
    }

    public Object getTag()
    {
        return mTag;
    }

}
