package cn.emagsoftware.net.wifi;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public abstract class WifiCallback extends BroadcastReceiver {
	
	public static final int ACTION_WIFI_EXIST = 0;
	public static final int ACTION_WIFI_ENABLED = 1;
	public static final int ACTION_WIFI_ENABLING = 2;
	public static final int ACTION_WIFI_DISABLED = 3;
	public static final int ACTION_WIFI_DISABLING = 4;
	public static final int ACTION_SCAN_RESULTS = 5;
	public static final int ACTION_NETWORK_CONNECTED = 6;
	public static final int ACTION_NETWORK_OBTAININGIP = 7;
	public static final int ACTION_NETWORK_DISCONNECTED = 8;
	public static final int ACTION_ERROR = 9;
	
	protected Context context = null;
	protected Handler handler = new Handler(Looper.getMainLooper());
	protected int[] autoUnregisterActions = new int[]{};
	protected int timeoutForAutoUnregisterActions = 0;
	protected boolean isDoneForAutoUnregisterActions = false;
	
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
		WifiUtils wifiUtils = WifiUtils.getInstance(arg0);
		if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
			int state = arg1.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
			if(state == WifiManager.WIFI_STATE_ENABLED){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_EXIST) > -1) {
						isDoneForAutoUnregisterActions = true;
						unregisterMe();
					}
					onWifiExist();
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLED) < 0) onWifiEnabled();
				}else{
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_EXIST) > -1 || Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLED) > -1) {
						isDoneForAutoUnregisterActions = true;
						unregisterMe();
					}
					onWifiExist();
					onWifiEnabled();
				}
			}else if(state == WifiManager.WIFI_STATE_ENABLING){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_EXIST) > -1) {
						isDoneForAutoUnregisterActions = true;
						unregisterMe();
					}
					onWifiExist();
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLING) < 0) onWifiEnabling();
				}else{
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_EXIST) > -1 || Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLING) > -1) {
						isDoneForAutoUnregisterActions = true;
						unregisterMe();
					}
					onWifiExist();
					onWifiEnabling();
				}
    		}else if(state == WifiManager.WIFI_STATE_DISABLED){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_EXIST) > -1) {
						isDoneForAutoUnregisterActions = true;
						unregisterMe();
					}
					onWifiExist();
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLED) < 0) onWifiDisabled();
				}else{
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_EXIST) > -1 || Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLED) > -1) {
						isDoneForAutoUnregisterActions = true;
						unregisterMe();
					}
					onWifiExist();
					onWifiDisabled();
				}
    		}else if(state == WifiManager.WIFI_STATE_DISABLING){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_EXIST) > -1) {
						isDoneForAutoUnregisterActions = true;
						unregisterMe();
					}
					onWifiExist();
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLING) < 0) onWifiDisabling();
				}else{
					if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_EXIST) > -1 || Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLING) > -1) {
						isDoneForAutoUnregisterActions = true;
						unregisterMe();
					}
					onWifiExist();
					onWifiDisabling();
				}
    		}else if(state == WifiManager.WIFI_STATE_UNKNOWN){
				if(!isWifiStateRefreshed) {
					isWifiStateRefreshed = true;
				}
				//ACTION_ERROR若被设为自动反注册，将在第一次接收到时即进行，ACTION_WIFI_EXIST也是如此
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_ERROR) > -1) {
					isDoneForAutoUnregisterActions = true;
					unregisterMe();
				}
				onError();
    		}
		}else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
			List<ScanResult> results = wifiUtils.getWifiManager().getScanResults();
			if(Arrays.binarySearch(autoUnregisterActions, ACTION_SCAN_RESULTS) > -1) {
				isDoneForAutoUnregisterActions = true;
				unregisterMe();
			}
			for(int i = 0;i < results.size();i++){
				ScanResult curr = results.get(i);
				for(int j = 0;j < i;j++){
					ScanResult pre = results.get(j);
					if(curr.SSID.equals(pre.SSID) && wifiUtils.getScanResultSecurity(curr).equals(wifiUtils.getScanResultSecurity(pre))){
						results.remove(i);
						i--;
						if(curr.level > pre.level){
							results.remove(j);
							results.add(j, curr);
						}
						break;
					}
				}
			}
			onScanResults(results);
		}else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
			NetworkInfo networkInfo = arg1.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
				NetworkInfo.DetailedState detailed = networkInfo.getDetailedState();
				if(detailed == NetworkInfo.DetailedState.CONNECTED){
					Log.d("WifiCallback", "get network state -> CONNECTED");
					if(!isNetworkStateRefreshed) {
						isNetworkStateRefreshed = true;
						isNetworkStateRefreshedToConnected = true;
						if(Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_CONNECTED) < 0) onNetworkConnected(wifiUtils.getConnectionInfo());
					}else{
						if(Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_CONNECTED) > -1) {
							isDoneForAutoUnregisterActions = true;
							unregisterMe();
						}
						onNetworkConnected(wifiUtils.getConnectionInfo());
					}
				}else if(detailed == NetworkInfo.DetailedState.OBTAINING_IPADDR){
					Log.d("WifiCallback", "get network state -> OBTAINING_IPADDR");
					if(!isNetworkStateRefreshed) {
						isNetworkStateRefreshed = true;
						if(Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_OBTAININGIP) < 0) onNetworkObtainingIp(wifiUtils.getConnectionInfo());
					}else{
						if(Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_OBTAININGIP) > -1) {
							isDoneForAutoUnregisterActions = true;
							unregisterMe();
						}
						onNetworkObtainingIp(wifiUtils.getConnectionInfo());
					}
    			}else if(detailed == NetworkInfo.DetailedState.DISCONNECTED){
    				Log.d("WifiCallback", "get network state -> DISCONNECTED");
					if(!isNetworkStateRefreshed) {
						isNetworkStateRefreshed = true;
						if(Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_DISCONNECTED) < 0) onNetworkDisconnected(wifiUtils.getConnectionInfo());
					}else{
						networkDisconnectedCount = networkDisconnectedCount + 1;
						if(Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_DISCONNECTED) > -1){
							if(isNetworkStateRefreshedToConnected){
								if(networkDisconnectedCount >= 2){
									isDoneForAutoUnregisterActions = true;
									unregisterMe();
									onNetworkDisconnected(wifiUtils.getConnectionInfo());
								}
							}else{
								isDoneForAutoUnregisterActions = true;
								unregisterMe();
								onNetworkDisconnected(wifiUtils.getConnectionInfo());
							}
						}else{
							onNetworkDisconnected(wifiUtils.getConnectionInfo());
						}
					}
    			}
			}
		}
	}
	
	public void onWifiExist(){}
	
	public void onWifiEnabled(){}
	
	public void onWifiEnabling(){}
	
	public void onWifiDisabled(){}
	
	public void onWifiDisabling(){}
	
	public void onScanResults(List<ScanResult> scanResults){}
	
	public void onNetworkConnected(WifiInfo wifiInfo){}
	
	public void onNetworkObtainingIp(WifiInfo wifiInfo){}
	
	public void onNetworkDisconnected(WifiInfo wifiInfo){}
	
	public void onError(){}
	
	public void onTimeout(){}
	
	public void registerMe(){
		IntentFilter wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        isDoneForAutoUnregisterActions = false;
        context.registerReceiver(this,wifiIntentFilter);
        if(timeoutForAutoUnregisterActions > 0){   //为0时将永不超时
            new Timer().schedule(new TimerTask() {
            	protected long timeCount = 0;
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				timeCount = timeCount + 100;
    				if(isDoneForAutoUnregisterActions){
    					cancel();
    				}else if(timeCount >= timeoutForAutoUnregisterActions){   //已超时
    					unregisterMe();
    					cancel();
    					handler.post(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								onTimeout();
							}
						});
    				}
    			}
    		},100,100);
        }
	}
	
	public void unregisterMe(){
		isDoneForAutoUnregisterActions = true;   //在反注册时置为true，使计时器能够尽快退出
		isWifiStateRefreshed = false;
		isNetworkStateRefreshed = false;
		isNetworkStateRefreshedToConnected = false;
		networkDisconnectedCount = 0;
		context.unregisterReceiver(this);
	}
	
	public void setAutoUnregisterActions(int[] actions){
		if(actions == null) throw new NullPointerException();
		Arrays.sort(actions);
		this.autoUnregisterActions = actions;
	}
	
	/**
	 * 设置等待自动反注册ACTION被执行的超时时间，超时时将回调onTimeout方法并自动反注册
	 * timeout设为0，将永不超时
	 * @param timeout
	 */
	public void setTimeoutForAutoUnregisterActions(int timeout){
		if(timeout < 0) throw new IllegalArgumentException("timeout could not be below zero.");
		this.timeoutForAutoUnregisterActions = timeout;
	}
	
}
