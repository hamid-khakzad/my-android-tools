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
        if(!IS_RUNNING)
        {
            onRestoreStaticState();
            IS_RUNNING = true;
        }
        super.onCreate(savedInstanceState); // ���ܻᴥ��һЩ�û���Ϊ���ʷ��ڻָ���̬״̬֮��
    }

    protected abstract void onRestoreStaticState();

}
