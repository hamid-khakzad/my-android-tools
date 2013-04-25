package cn.emagsoftware.ui.theme;

import cn.emagsoftware.ui.recreate.RecreateActivity;
import android.os.Bundle;

public class ThemeActivity extends RecreateActivity
{

    @Override
    protected void onCreateImpl(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreateImpl(savedInstanceState);
        getLayoutInflater().setFactory(ThemeFactory.createOrUpdateInstance(this, ThemeEngine.CUR_PACKAGENAME, ThemeEngine.CUR_GENERALTHEME_NAME));
    }

}
