package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.view.View;

public class ViewExistsInAttachFragment extends ViewCreatedListeningFragment
{

    private View mViewPoint = null;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        boolean isViewChanged = mViewPoint != view;
        mViewPoint = view;
        super.execSuperOnViewCreated(view, savedInstanceState);
        if (isViewChanged && mListener != null)
            mListener.onViewCreated(view, savedInstanceState);
    }

    @Override
    public final void onDestroyView()
    {
        // TODO Auto-generated method stub
        super.onDestroyView();
    }

    public void onDestroyViewImpl()
    {
    }

    @Override
    public void onDetach()
    {
        // TODO Auto-generated method stub
        super.onDetach();
        onDestroyViewImpl();
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
