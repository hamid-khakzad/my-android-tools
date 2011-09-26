package cn.emagsoftware.content.pm;

import java.util.ArrayList;
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
	 * @param isSort 为true时将按照ApplicationInfo.DisplayNameComparator排序
	 * @return
	 */
	public static List<ApplicationInfo> getInstalledApplications(Context context,boolean isSort){
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> applicationInfos = pm.getInstalledApplications(0);
		if(isSort) Collections.sort(applicationInfos, new ApplicationInfo.DisplayNameComparator(pm));
		return applicationInfos;
	}
	
	/**
	 * <p>根据packageName获取ApplicationInfo。若未找到指定的packageName，将返回null
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static ApplicationInfo getInstalledApplication(Context context,String packageName){
		List<ApplicationInfo> applications = getInstalledApplications(context,false);
		for(ApplicationInfo application:applications){
			if(application.packageName.equals(packageName)){
				return application;
			}
		}
		return null;
	}
	
	/**
	 * <p>获取指定Intent的ResolveInfo列表
	 * @param context
	 * @param intent
	 * @param isSort 为true时将按照ResolveInfo.DisplayNameComparator排序
	 * @return
	 */
	public static List<ResolveInfo> queryIntentActivities(Context context,Intent intent,boolean isSort){
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
		if(isSort) Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
		return resolveInfos;
	}
	
	/**
	 * <p>获取指定Intent和packageName的ResolveInfo列表
	 * @param context
	 * @param intent
	 * @param packageName
	 * @param isSort 为true时将按照ResolveInfo.DisplayNameComparator排序
	 * @return
	 */
	public static List<ResolveInfo> queryIntentActivities(Context context,Intent intent,String packageName,boolean isSort){
		List<ResolveInfo> resolveInfos = queryIntentActivities(context,intent,false);
		List<ResolveInfo> filterResolveInfos = new ArrayList<ResolveInfo>();
		for(ResolveInfo resolveInfo:resolveInfos){
			if(resolveInfo.activityInfo.packageName.equals(packageName)){
				filterResolveInfos.add(resolveInfo);
			}
		}
		if(isSort) Collections.sort(filterResolveInfos, new ResolveInfo.DisplayNameComparator(context.getPackageManager()));
		return filterResolveInfos;
	}
	
	/**
	 * <p>获取与指定packageName中启动Activity相关联的Intent，若未找到指定的packageName或packageName对应的程序无启动Activity，将返回null
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static Intent getLaunchIntentForPackage(Context context,String packageName){
		return context.getPackageManager().getLaunchIntentForPackage(packageName);
	}
	
}
