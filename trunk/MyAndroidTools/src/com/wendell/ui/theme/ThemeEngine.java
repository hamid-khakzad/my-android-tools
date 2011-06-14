package com.wendell.ui.theme;

import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public final class ThemeEngine {
	
	private static String THEME_INTENT_ACTION = "android.intent.action.MYANDROIDTOOLS_THEME";
	private static String THEME_INTENT_CATEGORY = "android.intent.category.MYANDROIDTOOLS_THEME";
	private static Vector<ThemeActivity> mThemeActivities = new Vector<ThemeActivity>();
	
	private ThemeEngine(){}
	
	public static List<ResolveInfo> queryThemePackages(Context context){
    	PackageManager pm = context.getPackageManager();
		Intent intent = new Intent();
		intent.setAction(THEME_INTENT_ACTION);
		intent.addCategory(THEME_INTENT_CATEGORY);
		List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
		return apps;
	}
	
	public static void changeTheme(String packageName,String themeName){
		for(int i = 0;i < mThemeActivities.size();i++){
			mThemeActivities.get(i).changeTheme(packageName, themeName);
		}
	}
	
	public static boolean addThemeActivity(ThemeActivity mThemeActivity){
		return mThemeActivities.add(mThemeActivity);
	}
	
	public static boolean removeThemeActivity(ThemeActivity mThemeActivity){
		return mThemeActivities.remove(mThemeActivity);
	}
	
}
