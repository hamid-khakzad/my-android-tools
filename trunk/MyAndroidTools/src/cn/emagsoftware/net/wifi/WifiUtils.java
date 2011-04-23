package cn.emagsoftware.net.wifi;

import java.util.List;

import cn.emagsoftware.net.wifi.support.Wifi;

import com.wendell.net.NetManager;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public final class WifiUtils {
	
	private Context context = null;
	private WifiManager wifiManager = null;
	private WifiLock wifiLock = null;
	private Handler handler = new Handler(Looper.getMainLooper());
	
	public static WifiUtils getInstance(Context context){
		return new WifiUtils(context);
	}
	
	private WifiUtils(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
		wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		wifiLock = wifiManager.createWifiLock("cn.emagsoftware.net.wifi.WifiUtils");
	}
	
	public boolean isWifiEnabled(){
		return wifiManager.isWifiEnabled();
	}
	
	public boolean isWifiConnected(){
		NetworkInfo wifiNetworkInfo = NetManager.getWifiNetworkInfo(context);
		if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) return true;
		return false;
	}
	
	public boolean isWifiUseful(int timeout,int tryTimes){
		return isWifiConnected() && NetManager.isNetUseful(timeout,tryTimes);
	}
	
	public WifiInfo getConnectionInfo(){
		return wifiManager.getConnectionInfo();
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
	
	/**
	 * @param callback
	 * @param timeout 单位为毫秒，设为0将永不超时
	 */
	public void checkWifiExist(final WifiCallback callback,final int timeout){
		if(callback == null) return;
		if(isWifiEnabled()){
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					callback.onWifiExist();
				}
			});
			return;
		}
		setWifiEnabled(true, new WifiCallback(context) {
			@Override
			public void onWifiEnabled() {
				// TODO Auto-generated method stub
				Log.d("WifiUtils", "revert to previous wifi state...");
				setWifiEnabled(false, new WifiCallback(context) {
					@Override
					public void onWifiDisabled() {
						// TODO Auto-generated method stub
						Log.d("WifiUtils", "revert to previous wifi state successfully.");
						callback.onWifiExist();
					}
					@Override
					public void onTimeout() {
						// TODO Auto-generated method stub
						Log.d("WifiUtils", "revert to previous wifi state time out.");
						callback.onWifiExist();
					}
					@Override
					public void onError() {
						// TODO Auto-generated method stub
						Log.d("WifiUtils", "revert to previous wifi state unsuccessfully.");
						callback.onWifiExist();
					}
				}, timeout);
			}
			@Override
			public void onTimeout() {
				// TODO Auto-generated method stub
				Log.d("WifiUtils", "revert to previous wifi state...");
				setWifiEnabled(false, new WifiCallback(context) {
					@Override
					public void onWifiDisabled() {
						// TODO Auto-generated method stub
						Log.d("WifiUtils", "revert to previous wifi state successfully.");
						callback.onTimeout();
					}
					@Override
					public void onTimeout() {
						// TODO Auto-generated method stub
						Log.d("WifiUtils", "revert to previous wifi state time out.");
						callback.onTimeout();
					}
					@Override
					public void onError() {
						// TODO Auto-generated method stub
						Log.d("WifiUtils", "revert to previous wifi state unsuccessfully.");
						callback.onTimeout();
					}
				}, timeout);
			}
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				callback.onError();
			}
		}, timeout);
	}
	
	/**
	 * @param enabled
	 * @param callback
	 * @param timeout 单位为毫秒，设为0将永不超时
	 */
	public void setWifiEnabled(boolean enabled,final WifiCallback callback,int timeout){
		if(!isWifiEnabled() && !enabled){    //如果Wifi不存在或已关闭时执行关闭Wifi，在开机第一次调用将不会广播到Receiver，所以这里统一在外部回调
			if(callback != null){
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						callback.onWifiDisabled();    //尽管如果Wifi不存在时，应回调onError，但判断Wifi是否存在比较耗时，所以这里统一回调onWifiDisabled
					}
				});
			}
			return;
		}
		if(callback != null){
			if(enabled) callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_WIFI_ENABLED,WifiCallback.ACTION_ERROR});
			else callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_WIFI_DISABLED,WifiCallback.ACTION_ERROR});
			callback.setTimeoutForAutoUnregisterActions(timeout);
			callback.registerMe();
		}
		boolean circs = wifiManager.setWifiEnabled(enabled);
		if(!circs) if(callback != null) {
			callback.unregisterMe();
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					callback.onError();
				}
			});
		}
	}
	
	/**
	 * @param callback
	 * @param timeout 单位为毫秒，设为0将永不超时
	 */
	public void startScan(final WifiCallback callback,int timeout){
		if(!isWifiEnabled()) {    //如果Wifi不存在或已关闭
			if(callback != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						callback.onError();
					}
				});
			}
			return;
		}
		if(callback != null){
			callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_ERROR,WifiCallback.ACTION_SCAN_RESULTS});
			callback.setTimeoutForAutoUnregisterActions(timeout);
			callback.registerMe();
		}
		boolean circs = wifiManager.startScan();
		if(!circs) if(callback != null){
			callback.unregisterMe();
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					callback.onError();
				}
			});
		}
	}
	
	/**
	 * @param wc
	 * @param callback
	 * @param timeout 单位为毫秒，设为0将永不超时
	 */
	public void connect(WifiConfiguration wc,final WifiCallback callback,int timeout){
		if(!isWifiEnabled()) {    //如果Wifi不存在或已关闭
			if(callback != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						callback.onError();
					}
				});
			}
			return;
		}
		if(callback != null){
			callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_ERROR,WifiCallback.ACTION_NETWORK_CONNECTED,WifiCallback.ACTION_NETWORK_DISCONNECTED});
			callback.setTimeoutForAutoUnregisterActions(timeout);
			callback.registerMe();
		}
		boolean circs = Wifi.connectToConfiguredNetwork(context, wifiManager, wc, true);
		if(!circs) if(callback != null){
			callback.unregisterMe();
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					callback.onError();
				}
			});
		}
	}
	
	/**
	 * @param sr
	 * @param password
	 * @param callback
	 * @param timeout 单位为毫秒，设为0将永不超时
	 */
	public void connect(ScanResult sr,String password,final WifiCallback callback,int timeout){
		if(!isWifiEnabled()) {    //如果Wifi不存在或已关闭
			if(callback != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						callback.onError();
					}
				});
			}
			return;
		}
		WifiConfiguration old = getConfiguration(sr);
		if(old != null){
			String security = getScanResultSecurity(sr);
			Wifi.setupSecurity(old, security, password);
			if(!wifiManager.saveConfiguration()) {
				if(callback != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							callback.onError();
						}
					});
				}
				return;
			}
		}
		if(callback != null){
			callback.setAutoUnregisterActions(new int[]{WifiCallback.ACTION_ERROR,WifiCallback.ACTION_NETWORK_CONNECTED,WifiCallback.ACTION_NETWORK_DISCONNECTED});
			callback.setTimeoutForAutoUnregisterActions(timeout);
			callback.registerMe();
		}
		boolean circs;
		if(old != null) circs = Wifi.connectToConfiguredNetwork(context, wifiManager, old, true);
		else circs = Wifi.connectToNewNetwork(context, wifiManager, sr, password, Integer.MAX_VALUE);
		if(!circs) if(callback != null) {
			callback.unregisterMe();
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					callback.onError();
				}
			});
		}
	}
	
}
