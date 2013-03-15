package cn.emagsoftware.telephony.receiver;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import cn.emagsoftware.telephony.SmsFilter;
import cn.emagsoftware.util.LogManager;

public abstract class SmsInterceptor
{

    private Context           context                              = null;
    private BroadcastReceiver receiver                             = null;
    private boolean           autoUnregisterWhenIntercept          = false;
    private boolean           isDoneForAutoUnregisterWhenIntercept = false;
    private Handler           handler                              = new Handler();
    private boolean           isUnregistered                       = true;
    private boolean           isUnregisteredCompletely             = true;
    private int               curTimeout                           = -1;

    public SmsInterceptor(Context ctx, SmsFilter interceptFilter)
    {
        if (ctx == null)
            throw new NullPointerException();
        context = ctx;
        if (interceptFilter == null) // ��nullʱĬ����������
            interceptFilter = new SmsFilter()
            {
                @Override
                public boolean accept(SmsMessage msg)
                {
                    // TODO Auto-generated method stub
                    return true;
                }
            };
        final SmsFilter interceptFilterPoint = interceptFilter;
        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, final Intent intent)
            {
                // TODO Auto-generated method stub
                if (isUnregistered)
                    return; // ����Ѿ���ע�ᣬ��ֱ�ӷ���
                Bundle bundle = intent.getExtras();
                Object[] messages = (Object[]) bundle.get("pdus");
                final SmsMessage[] smsMessages = new SmsMessage[messages.length];
                boolean isIntercept = false;
                for (int i = 0; i < messages.length; i++)
                {
                    smsMessages[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
                    if (!isIntercept && interceptFilterPoint.accept(smsMessages[i]))
                    {
                        isIntercept = true;
                        this.abortBroadcast();
                    }
                }
                if (isIntercept)
                {
                    // �Ƴٴ���ʹabortBroadcast�ܹ���Ч
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            dealInterceptDelay(intent, smsMessages);
                        }
                    });
                }
            }
        };
    }

    private void dealInterceptDelay(Intent smsIntent, SmsMessage[] smsMessages)
    {
        if (isUnregistered)
        {
            LogManager.logI(SmsInterceptor.class, "current interceptor has been invalid,resend sms broadcast what has been intercepted already.");
            context.sendBroadcast(smsIntent); // ���·��Ͷ��ţ���ֹ���Ŷ�ʧ
            return;
        }
        if (autoUnregisterWhenIntercept)
        {
            isDoneForAutoUnregisterWhenIntercept = true;
            if (!unregisterMe())
            {
                LogManager.logI(SmsInterceptor.class, "current interceptor has been invalid,resend sms broadcast what has been intercepted already.");
                context.sendBroadcast(smsIntent); // ���·��Ͷ��ţ���ֹ���Ŷ�ʧ
                return;
            }
        }
        onIntercept(smsMessages);
    }

    public void onIntercept(SmsMessage[] msg)
    {
    }

    public void onTimeout()
    {
    }

    public void setAutoUnregisterWhenIntercept(boolean auto)
    {
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.autoUnregisterWhenIntercept = auto;
    }

    /**
     * <p>ע�Ტָ�����ȼ��͵ȴ���ʱʱ�䣬��ʱʱ���ص�onTimeout�������Զ���ע�� <p>���ڳ�ʱ֮ǰ�Ѿ���ע�ᣬ�򽫲��ټ��㳬ʱ
     * 
     * @param priority
     * @param timeout ��λΪ���룬��Ϊ0��������ʱ
     */
    public void registerMe(int priority, int timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout could not be below zero.");
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.setPriority(priority);
        smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        isDoneForAutoUnregisterWhenIntercept = false;
        isUnregistered = false;
        isUnregisteredCompletely = false;
        context.registerReceiver(receiver, smsIntentFilter, null, handler);
        curTimeout = timeout;
        if (curTimeout > 0)
        { // Ϊ0ʱ��������ʱ
            new Timer().schedule(new TimerTask()
            {
                protected long timeCount = 0;

                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    timeCount = timeCount + 100;
                    if (isDoneForAutoUnregisterWhenIntercept)
                    {
                        cancel();
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // TODO Auto-generated method stub
                                isUnregisteredCompletely = true;
                            }
                        });
                    } else if (timeCount >= curTimeout)
                    { // �ѳ�ʱ
                        cancel();
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // TODO Auto-generated method stub
                                if (unregisterMe())
                                {
                                    onTimeout();
                                }
                                isUnregisteredCompletely = true;
                            }
                        });
                    }
                }
            }, 100, 100);
        }
    }

    public boolean unregisterMe()
    {
        if (curTimeout > 0)
            isDoneForAutoUnregisterWhenIntercept = true; // �ø�ֵΪtrue��ʹ��ʱ��ʱ���ܹ������˳�
        else
            isUnregisteredCompletely = true;
        isUnregistered = true;
        try
        {
            context.unregisterReceiver(receiver);
            return true;
        } catch (IllegalArgumentException e)
        {
            // �ظ���ע����׳����쳣����ͨ������ע���receiver�ڵ�ǰactivity����ʱ���Զ���ע�ᣬ���ٷ�ע�ᣬ�����׳����쳣
            LogManager.logW(SmsInterceptor.class, "unregister receiver failed.", e);
            return false;
        }
    }

}
