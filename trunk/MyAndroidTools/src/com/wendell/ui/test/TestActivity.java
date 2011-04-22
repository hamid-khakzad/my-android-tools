package com.wendell.ui.test;

import com.wendell.ui.ToastManager;

import cn.emagsoftware.net.wifi.WifiCallback;
import cn.emagsoftware.net.wifi.WifiUtils;
import android.app.Activity;
import android.os.Bundle;

public class TestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		WifiUtils.getInstance(this).setWifiEnabled(true, new WifiCallback(this) {
			@Override
			public void onWifiEnabled() {
				// TODO Auto-generated method stub
				ToastManager.showLong(TestActivity.this, "onWifiEnabled");
			}
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				ToastManager.showLong(TestActivity.this, "onError");
			}
			@Override
			public void onTimeout() {
				// TODO Auto-generated method stub
				ToastManager.showLong(TestActivity.this, "onTimeout");
			}
		}, 20000);
	}
	
}
