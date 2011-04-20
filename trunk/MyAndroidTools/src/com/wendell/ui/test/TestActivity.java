package com.wendell.ui.test;

import com.wendell.net.NetManager;
import com.wendell.ui.ToastManager;

import android.app.Activity;
import android.os.Bundle;

public class TestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ToastManager.showLong(this, String.valueOf(NetManager.isNetUseful(6000, 2)));
	}
	
}
