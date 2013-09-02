package cn.emagsoftware.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Wendell on 13-8-28.
 */
public abstract class GenericFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!GenericActivity.IS_RUNNING)
        {
            onRestoreStaticState();
            GenericActivity.IS_RUNNING = true;
        }
    }

    protected abstract void onRestoreStaticState();

}
