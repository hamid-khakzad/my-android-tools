package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Wendell on 13-8-26.
 */
public class GenericFragment extends Fragment {

    private Bundle detachState = null;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachState = new Bundle();
        onSaveInstanceState(detachState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachState = null;
    }

    public Bundle getDetachState()
    {
        return detachState;
    }

}
