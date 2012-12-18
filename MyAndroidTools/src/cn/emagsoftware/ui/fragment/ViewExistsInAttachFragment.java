package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewExistsInAttachFragment extends Fragment
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
        super.onViewCreated(view, savedInstanceState);
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
    public void onDetach()
    {
        // TODO Auto-generated method stub
        super.onDetach();
        onDestroyViewImpl(true);
        mViewPoint = null;
    }

    @Override
    public View getView()
    {
        // TODO Auto-generated method stub
        super.getView();
        return mViewPoint;
    }

}
