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

/**
 * <p>Wifi�����Ĺ㲥�����࣬ע�⣬����ʵ���Ƿ��̰߳�ȫ��
 * <p>����ɶ���ʹ�ã�Ҳ����WifiUtils�������Ϊ�ص���ʹ�á�
 * <p>��Ϊ�ص���ʹ��ʱ�����ڲ�ͬ�Ļص���ʹ��ͬһʵ����Ҫȷ����һ���ص��ѽ��������Ѿ��Զ���ע��
 * @author Wendell
 * @version 1.2
 */
public abstract class WifiCallback extends BroadcastReceiver {
	
	public static final int ACTION_WIFI_ENABLED = 0;
	public static final int ACTION_WIFI_ENABLING = 1;
	public static final int ACTION_WIFI_DISABLED = 2;
	public static final int ACTION_WIFI_DISABLING = 3;
	public static final int ACTION_ERROR = 4;
	public static final int ACTION_SCAN_RESULTS = 5;
	public static final int ACTION_NETWORK_CONNECTED = 6;
	public static final int ACTION_NETWORK_OBTAININGIP = 7;
	public static final int ACTION_NETWORK_DISCONNECTED = 8;
	
	protected Context context = null;
	protected Handler handler = new Handler(Looper.getMainLooper());
	protected int[] autoUnregisterActions = new int[]{};
	protected int timeoutForAutoUnregisterActions = 0;
	protected boolean isDoneForAutoUnregisterActions = false;
	protected boolean isUnregistered = true;
	
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
		if(isUnregistered) return;    //����Ѿ���ע�ᣬ��ֱ�ӷ���
		String action = arg1.getAction();
		WifiUtils wifiUtils = WifiUtils.getInstance(arg0);
		if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
			int state = arg1.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
			if(state == WifiManager.WIFI_STATE_ENABLED){
				Log.d("WifiCallback", "receive wifi state -> WIFI_STATE_ENABLED");
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLED) > -1) {
					isDoneForAutoUnregisterActions = true;
					unregisterMe();
				}
				onWifiEnabled();
			}else if(state == WifiManager.WIFI_STATE_ENABLING){
				Log.d("WifiCallback", "receive wifi state -> WIFI_STATE_ENABLING");
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLING) > -1) {
					isDoneForAutoUnregisterActions = true;
					unregisterMe();
				}
				onWifiEnabling();
    		}else if(state == WifiManager.WIFI_STATE_DISABLED){
    			Log.d("WifiCallback", "receive wifi state -> WIFI_STATE_DISABLED");
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLED) > -1) {
					isDoneForAutoUnregisterActions = true;
					unregisterMe();
				}
				onWifiDisabled();
    		}else if(state == WifiManager.WIFI_STATE_DISABLING){
    			Log.d("WifiCallback", "receive wifi state -> WIFI_STATE_DISABLING");
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLING) > -1) {
					isDoneForAutoUnregisterActions = true;
					unregisterMe();
				}
				onWifiDisabling();
    		}else if(state == WifiManager.WIFI_STATE_UNKNOWN){
    			Log.d("WifiCallback", "receive wifi state -> WIFI_STATE_UNKNOWN");
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_ERROR) > -1) {
					isDoneForAutoUnregisterActions = true;
					unregisterMe();
				}
				onError();
    		}
		}else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
			Log.d("WifiCallback", "receive wifi state -> SCAN_RESULTS_AVAILABLE");
			if(Arrays.binarySearch(autoUnregisterActions, ACTION_SCAN_RESULTS) > -1) {
				isDoneForAutoUnregisterActions = true;
				unregisterMe();
			}
			List<ScanResult> results = wifiUtils.getWifiManager().getScanResults();
			if(results != null){
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
			}
			onScanResults(results);
		}else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
			NetworkInfo networkInfo = arg1.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
				NetworkInfo.DetailedState detailed = networkInfo.getDetailedState();
				if(detailed == NetworkInfo.DetailedState.CONNECTED){
					Log.d("WifiCallback", "receive wifi state -> CONNECTED");
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
					Log.d("WifiCallback", "receive wifi state -> OBTAINING_IPADDR");
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
    				Log.d("WifiCallback", "receive wifi state -> DISCONNECTED");
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
	
	public void onWifiEnabled(){}
	
	public void onWifiEnabling(){}
	
	public void onWifiDisabled(){}
	
	public void onWifiDisabling(){}
	
	public void onScanResults(List<ScanResult> scanResults){}
	
	public void onNetworkConnected(WifiInfo wifiInfo){}
	
	public void onNetworkObtainingIp(WifiInfo wifiInfo){}
	
	public void onNetworkDisconnected(WifiInfo wifiInfo){}
	
	public void onTimeout(){}
	
	public void onWifiExist(){}
	
	public void onError(){}
	
	public void registerMe(){
		IntentFilter wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        isDoneForAutoUnregisterActions = false;
        isUnregistered = false;
        context.registerReceiver(this,wifiIntentFilter);
        if(timeoutForAutoUnregisterActions > 0){   //Ϊ0ʱ��������ʱ
            new Timer().schedule(new TimerTask() {
            	protected long timeCount = 0;
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				timeCount = timeCount + 100;
    				if(isDoneForAutoUnregisterActions){
    					cancel();
    				}else if(timeCount >= timeoutForAutoUnregisterActions){   //�ѳ�ʱ
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
		isDoneForAutoUnregisterActions = true;   //�ڷ�ע��ʱ��Ϊtrue��ʹ��ʱ���ܹ������˳�
		isUnregistered = true;
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
	 * ���õȴ��Զ���ע��ACTION��ִ�еĳ�ʱʱ�䣬��ʱʱ���ص�onTimeout�������Զ���ע��
	 * timeout��Ϊ0����������ʱ
	 * @param timeout
	 */
	public void setTimeoutForAutoUnregisterActions(int timeout){
		if(timeout < 0) throw new IllegalArgumentException("timeout could not be below zero.");
		this.timeoutForAutoUnregisterActions = timeout;
	}
	
}
