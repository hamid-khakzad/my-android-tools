package cn.emagsoftware.telephony;

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
	 * <p>���Ͷ���
	 * @param context
	 * @param to
	 * @param text
	 * @param ssc
	 * @param timeout ��λΪ���룬��Ϊ0��������ʱ
	 */
	public static void sendMessage(Context context,String to,String text,SmsSendCallback ssc,int timeout){
		sendMessageToken = sendMessageToken + 1;
		Intent sentIntent = new Intent(SMS_SENT_ACTION);
		sentIntent.putExtra("SMS_TOKEN", sendMessageToken);
		sentIntent.putExtra("SMS_TO", to);
		sentIntent.putExtra("SMS_TEXT", text);
		PendingIntent sentPI = PendingIntent.getBroadcast(context,0,sentIntent,PendingIntent.FLAG_ONE_SHOT);
		/*Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION);
		deliveredIntent.putExtra("SMS_TOKEN", sendMessageToken);
		deliveredIntent.putExtra("SMS_TO", to);
		deliveredIntent.putExtra("SMS_TEXT", text);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(context,0,deliveredIntent,PendingIntent.FLAG_ONE_SHOT);*/
		if(ssc != null){
			ssc.setToken(sendMessageToken);
			ssc.setAutoUnregisterActions(new int[]{SmsSendCallback.ACTION_SENT});
			ssc.setTimeout(timeout);
			ssc.registerMe();
		}
		//��ʱ������deliveryIntent�¼��Ľ��գ���Ϊ����ĳЩ�����ϻᵯ����ִ��Ϣ
		smsManager.sendTextMessage(to, null, text, sentPI, null);
	}
	
	/**
	 * <p>���ն���
	 * @param sr
	 * @param interruptWhenReceive
	 * @param timeout ��λΪ���룬��Ϊ0��������ʱ����ʱ������Ҫ�ֹ���ע��
	 */
	public static void receiveMessage(SmsReceiver sr,boolean interruptWhenReceive,int timeout){
		if(sr == null) throw new NullPointerException();
		sr.setAutoUnregisterWhenReceive(interruptWhenReceive);
		sr.setTimeout(timeout);
		sr.registerMe();
	}
	
	/**
	 * <p>���ض���
	 * @param si
	 * @param interruptWhenIntercept
	 * @param timeout ��λΪ���룬��Ϊ0��������ʱ����ʱ������Ҫ�ֹ���ע��
	 */
	public static void interceptMessage(SmsInterceptor si,boolean interruptWhenIntercept,int timeout){
		if(si == null) throw new NullPointerException();
		si.setAutoUnregisterWhenIntercept(interruptWhenIntercept);
		si.setTimeout(timeout);
		si.registerMe(1000);
	}
	
}
