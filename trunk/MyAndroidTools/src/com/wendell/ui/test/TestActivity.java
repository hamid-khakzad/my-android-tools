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
		WifiUtils.getInstance(this).checkWifiExist(new WifiCallback(this) {
			@Override
			public void onWifiExist() {
				// TODO Auto-generated method stub
				ToastManager.showLong(TestActivity.this, "wifi exist.");
			}
			@Override
			public void onTimeout() {
				// TODO Auto-generated method stub
				ToastManager.showLong(TestActivity.this, "time out");
			}
			@Override
			public void onError() {
				// TODO Auto-generated method stub
				ToastManager.showLong(TestActivity.this, "wifi not exist.");
			}
		}, 10000);
	}
	
}
