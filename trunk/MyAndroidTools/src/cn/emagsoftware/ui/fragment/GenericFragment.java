package cn.emagsoftware.ui.fragment;

import android.support.v4.app.Fragment;

/**
 * Created by Wendell on 13-8-26.
 */
public class GenericFragment extends Fragment {

    private boolean isViewDetached = false;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewDetached = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isViewDetached = false;
    }

    public boolean isViewDetached()
    {
        return isViewDetached;
    }

}
