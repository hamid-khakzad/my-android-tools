package cn.emagsoftware.ui.theme;

import cn.emagsoftware.ui.GenericActivity;
import android.os.Bundle;

import java.util.Arrays;

public class ThemeActivity extends GenericActivity
{

    public static final String REFRESH_TYPE_THEME_CHANGED = "ThemeActivity.REFRESH_TYPE_THEME_CHANGED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().setFactory(ThemeFactory.createOrUpdateInstance(this, ThemeManager.CUR_PACKAGENAME, ThemeManager.CUR_GENERALTHEME_NAME));
        String[] refreshTypes = getRefreshTypes();
        if(refreshTypes == null || Arrays.binarySearch(refreshTypes,REFRESH_TYPE_THEME_CHANGED) < 0)
            throw new IllegalStateException("getRefreshTypes() should include ThemeActivity.REFRESH_TYPE_THEME_CHANGED");
    }

    @Override
    protected String[] getRefreshTypes() {
        super.getRefreshTypes();
        return new String[]{REFRESH_TYPE_THEME_CHANGED};
    }

    @Override
    protected final void dispatchRefresh(String refreshType, Bundle bundle) {
        if(REFRESH_TYPE_THEME_CHANGED.equals(refreshType)) recreate();
        super.dispatchRefresh(refreshType, bundle);
    }

}
