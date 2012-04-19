package cn.emagsoftware.ui.theme;

import java.util.List;
import java.util.Vector;

import cn.emagsoftware.content.pm.PackageMgr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

public final class ThemeEngine
{

    public static String                 CUR_PACKAGENAME       = null;
    public static String                 CUR_THEMENAME         = null;

    private static String                THEME_INTENT_ACTION   = "android.intent.action.MYANDROIDTOOLS_THEME";
    private static String                THEME_INTENT_CATEGORY = "android.intent.category.MYANDROIDTOOLS_THEME";
    private static Vector<ThemeActivity> mThemeActivities      = new Vector<ThemeActivity>();

    private ThemeEngine()
    {
    }

    public static List<ResolveInfo> queryThemes(Context context)
    {
        Intent intent = new Intent();
        intent.setAction(THEME_INTENT_ACTION);
        intent.addCategory(THEME_INTENT_CATEGORY);
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
     * <p>改变主题
     * 
     * @param context
     * @param packageName 需要应用的主题包名，如果要使用默认主题，可传null
     * @param themeName 主题包styles.xml中通用主题样式的名字，如果没有通用主题样式，可传null
     */
    public static void changeTheme(Context context, String packageName, String themeName)
    {
        ThemeFactory.createOrUpdateInstance(context, packageName, themeName);
        for (int i = 0; i < mThemeActivities.size(); i++)
        {
            ThemeActivity tActivity = mThemeActivities.get(i);
            tActivity.resetContentView();
        }
        CUR_PACKAGENAME = packageName;
        CUR_THEMENAME = themeName;
    }

    public static boolean addThemeActivity(ThemeActivity mThemeActivity)
    {
        return mThemeActivities.add(mThemeActivity);
    }

    public static boolean removeThemeActivity(ThemeActivity mThemeActivity)
    {
        return mThemeActivities.remove(mThemeActivity);
    }

}
