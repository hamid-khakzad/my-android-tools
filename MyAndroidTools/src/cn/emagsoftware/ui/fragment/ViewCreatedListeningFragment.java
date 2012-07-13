package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewCreatedListeningFragment extends Fragment
{

    private OnViewCreatedListener mListener           = null;
    private Bundle                mSavedInstanceState = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        mSavedInstanceState = savedInstanceState;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * <p>需要注意的是，如果View已经存在，那么在调用该方法时onViewCreated方法就会立即被回调
     * 
     * @param listener
     */
    public void setOnViewCreatedListener(OnViewCreatedListener listener)
    {
        mListener = listener;
        if (listener != null)
        {
            View view = getView();
            if (view != null)
                listener.onViewCreated(view, mSavedInstanceState);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        if (mListener != null)
            mListener.onViewCreated(view, savedInstanceState);
    }

}
