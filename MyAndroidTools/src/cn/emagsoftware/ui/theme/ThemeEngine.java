package cn.emagsoftware.ui.theme;

import java.util.List;
import java.util.Vector;

import cn.emagsoftware.content.pm.PackageMgr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

/**
 * @deprecated 请使用cn.emagsoftware.ui.theme2包执行主题切换功能
 * @author Wendell
 * 
 */
public final class ThemeEngine
{

    static String                        CUR_PACKAGENAME       = null;
    static String                        CUR_GENERALTHEME_NAME = null;

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
     * @param generalThemeName 主题包styles.xml中通用主题样式的名字，如果没有通用主题样式，可传null
     */
    public static void changeTheme(Context context, String packageName, String generalThemeName)
    {
        CUR_PACKAGENAME = packageName;
        CUR_GENERALTHEME_NAME = generalThemeName;
        ThemeFactory.createOrUpdateInstance(context, packageName, generalThemeName);
        for (int i = 0; i < mThemeActivities.size(); i++)
        {
            ThemeActivity tActivity = mThemeActivities.get(i);
            tActivity.resetContentView();
        }
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

    public static boolean addThemeActivity(ThemeActivity mThemeActivity)
    {
        return mThemeActivities.add(mThemeActivity);
    }

    public static boolean removeThemeActivity(ThemeActivity mThemeActivity)
    {
        return mThemeActivities.remove(mThemeActivity);
    }

}
