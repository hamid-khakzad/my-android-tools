package cn.emagsoftware.ui.theme;

import java.util.List;

import cn.emagsoftware.content.pm.PackageMgr;
import cn.emagsoftware.ui.recreate.RecreateManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

public final class ThemeManager
{

    static String               CUR_PACKAGENAME               = null;
    static String               CUR_GENERALTHEME_NAME         = null;

    private static final String ACTION_MYANDROIDTOOLS_THEME   = "android.intent.action.MYANDROIDTOOLS_THEME";
    private static final String CATEGORY_MYANDROIDTOOLS_THEME = "android.intent.category.MYANDROIDTOOLS_THEME";

    private ThemeManager()
    {
    }

    public static List<ResolveInfo> queryThemes(Context context)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_MYANDROIDTOOLS_THEME);
        intent.addCategory(CATEGORY_MYANDROIDTOOLS_THEME);
        return PackageMgr.queryIntentActivities(context, intent, true);
    }

    public static boolean isThemeExist(Context context, String packageName)
    {
        if (packageName == null || packageName.trim().equals(""))
            return false;
        ApplicationInfo application = PackageMgr.getInstalledApplication(context, packageName);
        return application == null ? false : true;
    }

    /**
     * <p>�ı�����
     * 
     * @param packageName ��ҪӦ�õ�������������Ҫʹ��Ĭ�����⣬�ɴ�null
     * @param generalThemeName �����styles.xml��ͨ��������ʽ�����֣����û��ͨ��������ʽ���ɴ�null
     */
    public static void changeTheme(String packageName, String generalThemeName)
    {
        CUR_PACKAGENAME = packageName;
        CUR_GENERALTHEME_NAME = generalThemeName;
        RecreateManager.recreateAll();
    }

    public static String getCurPackageName()
    {
        return CUR_PACKAGENAME;
    }

    public static String getCurGeneralThemeName()
    {
        return CUR_GENERALTHEME_NAME;
    }

    public static void setApplyTheme(Context context, boolean shouldApplyTheme)
    {
        ThemeFactory.createOrUpdateInstance(context, CUR_PACKAGENAME, CUR_GENERALTHEME_NAME).setApplyTheme(shouldApplyTheme);
    }

    public static boolean getApplyTheme(Context context)
    {
        return ThemeFactory.createOrUpdateInstance(context, CUR_PACKAGENAME, CUR_GENERALTHEME_NAME).getApplyTheme();
    }

}
