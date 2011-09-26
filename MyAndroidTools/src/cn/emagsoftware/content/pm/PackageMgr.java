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
	 * <p>��ȡ�Ѱ�װ��ApplicationInfo�б�
	 * @param context
	 * @return
	 */
	public static List<ApplicationInfo> getInstalledApplications(Context context){
		return context.getPackageManager().getInstalledApplications(0);
	}
	
	/**
	 * <p>����packageName��ȡApplicationInfo����δ�ҵ�ָ����packageName��������null
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
	 * <p>��ȡָ��Intent��ResolveInfo�б�������ResolveInfo.DisplayNameComparator����
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
