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
     * <p>�ı�����
     * 
     * @param context
     * @param packageName ��ҪӦ�õ�������������Ҫʹ��Ĭ�����⣬�ɴ�null
     * @param themeName �����styles.xml��ͨ��������ʽ�����֣����û��ͨ��������ʽ���ɴ�null
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
