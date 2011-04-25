package com.wendell.ui.test;

import com.wendell.ui.ToastManager;

import cn.emagsoftware.telephony.SmsUtils;
import cn.emagsoftware.telephony.receiver.SmsInterceptor;
import android.app.Activity;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class TestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SmsUtils.interceptMessage(new SmsInterceptor(this,null) {
			@Override
			public void onIntercept(SmsMessage[] msg) {
				// TODO Auto-generated method stub
				ToastManager.showLong(TestActivity.this, "���ص�����:["+msg[0].getMessageBody()+"]");
			}
			@Override
			public void onTimeout() {
				// TODO Auto-generated method stub
				ToastManager.showLong(TestActivity.this, "���س�ʱ");
			}
		}, true, 0);
	}
	
}
