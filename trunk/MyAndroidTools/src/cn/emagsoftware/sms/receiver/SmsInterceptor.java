package cn.emagsoftware.sms.receiver;

import cn.emagsoftware.sms.SmsFilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;

public abstract class SmsInterceptor extends BroadcastReceiver {
	
	protected Context context = null;
	protected SmsFilter interceptFilter = new SmsFilter() {
		@Override
		public boolean accept(SmsMessage msg) {
			// TODO Auto-generated method stub
			return true;
		}
	};
	
	public SmsInterceptor(Context context,SmsFilter interceptFilter){
		if(context == null) throw new NullPointerException();
		this.context = context;
		if(interceptFilter != null) this.interceptFilter = interceptFilter;   //传null时默认拦截所有
	}
	
	@Override
	public final void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Bundle bundle = arg1.getExtras();
		Object[] messages = (Object[])bundle.get("pdus");
		SmsMessage[] smsMessages = new SmsMessage[messages.length];
		boolean isIntercept = false;
		for (int i = 0; i < messages.length; i++) {
			smsMessages[i] = SmsMessage.createFromPdu((byte[])messages[i]);
			if(!isIntercept && interceptFilter.accept(smsMessages[i])) {
				isIntercept = true;
				this.abortBroadcast();
			}
		}
		if(isIntercept) onIntercept(smsMessages);
	}
	
	public void onIntercept(SmsMessage[] msg){}
	
	public void registerMe(int priority){
		IntentFilter smsIntentFilter = new IntentFilter();
		smsIntentFilter.setPriority(priority);
		smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        context.registerReceiver(this,smsIntentFilter);
	}
	
	public void unregisterMe(){
		context.unregisterReceiver(this);
	}
	
}
