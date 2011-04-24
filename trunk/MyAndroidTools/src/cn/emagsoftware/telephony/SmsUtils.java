package cn.emagsoftware.telephony;

import java.util.Timer;
import java.util.TimerTask;

import cn.emagsoftware.telephony.receiver.SmsInterceptor;
import cn.emagsoftware.telephony.receiver.SmsReceiver;
import cn.emagsoftware.telephony.receiver.SmsSendCallback;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public final class SmsUtils {
	
	public static final String SMS_SENT_ACTION = "cn.emagsoftware.telephony.SMS_SENT";
	public static final String SMS_DELIVERED_ACTION = "cn.emagsoftware.telephony.SMS_DELIVERED";
	
	private static int sendMessageToken = 0;
	private static SmsManager smsManager = SmsManager.getDefault();
	
	private SmsUtils(){
	}
	
	/**
	 * <p>发送短信
	 * @param context
	 * @param to
	 * @param text
	 * @param ssc
	 */
	public static void sendMessage(Context context,String to,String text,SmsSendCallback ssc){
		sendMessageToken = sendMessageToken + 1;
		Intent sentIntent = new Intent(SMS_SENT_ACTION);
		sentIntent.putExtra("SMS_TOKEN", sendMessageToken);
		sentIntent.putExtra("SMS_TO", to);
		sentIntent.putExtra("SMS_TEXT", text);
		PendingIntent sentPI = PendingIntent.getBroadcast(context,0,sentIntent,PendingIntent.FLAG_ONE_SHOT);
		Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION);
		deliveredIntent.putExtra("SMS_TOKEN", sendMessageToken);
		deliveredIntent.putExtra("SMS_TO", to);
		deliveredIntent.putExtra("SMS_TEXT", text);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(context,0,deliveredIntent,PendingIntent.FLAG_ONE_SHOT);
		if(ssc != null){
			ssc.setToken(sendMessageToken);
			ssc.setAutoUnregisterActions(new int[]{SmsSendCallback.ACTION_SENT});
			ssc.registerMe();
		}
		smsManager.sendTextMessage(to, null, text, sentPI, deliveredPI);
	}
	
	/**
	 * <p>接收短信
	 * @param sr
	 * @param millisecond 单位为毫秒，设为0将永不超时，此时，需手工反注册
	 * @param interruptWhenReceive
	 */
	public static void receiveMessage(final SmsReceiver sr,long millisecond,boolean interruptWhenReceive){
		if(millisecond < 0) throw new IllegalArgumentException("millisecond could not be below zero.");
		sr.setAutoUnregisterWhenReceive(interruptWhenReceive);
		sr.registerMe();
		if(millisecond > 0){
			new Timer().schedule(new TimerTask(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					sr.unregisterMe();
				}
			}, millisecond);
		}
	}
	
	/**
	 * <p>拦截短信
	 * @param si
	 * @param millisecond 单位为毫秒，设为0将永不超时，此时，需手工反注册
	 */
	public static void interceptMessage(final SmsInterceptor si,long millisecond){
		if(millisecond < 0) throw new IllegalArgumentException("millisecond could not be below zero.");
		si.registerMe(1000);
		if(millisecond > 0){
			new Timer().schedule(new TimerTask(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					si.unregisterMe();
				}
			}, millisecond);
		}
	}
	
}
