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
	 * <p>��ȡ�Ѱ�װ��ApplicationInfo�б�
	 * @param context
	 * @param isSort Ϊtrueʱ������ApplicationInfo.DisplayNameComparator����
	 * @return
	 */
	public static List<ApplicationInfo> getInstalledApplications(Context context,boolean isSort){
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> applicationInfos = pm.getInstalledApplications(0);
		if(isSort) Collections.sort(applicationInfos, new ApplicationInfo.DisplayNameComparator(pm));
		return applicationInfos;
	}
	
	/**
	 * <p>����packageName��ȡApplicationInfo����δ�ҵ�ָ����packageName��������null
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
	 * <p>��ȡָ��Intent��ResolveInfo�б�
	 * @param context
	 * @param intent
	 * @param isSort Ϊtrueʱ������ResolveInfo.DisplayNameComparator����
	 * @return
	 */
	public static List<ResolveInfo> queryIntentActivities(Context context,Intent intent,boolean isSort){
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
		if(isSort) Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
		return resolveInfos;
	}
	
	/**
	 * <p>��ȡָ��Intent��packageName��ResolveInfo�б�
	 * @param context
	 * @param intent
	 * @param packageName
	 * @param isSort Ϊtrueʱ������ResolveInfo.DisplayNameComparator����
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
	 * <p>��ȡ��ָ��packageName������Activity�������Intent����δ�ҵ�ָ����packageName��packageName��Ӧ�ĳ���������Activity��������null
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static Intent getLaunchIntentForPackage(Context context,String packageName){
		return context.getPackageManager().getLaunchIntentForPackage(packageName);
	}
	
}
