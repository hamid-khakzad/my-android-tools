package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

public class ViewExistsInAttachFragment extends Fragment
{
    
    private View mViewPoint = null;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        mViewPoint = view;
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
