package cn.emagsoftware.ui.theme;

import cn.emagsoftware.ui.recreate.RecreateActivity;
import android.os.Bundle;

public class ThemeActivity extends RecreateActivity
{

    @Override
    protected void onCreateImpl(Bundle savedInstanceState)
    {
        super.onCreateImpl(savedInstanceState);
        getLayoutInflater().setFactory(ThemeFactory.createOrUpdateInstance(this, ThemeManager.CUR_PACKAGENAME, ThemeManager.CUR_GENERALTHEME_NAME));
    }

}
