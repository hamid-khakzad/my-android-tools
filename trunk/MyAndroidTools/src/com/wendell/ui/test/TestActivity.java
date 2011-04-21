package com.wendell.ui.test;

import com.wendell.ui.ToastManager;

import cn.emagsoftware.net.wifi.WifiUtils;
import android.app.Activity;
import android.os.Bundle;

public class TestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ToastManager.showLong(this, String.valueOf(WifiUtils.getInstance(this).isWifiExist()));
	}
	
}
