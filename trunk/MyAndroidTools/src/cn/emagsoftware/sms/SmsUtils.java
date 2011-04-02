package cn.emagsoftware.sms;

import java.util.Timer;
import java.util.TimerTask;

import cn.emagsoftware.sms.receiver.SmsInterceptor;
import cn.emagsoftware.sms.receiver.SmsReceiver;
import cn.emagsoftware.sms.receiver.SmsSendCallback;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public final class SmsUtils {
	
	public static final String SMS_SENT_ACTION = "cn.emagsoftware.sms.SENT";
	public static final String SMS_DELIVERED_ACTION = "cn.emagsoftware.sms.DELIVERED";
	
	private static int sendMessageToken = 0;
	private static SmsManager smsManager = SmsManager.getDefault();
	
	private SmsUtils(){
	}
	
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
			ssc.setAutoUnregisterWhenAction(true, SmsSendCallback.ACTION_SENT);
			ssc.registerMe();
		}
		smsManager.sendTextMessage(to, null, text, sentPI, deliveredPI);
	}
	
	public static void receiveMessage(final SmsReceiver sr,long millisecond,boolean interruptWhenReceive){
		sr.setAutoUnregisterWhenReceive(interruptWhenReceive);
		sr.registerMe();
		new Timer().schedule(new TimerTask(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				sr.unregisterMe();
			}
		}, millisecond);
	}
	
	public static void interceptMessage(final SmsInterceptor si,long millisecond){
		si.registerMe(1000);
		new Timer().schedule(new TimerTask(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				si.unregisterMe();
			}
		}, millisecond);
	}
	
}
