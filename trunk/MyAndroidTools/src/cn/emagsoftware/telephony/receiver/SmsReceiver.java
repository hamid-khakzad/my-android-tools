package cn.emagsoftware.telephony.receiver;

import java.util.Timer;
import java.util.TimerTask;

import cn.emagsoftware.telephony.SmsFilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
	protected Handler handler = new Handler(Looper.getMainLooper());
	protected boolean autoUnregisterWhenReceive = false;
	protected int timeout = 0;
	protected boolean isDoneForAutoUnregisterWhenReceive = false;
	protected boolean isUnregistered = true;
	
	public SmsReceiver(Context context,SmsFilter receiveFilter){
		if(context == null) throw new NullPointerException();
		this.context = context;
		if(receiveFilter != null) this.receiveFilter = receiveFilter;   //传null时默认接收所有
	}
	
	@Override
	public final void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		if(isUnregistered) return;    //如果已经反注册，将直接返回
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
				isDoneForAutoUnregisterWhenReceive = true;
				if(!unregisterMe()) return;
			}
			onReceive(returnSmsMessages);
		}
	}
	
	public void onReceive(SmsMessage[] msg){}
	
	public void onTimeout(){}
	
	public void registerMe(){
		IntentFilter smsIntentFilter = new IntentFilter();
		smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		isUnregistered = false;
        context.registerReceiver(this,smsIntentFilter);
        if(timeout > 0){   //为0时将永不超时
            new Timer().schedule(new TimerTask() {
            	protected long timeCount = 0;
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				timeCount = timeCount + 100;
    				if(isDoneForAutoUnregisterWhenReceive){
    					cancel();
    				}else if(timeCount >= timeout){   //已超时
    					cancel();
    					if(unregisterMe()){
    						handler.post(new Runnable() {
    							@Override
    							public void run() {
    								// TODO Auto-generated method stub
    								onTimeout();
    							}
    						});
    					}
    				}
    			}
    		},100,100);
        }
	}
	
	public boolean unregisterMe(){
		isUnregistered = true;
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
	
	/**
	 * <p>设置接收短信的超时时间，超时时将回调onTimeout方法并自动反注册
	 * <p>若设置了接收到短信时自动反注册，在接收到短信时，超时计时器将随之退出而不再计时
	 * @param timeout 单位为毫秒，设为0将永不超时
	 */
	public void setTimeout(int timeout){
		if(timeout < 0) throw new IllegalArgumentException("timeout could not be below zero.");
		this.timeout = timeout;
	}
	
}
