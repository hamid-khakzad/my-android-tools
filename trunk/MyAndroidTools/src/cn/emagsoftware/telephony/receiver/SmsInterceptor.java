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

public abstract class SmsInterceptor extends BroadcastReceiver {
	
	protected Context context = null;
	protected SmsFilter interceptFilter = new SmsFilter() {
		@Override
		public boolean accept(SmsMessage msg) {
			// TODO Auto-generated method stub
			return true;
		}
	};
	protected Handler handler = new Handler(Looper.getMainLooper());
	protected boolean autoUnregisterWhenIntercept = false;
	protected int timeout = 0;
	protected boolean isDoneForAutoUnregisterWhenIntercept = false;
	protected boolean isUnregistered = true;
	
	public SmsInterceptor(Context context,SmsFilter interceptFilter){
		if(context == null) throw new NullPointerException();
		this.context = context;
		if(interceptFilter != null) this.interceptFilter = interceptFilter;   //传null时默认拦截所有
	}
	
	@Override
	public final void onReceive(Context arg0, final Intent arg1) {
		// TODO Auto-generated method stub
		if(isUnregistered) return;    //如果已经反注册，将直接返回
		Bundle bundle = arg1.getExtras();
		Object[] messages = (Object[])bundle.get("pdus");
		final SmsMessage[] smsMessages = new SmsMessage[messages.length];
		boolean isIntercept = false;
		for (int i = 0; i < messages.length; i++) {
			smsMessages[i] = SmsMessage.createFromPdu((byte[])messages[i]);
			if(!isIntercept && interceptFilter.accept(smsMessages[i])) {
				isIntercept = true;
				this.abortBroadcast();
			}
		}
		if(isIntercept) {
			//推迟处理，使abortBroadcast能够生效
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					dealInterceptDelay(arg1,smsMessages);
				}
			});
		}
	}
	
	protected final void dealInterceptDelay(Intent smsIntent,SmsMessage[] smsMessages){
		if(isUnregistered){
			Log.i("SmsInterceptor", "current interceptor has been invalid,resend sms broadcast what has been intercepted already.");
			context.sendBroadcast(smsIntent);    //重新发送短信，防止短信丢失
			return;
		}
		if(autoUnregisterWhenIntercept){
			isDoneForAutoUnregisterWhenIntercept = true;
			if(!unregisterMe()) {
				Log.i("SmsInterceptor", "current interceptor has been invalid,resend sms broadcast what has been intercepted already.");
				context.sendBroadcast(smsIntent);    //重新发送短信，防止短信丢失
				return;
			}
		}
		onIntercept(smsMessages);
	}
	
	public void onIntercept(SmsMessage[] msg){}
	
	public void onTimeout(){}
	
	public void registerMe(int priority){
		IntentFilter smsIntentFilter = new IntentFilter();
		smsIntentFilter.setPriority(priority);
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
    				if(isDoneForAutoUnregisterWhenIntercept){
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
			Log.e("SmsInterceptor", "unregister receiver failed.", e);
			return false;
		}
	}
	
	public void setAutoUnregisterWhenIntercept(boolean auto){
		this.autoUnregisterWhenIntercept = auto;
	}
	
	/**
	 * <p>设置拦截短信的超时时间，超时时将回调onTimeout方法并自动反注册
	 * <p>若设置了拦截到短信时自动反注册，在拦截到短信时，超时计时器将随之退出而不再计时
	 * @param timeout 单位为毫秒，设为0将永不超时
	 */
	public void setTimeout(int timeout){
		if(timeout < 0) throw new IllegalArgumentException("timeout could not be below zero.");
		this.timeout = timeout;
	}
	
}
