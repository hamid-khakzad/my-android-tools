package cn.emagsoftware.ui.test;

import cn.emagsoftware.ui.GenericActivity;
import cn.emagsoftware.ui.R;
import android.os.Bundle;

public class TestActivity extends GenericActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
    }

    @Override
    protected void onRestoreStaticState() {
    }

}
