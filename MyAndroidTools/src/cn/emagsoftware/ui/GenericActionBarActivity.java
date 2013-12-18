package cn.emagsoftware.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by Wendell on 13-8-28.
 */
public abstract class GenericActionBarActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!GenericActivity.IS_RUNNING)
        {
            onRestoreStaticState();
            GenericActivity.IS_RUNNING = true;
        }
        super.onCreate(savedInstanceState); // 可能会触发一些用户行为，故放在恢复静态状态之后
    }

    protected abstract void onRestoreStaticState();

}
