package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Wendell on 13-8-26.
 */
public class GenericFragment extends Fragment {

    private Bundle backStackState = null;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        backStackState = new Bundle();
        onSaveInstanceState(backStackState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        backStackState = null;
    }

    public Bundle getBackStackState()
    {
        return backStackState;
    }

}
