package cn.emagsoftware.ui.test;

import cn.emagsoftware.ui.GenericActionBarActivity;
import cn.emagsoftware.ui.R;
import android.os.Bundle;

public class TestActivity extends GenericActionBarActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        getSupportActionBar().setTitle("MyAndroidTools");
    }

    @Override
    protected void onRestoreStaticState() {
    }

}
