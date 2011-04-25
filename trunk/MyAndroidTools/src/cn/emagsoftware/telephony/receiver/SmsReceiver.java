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
		if(receiveFilter != null) this.receiveFilter = receiveFilter;   //��nullʱĬ�Ͻ�������
	}
	
	@Override
	public final void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		if(isUnregistered) return;    //����Ѿ���ע�ᣬ��ֱ�ӷ���
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
        if(timeout > 0){   //Ϊ0ʱ��������ʱ
            new Timer().schedule(new TimerTask() {
            	protected long timeCount = 0;
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				timeCount = timeCount + 100;
    				if(isDoneForAutoUnregisterWhenReceive){
    					cancel();
    				}else if(timeCount >= timeout){   //�ѳ�ʱ
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
			//�ظ���ע����׳����쳣����ͨ������ע���receiver�ڵ�ǰactivity����ʱ���Զ���ע�ᣬ���ٷ�ע�ᣬ�����׳����쳣
			Log.e("SmsReceiver", "unregister receiver failed.", e);
			return false;
		}
	}
	
	public void setAutoUnregisterWhenReceive(boolean auto){
		this.autoUnregisterWhenReceive = auto;
	}
	
	/**
	 * <p>���ý��ն��ŵĳ�ʱʱ�䣬��ʱʱ���ص�onTimeout�������Զ���ע��
	 * <p>�������˽��յ�����ʱ�Զ���ע�ᣬ�ڽ��յ�����ʱ����ʱ��ʱ������֮�˳������ټ�ʱ
	 * @param timeout ��λΪ���룬��Ϊ0��������ʱ
	 */
	public void setTimeout(int timeout){
		if(timeout < 0) throw new IllegalArgumentException("timeout could not be below zero.");
		this.timeout = timeout;
	}
	
}
