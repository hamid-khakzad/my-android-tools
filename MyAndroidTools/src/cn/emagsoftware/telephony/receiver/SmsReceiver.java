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

public abstract class SmsReceiver
{

    /*private Context           context                            = null;
    private BroadcastReceiver receiver                           = null;
    private boolean           autoUnregisterWhenReceive          = false;
    private boolean           isDoneForAutoUnregisterWhenReceive = false;
    private Handler           handler                            = new Handler();
    private boolean           isUnregistered                     = true;
    private boolean           isUnregisteredCompletely           = true;
    private int               curTimeout                         = -1;

    public SmsReceiver(Context ctx, SmsFilter receiveFilter)
    {
        if (ctx == null)
            throw new NullPointerException();
        context = ctx;
        if (receiveFilter == null) // 传null时默认接收所有
            receiveFilter = new SmsFilter()
            {
                @Override
                public boolean accept(SmsMessage msg)
                {
                    // TODO Auto-generated method stub
                    return true;
                }
            };
        final SmsFilter receiveFilterPoint = receiveFilter;
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
                SmsMessage[] smsMessages = new SmsMessage[messages.length];
                int smsMessagesIndex = 0;
                for (int i = 0; i < messages.length; i++)
                {
                    SmsMessage msg = SmsMessage.createFromPdu((byte[]) messages[i]);
                    if (receiveFilterPoint.accept(msg))
                    {
                        smsMessages[smsMessagesIndex] = msg;
                        smsMessagesIndex = smsMessagesIndex + 1;
                    }
                }
                SmsMessage[] returnSmsMessages = new SmsMessage[smsMessagesIndex];
                for (int i = 0; i < returnSmsMessages.length; i++)
                {
                    returnSmsMessages[i] = smsMessages[i];
                }
                if (returnSmsMessages.length > 0)
                {
                    if (autoUnregisterWhenReceive)
                    {
                        isDoneForAutoUnregisterWhenReceive = true;
                        if (!unregisterMe())
                            return;
                    }
                    SmsReceiver.this.onReceive(returnSmsMessages);
                }
            }
        };
    }

    public void onReceive(SmsMessage[] msg)
    {
    }

    public void onTimeout()
    {
    }

    public void setAutoUnregisterWhenReceive(boolean auto)
    {
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.autoUnregisterWhenReceive = auto;
    }

    *//**
     * <p>注册并指定等待超时时间，超时时将回调onTimeout方法并自动反注册 <p>若在超时之前已经反注册，则将不再计算超时
     * 
     * @param timeout 单位为毫秒，设为0将永不超时
     *//*
    public void registerMe(int timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout could not be below zero.");
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");

        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

        isDoneForAutoUnregisterWhenReceive = false;
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
                    if (isDoneForAutoUnregisterWhenReceive)
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
            isDoneForAutoUnregisterWhenReceive = true; // 置该值为true，使超时计时器能够尽快退出
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
            LogManager.logW(SmsReceiver.class, "unregister receiver failed.", e);
            return false;
        }
    }*/

}
