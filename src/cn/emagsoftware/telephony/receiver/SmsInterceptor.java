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
        if (interceptFilter == null) // 传null时默认拦截所有
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
                    return; // 如果已经反注册，将直接返回
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
                    // 推迟处理，使abortBroadcast能够生效
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
            context.sendBroadcast(smsIntent); // 重新发送短信，防止短信丢失
            return;
        }
        if (autoUnregisterWhenIntercept)
        {
            isDoneForAutoUnregisterWhenIntercept = true;
            if (!unregisterMe())
            {
                LogManager.logI(SmsInterceptor.class, "current interceptor has been invalid,resend sms broadcast what has been intercepted already.");
                context.sendBroadcast(smsIntent); // 重新发送短信，防止短信丢失
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
     * <p>注册并指定优先级和等待超时时间，超时时将回调onTimeout方法并自动反注册 <p>若在超时之前已经反注册，则将不再计算超时
     * 
     * @param priority
     * @param timeout 单位为毫秒，设为0将永不超时
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
        { // 为0时将永不超时
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
                    { // 已超时
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
            isDoneForAutoUnregisterWhenIntercept = true; // 置该值为true，使超时计时器能够尽快退出
        else
            isUnregisteredCompletely = true;
        isUnregistered = true;
        try
        {
            context.unregisterReceiver(receiver);
            return true;
        } catch (IllegalArgumentException e)
        {
            // 重复反注册会抛出该异常，如通过代码注册的receiver在当前activity销毁时会自动反注册，若再反注册，即会抛出该异常
            LogManager.logW(SmsInterceptor.class, "unregister receiver failed.", e);
            return false;
        }
    }

}
