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
		return getActiveNetworkInfo(context).isConnected();
	}
	
	public static boolean isNetUseful(int timeout){
		try{
			HttpResponseResult result = HttpConnectionManager.doGet(USEFUL_TEST_URL, false, true, timeout, null);
			if(result.getResponseCode() != HttpURLConnection.HTTP_OK) return false;
			String host = result.getResponseURL().getHost();
			String content = result.getDataString("gb2312");
			if(USEFUL_TEST_HOST.equalsIgnoreCase(host) && content.indexOf(USEFUL_TEST_HOST) >= 0) {    //若能访问到原始站点，证明网络有效
				return true;
			}
			return false;
		}catch(IOException e){
			Log.e("NetManager", "connect to "+USEFUL_TEST_URL+" for method of isNetUseful occered an error,will return false.", e);
			return false;
		}
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
