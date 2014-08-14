package cn.emagsoftware.ui.test;

import cn.emagsoftware.ui.R;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class TestActivity extends ActionBarActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        getSupportActionBar().setTitle("MyAndroidTools");
    }
    
}
