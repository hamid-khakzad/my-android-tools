package cn.emagsoftware.net;

import java.io.IOException;
import java.net.HttpURLConnection;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import cn.emagsoftware.net.http.HttpConnectionManager;
import cn.emagsoftware.net.http.HttpResponseResult;

public final class NetManager {
	
	private static final String USEFUL_TEST_URL = "http://www.baidu.com";
	private static final String USEFUL_TEST_HOST = "www.baidu.com";
	
	private NetManager(){}
	
	public static boolean isNetConnected(Context context){
		NetworkInfo info = getActiveNetworkInfo(context);
		if(info != null) return info.getState() == NetworkInfo.State.CONNECTED;
		return false;
	}
	
	public static boolean isNetUseful(int timeout,int tryTimes){
		if(tryTimes <= 0) throw new IllegalArgumentException("trying times should be greater than zero.");
		int th = 1;
		while(th <= tryTimes){
			try{
				HttpResponseResult result = HttpConnectionManager.doGet(USEFUL_TEST_URL, "gb2312", true, timeout, null);
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
	
	/**
	 * <p>以小写形式返回指定网络的详细类型
	 * @param info
	 * @return wifi、cmnet、cmwap等
	 */
	public static String getNetworkInfoType(NetworkInfo info){
		String type = info.getTypeName().toLowerCase();
		if (type.equals("wifi")) return type;
		type = info.getExtraInfo().toLowerCase();
		return type;
	}
	
	/**
	 * <p>是否处于飞行模式
	 * @param context
	 * @return
	 */
	public static boolean isInAirplaneMode(Context context){
		return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	
	/**
	 * <p>是否存在多个已连接的网络
	 * @param context
	 * @return
	 */
	public static boolean isAvailableMultiConnectedNets(Context context){
		boolean wifiConnected = false;
		boolean otherConnected = false;
		NetworkInfo[] infos = getAllNetworkInfo(context);
		if(infos != null){
			for(int i = 0;i < infos.length;i++){
				if(infos[i].getState() == NetworkInfo.State.CONNECTED){
					if(infos[i].getType() == ConnectivityManager.TYPE_WIFI){
						wifiConnected = true;
					}else{
						otherConnected = true;
					}
				}
			}
		}
		return wifiConnected && otherConnected;
	}
	
	/**
	 * <p>打开系统网络设置的Activity
	 * @param context
	 */
	public static void startWirelessSettingsActivity(Context context){
		Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		context.startActivity(intent);
	}
	
}
