package cn.emagsoftware.wifi;

import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public abstract class WifiCallback extends BroadcastReceiver {
	
	public static final int ACTION_WIFI_ENABLED = 0;
	public static final int ACTION_WIFI_ENABLING = 1;
	public static final int ACTION_WIFI_DISABLED = 2;
	public static final int ACTION_WIFI_DISABLING = 3;
	public static final int ACTION_WIFI_UNKNOWN = 4;
	public static final int ACTION_SCAN_RESULTS = 5;
	public static final int ACTION_NETWORK_CONNECTED = 6;
	public static final int ACTION_NETWORK_OBTAININGIP = 7;
	public static final int ACTION_NETWORK_DISCONNECTED = 8;
	
	protected Context context = null;
	protected int[] autoUnregisterActions = new int[]{};
	
	protected boolean isWifiStateRefreshed = false;
	protected boolean isNetworkStateRefreshed = false;
	protected boolean isNetworkStateRefreshedToConnected = false;
	protected int networkDisconnectedCount = 0;
	
	public WifiCallback(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
		Arrays.sort(autoUnregisterActions);
	}
	
	@Override
	public final void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		String action = arg1.getAction();
		WifiManager wifiManager = WifiUtils.getInstance(arg0).getWifiManager();
		if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
			int state = arg1.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
			if(state == WifiManager.WIFI_STATE_ENABLED){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
					if(autoUnregisterActions.length == 0) onWifiEnabled();
				}else{
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLED) > -1) unregisterMe();
					onWifiEnabled();
				}
			}else if(state == WifiManager.WIFI_STATE_ENABLING){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
					if(autoUnregisterActions.length == 0) onWifiEnabling();
				}else{
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLING) > -1) unregisterMe();
					onWifiEnabling();
				}
    		}else if(state == WifiManager.WIFI_STATE_DISABLED){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
					if(autoUnregisterActions.length == 0) onWifiDisabled();
				}else{
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLED) > -1) unregisterMe();
					onWifiDisabled();
				}
    		}else if(state == WifiManager.WIFI_STATE_DISABLING){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
					if(autoUnregisterActions.length == 0) onWifiDisabling();
				}else{
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLING) > -1) unregisterMe();
					onWifiDisabling();
				}
    		}else if(state == WifiManager.WIFI_STATE_UNKNOWN){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
					if(autoUnregisterActions.length == 0) onWifiUnknown();
				}else{
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_UNKNOWN) > -1) unregisterMe();
					onWifiUnknown();
				}
    		}
		}else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
			List<ScanResult> results = wifiManager.getScanResults();
			if(Arrays.binarySearch(autoUnregisterActions, ACTION_SCAN_RESULTS) > -1) unregisterMe();
			onScanResults(results);
		}else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
			NetworkInfo networkInfo = arg1.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
				NetworkInfo.DetailedState detailed = networkInfo.getDetailedState();
				if(detailed == NetworkInfo.DetailedState.CONNECTED){
					if(!isNetworkStateRefreshed) {
						isNetworkStateRefreshed = true;
						isNetworkStateRefreshedToConnected = true;
						if(autoUnregisterActions.length == 0) onNetworkConnected(wifiManager.getConnectionInfo());
					}else{
						if(Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_CONNECTED) > -1) unregisterMe();
						onNetworkConnected(wifiManager.getConnectionInfo());
					}
				}else if(detailed == NetworkInfo.DetailedState.OBTAINING_IPADDR){
					if(!isNetworkStateRefreshed) {
						isNetworkStateRefreshed = true;
						if(autoUnregisterActions.length == 0) onNetworkObtainingIp(wifiManager.getConnectionInfo());
					}else{
						if(Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_OBTAININGIP) > -1) unregisterMe();
						onNetworkObtainingIp(wifiManager.getConnectionInfo());
					}
    			}else if(detailed == NetworkInfo.DetailedState.DISCONNECTED){
					if(!isNetworkStateRefreshed) {
						isNetworkStateRefreshed = true;
						if(autoUnregisterActions.length == 0) onNetworkDisconnected(wifiManager.getConnectionInfo());
					}else{
						networkDisconnectedCount = networkDisconnectedCount + 1;
						if(Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_DISCONNECTED) > -1){
							if(isNetworkStateRefreshedToConnected){
								if(networkDisconnectedCount >= 2){
									isNetworkStateRefreshedToConnected = false;
									networkDisconnectedCount = 0;
									unregisterMe();
									onNetworkDisconnected(wifiManager.getConnectionInfo());
								}
							}else{
								networkDisconnectedCount = 0;
								unregisterMe();
								onNetworkDisconnected(wifiManager.getConnectionInfo());
							}
						}else{
							onNetworkDisconnected(wifiManager.getConnectionInfo());
						}
					}
    			}
			}
		}
	}
	
	public void onWifiEnabled(){}
	
	public void onWifiEnabling(){}
	
	public void onWifiDisabled(){}
	
	public void onWifiDisabling(){}
	
	public void onWifiUnknown(){}
	
	public void onScanResults(List<ScanResult> scanResults){}
	
	public void onNetworkConnected(WifiInfo wifiInfo){}
	
	public void onNetworkObtainingIp(WifiInfo wifiInfo){}
	
	public void onNetworkDisconnected(WifiInfo wifiInfo){}
	
	public void registerMe(){
		IntentFilter wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.registerReceiver(this,wifiIntentFilter);
	}
	
	public void unregisterMe(){
		context.unregisterReceiver(this);
	}
	
	public void setAutoUnregisterActions(int[] actions){
		if(actions == null) throw new NullPointerException();
		Arrays.sort(actions);
		this.autoUnregisterActions = actions;
	}
	
}
