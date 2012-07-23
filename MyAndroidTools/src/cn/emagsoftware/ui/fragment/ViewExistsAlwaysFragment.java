package cn.emagsoftware.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewExistsAlwaysFragment extends GenericFragment
{

    private View mViewPoint = null;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreateView(inflater, container, savedInstanceState);
        if (mViewPoint == null)
            return onCreateViewImpl(inflater, container, savedInstanceState);
        else
            return mViewPoint;
    }

    public View onCreateViewImpl(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return null;
    }

    @Override
    public final void onViewCreated(View view, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        boolean isNone = mViewPoint == null;
        mViewPoint = view;
        super.execSuperOnViewCreated(view, savedInstanceState);
        if (isNone && mListener != null)
        {
            mListener.onCreateViewCallback(getActivity(), view, savedInstanceState);
        }
        onViewCreatedImpl(view, savedInstanceState, isNone);
    }

    public void onViewCreatedImpl(View view, Bundle savedInstanceState, boolean isNewView)
    {
    }

    @Override
    public final void onDestroyView()
    {
        // TODO Auto-generated method stub
        super.onDestroyView();
        onDestroyViewImpl(false);
    }

    public void onDestroyViewImpl(boolean isViewDestroyed)
    {
    }

    @Override
    public View getView()
    {
        // TODO Auto-generated method stub
        super.getView();
        return mViewPoint;
    }

    @Override
    public void setOnCreateViewCallbackListener(OnCreateViewCallbackListener listener)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("current method is not supported for this class,use setOnCreateViewCallbackListener(Activity, OnCreateViewCallbackListener) instead");
    }

    public void setOnCreateViewCallbackListener(Activity activity, OnCreateViewCallbackListener listener)
    {
        // TODO Auto-generated method stub
        if (activity == null)
            throw new NullPointerException();
        mListener = listener;
        if (listener != null)
        {
            View view = getView();
            if (view != null)
                listener.onCreateViewCallback(activity, view, mSavedInstanceState);
        }
    }

}
