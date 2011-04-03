package cn.emagsoftware.sms.receiver;

import java.util.Arrays;

import cn.emagsoftware.sms.SmsUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class SmsSendCallback extends BroadcastReceiver {
	
	public static final int ACTION_SENT = 0;
	public static final int ACTION_DELIVERED = 1;
	
	protected Context context = null;
	protected int token = -1;
	protected int[] autoUnregisterActions = new int[]{};
	
	public SmsSendCallback(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
		Arrays.sort(autoUnregisterActions);
	}
	
	/**
	 * 若设为-1，将监听所有的短信发送
	 * @param token
	 */
	public void setToken(int token){
		this.token = token;
	}
	
	@Override
	public final void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		String actionStr = arg1.getAction();
		int code = getResultCode();
		int srcToken = arg1.getIntExtra("SMS_TOKEN", -1);
		String to = arg1.getStringExtra("SMS_TO");
		String text = arg1.getStringExtra("SMS_TEXT");
		if(token == -1 || token == srcToken){   //验证token
			if(actionStr.equals(SmsUtils.SMS_SENT_ACTION)){
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_SENT) > -1) unregisterMe();
				if(code == Activity.RESULT_OK){
					onSendSuccess(to,text);
				}else{
					onSendFailure(to,text);
				}
			}else if(actionStr.equals(SmsUtils.SMS_DELIVERED_ACTION)){
				if(Arrays.binarySearch(autoUnregisterActions, ACTION_DELIVERED) > -1) unregisterMe();
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
	
	public void setAutoUnregisterActions(int[] actions){
		if(actions == null) throw new NullPointerException();
		Arrays.sort(actions);
		this.autoUnregisterActions = actions;
	}
	
}
