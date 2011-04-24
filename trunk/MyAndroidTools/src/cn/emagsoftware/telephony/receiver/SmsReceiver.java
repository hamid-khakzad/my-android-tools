package cn.emagsoftware.telephony.receiver;

import cn.emagsoftware.telephony.SmsFilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public abstract class SmsReceiver extends BroadcastReceiver {
	
	protected Context context = null;
	protected SmsFilter receiveFilter = new SmsFilter() {
		@Override
		public boolean accept(SmsMessage msg) {
			// TODO Auto-generated method stub
			return true;
		}
	};
	protected boolean autoUnregisterWhenReceive = false;
	
	public SmsReceiver(Context context,SmsFilter receiveFilter){
		if(context == null) throw new NullPointerException();
		this.context = context;
		if(receiveFilter != null) this.receiveFilter = receiveFilter;   //传null时默认接收所有
	}
	
	@Override
	public final void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Bundle bundle = arg1.getExtras();
		Object[] messages = (Object[])bundle.get("pdus");
		SmsMessage[] smsMessages = new SmsMessage[messages.length];
		int smsMessagesIndex = 0;
		for (int i = 0; i < messages.length; i++) {
			SmsMessage msg = SmsMessage.createFromPdu((byte[])messages[i]);
			if(receiveFilter.accept(msg)) {
				smsMessages[smsMessagesIndex] = msg;
				smsMessagesIndex = smsMessagesIndex + 1;
			}
		}
		SmsMessage[] returnSmsMessages = new SmsMessage[smsMessagesIndex];
		for(int i = 0;i < returnSmsMessages.length;i++){
			returnSmsMessages[i] = smsMessages[i];
		}
		if(returnSmsMessages.length > 0){
			if(autoUnregisterWhenReceive) {
				if(!unregisterMe()) return;
			}
			onReceive(returnSmsMessages);
		}
	}
	
	public void onReceive(SmsMessage[] msg){}
	
	public void registerMe(){
		IntentFilter smsIntentFilter = new IntentFilter();
		smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        context.registerReceiver(this,smsIntentFilter);
	}
	
	public boolean unregisterMe(){
		try{
			context.unregisterReceiver(this);
			return true;
		}catch(IllegalArgumentException e){
			//重复反注册会抛出该异常，如通过代码注册的receiver在当前activity销毁时会自动反注册，若再反注册，即会抛出该异常
			Log.e("SmsReceiver", "unregister receiver failed.", e);
			return false;
		}
	}
	
	public void setAutoUnregisterWhenReceive(boolean auto){
		this.autoUnregisterWhenReceive = auto;
	}
	
}
