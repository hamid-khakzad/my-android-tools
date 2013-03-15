package cn.emagsoftware.ui.theme;

import cn.emagsoftware.ui.ConfigChangeHandlingActivity;
import android.os.Bundle;

/**
 * @deprecated ��ʹ��cn.emagsoftware.ui.theme2��ִ�������л�����
 * @author Wendell
 * 
 */
public abstract class ThemeActivity extends ConfigChangeHandlingActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ThemeEngine.addThemeActivity(this);
        getLayoutInflater().setFactory(ThemeFactory.createOrUpdateInstance(this, ThemeEngine.CUR_PACKAGENAME, ThemeEngine.CUR_GENERALTHEME_NAME));
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        ThemeEngine.removeThemeActivity(this);
    }

}
