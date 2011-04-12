package cn.emagsoftware.wifi;

import java.util.List;

import com.farproc.wifi.connecter.Wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public final class WifiUtils {
	
	private static WifiUtils wifiUtils = null;
	
	private Context context = null;
	private WifiManager wifiManager = null;
	private WifiLock wifiLock = null;
	private ConnectivityManager connectivityManager = null;
	
	public synchronized static WifiUtils getInstance(Context context){
		if(wifiUtils != null) return wifiUtils;
		return new WifiUtils(context);
	}
	
	private WifiUtils(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
		wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		wifiLock = wifiManager.createWifiLock("cn.emagsoftware.wifi.WifiUtils");
		connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	public boolean isWifiEnabled(){
		return wifiManager.isWifiEnabled();
	}
	
	public WifiInfo getConnectedInfo(){
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);    
		if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()){
			return wifiManager.getConnectionInfo();
		}
		return null;
	}
	
	public void lockWifi(){
		wifiLock.acquire();
	}
	
	public void unlockWifi(){
		if(!wifiLock.isHeld()){
			wifiLock.release();
		}
	}
	
	public List<WifiConfiguration> getConfigurations(){
		return wifiManager.getConfiguredNetworks();
	}
	
	public WifiConfiguration getConfiguration(ScanResult sr){
		return Wifi.getWifiConfiguration(wifiManager, sr, null);
	}
	
	public String getScanResultSecurity(ScanResult sr){
		return Wifi.getScanResultSecurity(sr);
	}
	
	public boolean disconnect(){
		return wifiManager.disconnect();
	}
	
	public WifiManager getWifiManager(){
		return wifiManager;
	}
	
	public void setWifiEnabled(boolean enabled,WifiCallback callback){
		if(enabled == isWifiEnabled()){
			if(callback != null){
				if(enabled) callback.onWifiEnabled();
				else callback.onWifiDisabled();
			}
			return;
		}
		if(callback != null){
			if(enabled) callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_WIFI_ENABLED});
			else callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_WIFI_DISABLED});
			callback.registerMe();
		}
		boolean circs = wifiManager.setWifiEnabled(enabled);
		if(!circs) if(callback != null) {
			callback.unregisterMe();
			callback.onCallbackFailure();
		}
	}
	
	public void startScan(WifiCallback callback){
		if(!isWifiEnabled()) {
			if(callback != null) callback.onCallbackFailure();
			return;
		}
		if(callback != null){
			callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_SCAN_RESULTS});
			callback.registerMe();
		}
		boolean circs = wifiManager.startScan();
		if(!circs) if(callback != null){
			callback.unregisterMe();
			callback.onCallbackFailure();
		}
	}
	
	public void connect(WifiConfiguration wc,WifiCallback callback){
		if(!isWifiEnabled()) {
			if(callback != null) callback.onCallbackFailure();
			return;
		}
		if(callback != null){
			callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_NETWORK_CONNECTED,WifiCallback.ACTION_NETWORK_DISCONNECTED});
			callback.registerMe();
		}
		boolean circs = Wifi.connectToConfiguredNetwork(context, wifiManager, wc, true);
		if(!circs) if(callback != null){
			callback.unregisterMe();
			callback.onCallbackFailure();
		}
	}
	
	public void connect(ScanResult sr,String password,WifiCallback callback){
		if(!isWifiEnabled()) {
			if(callback != null) callback.onCallbackFailure();
			return;
		}
		WifiConfiguration old = getConfiguration(sr);
		if(old != null){
			String security = getScanResultSecurity(sr);
			Wifi.setupSecurity(old, security, password);
			if(!wifiManager.saveConfiguration()) {
				if(callback != null) callback.onCallbackFailure();
				return;
			}
		}
		if(callback != null){
			callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_NETWORK_CONNECTED,WifiCallback.ACTION_NETWORK_DISCONNECTED});
			callback.registerMe();
		}
		boolean circs;
		if(old != null) circs = Wifi.connectToConfiguredNetwork(context, wifiManager, old, true);
		else circs = Wifi.connectToNewNetwork(context, wifiManager, sr, password, Integer.MAX_VALUE);
		if(!circs) if(callback != null) {
			callback.unregisterMe();
			callback.onCallbackFailure();
		}
	}
	
}
