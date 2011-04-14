package com.wendell.ui.test;

import com.wendell.ui.dialog.DialogManager;

import android.app.Activity;
import android.os.Bundle;

public class TestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		DialogManager.showAlertDialog(this, "提示", "提示内容", new String[]{"是","否","取消"}, null, true, true);
	}
	
}
