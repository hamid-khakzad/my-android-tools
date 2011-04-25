package com.wendell.ui.test;

import java.util.List;

import com.wendell.ui.ToastManager;

import cn.emagsoftware.net.wifi.WifiCallback;
import cn.emagsoftware.net.wifi.WifiUtils;
import android.app.Activity;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;

public class TestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		final WifiUtils wifiUtils = WifiUtils.getInstance(this);
		wifiUtils.setWifiEnabled(true,new WifiCallback(this) {
			@Override
			public void onWifiEnabled() {
				// TODO Auto-generated method stub
				Log.e("TestActivity", String.valueOf(wifiUtils.isWifiEnabled()));
				wifiUtils.startScan(new WifiCallback(TestActivity.this) {
					@Override
					public void onScanResults(List<ScanResult> scanResults) {
						// TODO Auto-generated method stub
						ToastManager.showLong(TestActivity.this, "onScanResults");
					}
					@Override
					public void onError() {
						// TODO Auto-generated method stub
						ToastManager.showLong(TestActivity.this, "onError");
					}
				}, 10000);
			}
		}, 0);
	}
	
}
