package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GenericFragment extends Fragment
{

    private Bundle        mSavedInstanceState = null;
    OnViewCreatedListener mListener           = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        mSavedInstanceState = savedInstanceState;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        if (mListener != null)
            mListener.onViewCreated(getActivity(), view, savedInstanceState);
    }

    void execSuperOnViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
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
                listener.onViewCreated(getActivity(), view, mSavedInstanceState);
        }
    }

}
