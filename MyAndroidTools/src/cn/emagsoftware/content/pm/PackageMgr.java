package cn.emagsoftware.content.pm;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public final class PackageMgr {
	
	private PackageMgr(){}
	
	/**
	 * <p>获取已安装的ApplicationInfo列表
	 * @param context
	 * @return
	 */
	public static List<ApplicationInfo> getInstalledApplications(Context context){
		return context.getPackageManager().getInstalledApplications(0);
	}
	
	/**
	 * <p>根据packageName获取ApplicationInfo。若未找到指定的packageName，将返回null
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static ApplicationInfo getInstalledApplication(Context context,String packageName){
		List<ApplicationInfo> applications = getInstalledApplications(context);
		for(ApplicationInfo application:applications){
			if(application.packageName.equals(packageName)){
				return application;
			}
		}
		return null;
	}
	
	/**
	 * <p>获取指定Intent的ResolveInfo列表，将按照ResolveInfo.DisplayNameComparator排序
	 * @param context
	 * @param intent
	 * @return
	 */
	public static List<ResolveInfo> queryIntentActivities(Context context,Intent intent){
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
		Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
		return resolveInfos;
	}
	
}
