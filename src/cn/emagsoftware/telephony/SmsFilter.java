package cn.emagsoftware.telephony;

import android.telephony.SmsMessage;

public interface SmsFilter
{

    public boolean accept(SmsMessage msg);

}
