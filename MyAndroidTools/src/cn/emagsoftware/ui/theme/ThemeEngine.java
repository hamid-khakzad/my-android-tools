package cn.emagsoftware.ui.theme;

import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;

public final class ThemeEngine {
	
	public static String CUR_PACKAGENAME = null;
	public static String CUR_THEMENAME = null;
	
	private static String THEME_INTENT_ACTION = "android.intent.action.MYANDROIDTOOLS_THEME";
	private static String THEME_INTENT_CATEGORY = "android.intent.category.MYANDROIDTOOLS_THEME";
	private static Vector<ThemeActivity> mThemeActivities = new Vector<ThemeActivity>();
	
	private ThemeEngine(){}
	
	public static List<ResolveInfo> queryThemes(Context context){
    	PackageManager pm = context.getPackageManager();
		Intent intent = new Intent();
		intent.setAction(THEME_INTENT_ACTION);
		intent.addCategory(THEME_INTENT_CATEGORY);
		List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
		return apps;
	}
	
	public static boolean isThemeExist(Context context,String packageName){
		if(packageName == null || packageName.trim().equals("")) return false;
		PackageManager pm = context.getPackageManager();
		try{
			pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		}catch(NameNotFoundException e){
			return false;
		}
	}
	
	/**
	 * <p>�ı�����
	 * @param context
	 * @param packageName ��ҪӦ�õ�������������Ҫʹ��Ĭ�����⣬�ɴ�null
	 * @param themeName �����styles.xml��ͨ��������ʽ�����֣����û��ͨ��������ʽ���ɴ�null
	 */
	public static void changeTheme(Context context,String packageName,String themeName){
		ThemeFactory.createOrUpdateInstance(context, packageName, themeName);
		for(int i = 0;i < mThemeActivities.size();i++){
			ThemeActivity tActivity = mThemeActivities.get(i);
			View prevContentView = tActivity.curContentView;
			if(tActivity.resetUI()) tActivity.onInit(prevContentView);
		}
		CUR_PACKAGENAME = packageName;
		CUR_THEMENAME = themeName;
	}
	
	public static boolean addThemeActivity(ThemeActivity mThemeActivity){
		return mThemeActivities.add(mThemeActivity);
	}
	
	public static boolean removeThemeActivity(ThemeActivity mThemeActivity){
		return mThemeActivities.remove(mThemeActivity);
	}
	
}
