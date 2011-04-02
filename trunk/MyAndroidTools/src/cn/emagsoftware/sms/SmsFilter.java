package cn.emagsoftware.sms;

import android.telephony.SmsMessage;

public interface SmsFilter {
	
	public boolean accept(SmsMessage msg);
	
}
