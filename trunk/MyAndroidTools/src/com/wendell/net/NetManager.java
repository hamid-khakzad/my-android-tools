package com.wendell.net;

import java.io.IOException;
import java.net.HttpURLConnection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.wendell.net.http.HttpConnectionManager;
import com.wendell.net.http.HttpResponseResult;

public final class NetManager {
	
	private static final String USEFUL_TEST_URL = "http://www.baidu.com";
	private static final String USEFUL_TEST_HOST = "www.baidu.com";
	
	private NetManager(){}
	
	public static boolean isNetConnected(Context context){
		NetworkInfo info = getActiveNetworkInfo(context);
		if(info != null) return info.isConnected();
		return false;
	}
	
	public static boolean isNetUseful(int timeout,int tryTimes){
		if(tryTimes <= 0) throw new IllegalArgumentException("trying times should be greater than zero.");
		int th = 1;
		while(th <= tryTimes){
			try{
				HttpResponseResult result = HttpConnectionManager.doGet(USEFUL_TEST_URL, false, true, timeout, null);
				if(result.getResponseCode() == HttpURLConnection.HTTP_OK){
					String host = result.getResponseURL().getHost();
					String content = result.getDataString("gb2312");
					if(USEFUL_TEST_HOST.equalsIgnoreCase(host) && content.indexOf(USEFUL_TEST_HOST) >= 0) {    //若能访问到原始站点，证明网络有效
						return true;
					}
				}
			}catch(IOException e){
				Log.e("NetManager", "the "+th+" time to check net for method of isNetUseful failed.", e);
			}
			th++;
		}
		Log.e("NetManager", "checking net for method of isNetUseful has all failed,will return false.");
		return false;
	}
	
	public static NetworkInfo getActiveNetworkInfo(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getActiveNetworkInfo();
	}
	
	public static NetworkInfo getMobileNetworkInfo(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	}
	
	public static NetworkInfo getWifiNetworkInfo(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	}
	
	public static NetworkInfo[] getAllNetworkInfo(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivityManager.getAllNetworkInfo();
	}
	
}
