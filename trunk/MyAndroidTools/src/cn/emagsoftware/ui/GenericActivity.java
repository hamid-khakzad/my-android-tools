package cn.emagsoftware.ui;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Wendell on 13-8-28.
 */
public abstract class GenericActivity extends Activity {

    static boolean IS_RUNNING = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!IS_RUNNING)
        {
            if(!isFirstForProcess())
                onRestoreStaticState();
            IS_RUNNING = true;
        }
    }

    protected abstract boolean isFirstForProcess();

    protected abstract void onRestoreStaticState();

}
