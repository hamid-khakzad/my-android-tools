package cn.emagsoftware.sms.receiver;

import cn.emagsoftware.sms.SmsUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class SmsSendCallback extends BroadcastReceiver {
	
	public static final int ACTION_SENT = 0;
	public static final int ACTION_DELIVERED = 1;
	public static final int ACTION_SENT_OR_DELIVERED = 2;
	
	protected Context context = null;
	protected int token;
	protected boolean autoUnregisterWhenAction = false;
	protected int action;
	
	public SmsSendCallback(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
	}
	
	public void setToken(int token){
		this.token = token;
	}
	
	@Override
	public final void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		String actionStr = arg1.getAction();
		int code = getResultCode();
		int srcToken = arg1.getIntExtra("SMS_TOKEN", 0);
		String to = arg1.getStringExtra("SMS_TO");
		String text = arg1.getStringExtra("SMS_TEXT");
		if(token == srcToken){   //验证token
			if(actionStr.equals(SmsUtils.SMS_SENT_ACTION)){
				if(autoUnregisterWhenAction){   //是否自动取消注册
					if(action == ACTION_SENT || action == ACTION_SENT_OR_DELIVERED) unregisterMe();
				}
				if(code == Activity.RESULT_OK){
					onSendSuccess(to,text);
				}else{
					onSendFailure(to,text);
				}
			}else if(actionStr.equals(SmsUtils.SMS_DELIVERED_ACTION)){
				if(autoUnregisterWhenAction){   //是否自动取消注册
					if(action == ACTION_DELIVERED || action == ACTION_SENT_OR_DELIVERED) unregisterMe();
				}
				if(code == Activity.RESULT_OK){
					onDeliverSuccess(to,text);
				}else{
					onDeliverFailure(to,text);
				}
			}
		}
	}
	
	public void onDeliverSuccess(String to,String text){}
	
	public void onDeliverFailure(String to,String text){}
	
	public void onSendSuccess(String to,String text){}
	
	public void onSendFailure(String to,String text){}
	
	public void registerMe(){
		IntentFilter smsIntentFilter = new IntentFilter();
		smsIntentFilter.addAction(SmsUtils.SMS_SENT_ACTION);
		smsIntentFilter.addAction(SmsUtils.SMS_DELIVERED_ACTION);
        context.registerReceiver(this,smsIntentFilter);
	}
	
	public void unregisterMe(){
		context.unregisterReceiver(this);
	}
	
	public void setAutoUnregisterWhenAction(boolean auto,int action){
		this.autoUnregisterWhenAction = auto;
		this.action = action;
	}
	
}
