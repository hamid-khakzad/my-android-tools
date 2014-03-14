package cn.emagsoftware.telephony.receiver;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import cn.emagsoftware.telephony.SmsUtils;
import cn.emagsoftware.util.LogManager;

public abstract class SmsSendCallback
{

    public static final int   ACTION_SENT                    = 0;
    public static final int   ACTION_DELIVERED               = 1;

    private Context           context                        = null;
    private BroadcastReceiver receiver                       = null;
    private int               token                          = -1;
    private int[]             autoUnregisterActions          = new int[] {};
    private boolean           isDoneForAutoUnregisterActions = false;
    private Handler           handler                        = new Handler();
    private boolean           isUnregistered                 = true;
    private boolean           isUnregisteredCompletely       = true;
    private int               curTimeout                     = -1;

    public SmsSendCallback(Context ctx)
    {
        if (ctx == null)
            throw new NullPointerException();
        context = ctx;
        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (isUnregistered)
                    return; // 如果已经反注册，将直接返回
                String actionStr = intent.getAction();
                int code = getResultCode();
                int srcToken = intent.getIntExtra("SMS_TOKEN", -1);
                if (token == -1 || token == srcToken)
                { // 验证token
                    if (actionStr.equals(SmsUtils.SMS_SENT_ACTION))
                    {
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_SENT) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        if (code == Activity.RESULT_OK)
                        {
                            onSendSuccess();
                        } else
                        {
                            onSendFailure();
                        }
                    } else if (actionStr.equals(SmsUtils.SMS_DELIVERED_ACTION))
                    {
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_DELIVERED) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        if (code == Activity.RESULT_OK)
                        {
                            onDeliverSuccess();
                        } else
                        {
                            onDeliverFailure();
                        }
                    }
                }
            }
        };
        Arrays.sort(autoUnregisterActions);
    }

    public void onDeliverSuccess()
    {
    }

    public void onDeliverFailure()
    {
    }

    public void onSendSuccess()
    {
    }

    public void onSendFailure()
    {
    }

    public void onTimeout()
    {
    }

    /**
     * 若设为-1，将监听所有的短信发送
     * 
     * @param token
     */
    public void setToken(int token)
    {
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.token = token;
    }

    public void setAutoUnregisterActions(int[] actions)
    {
        if (actions == null)
            throw new NullPointerException();
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.autoUnregisterActions = actions.clone();
        Arrays.sort(autoUnregisterActions);
    }

    /**
     * <p>注册并指定等待超时时间，超时时将回调onTimeout方法并自动反注册 <p>若在超时之前已经反注册，则将不再计算超时
     * 
     * @param timeout 单位为毫秒，设为0将永不超时
     */
    public void registerMe(int timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout could not be below zero.");
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.addAction(SmsUtils.SMS_SENT_ACTION);
        smsIntentFilter.addAction(SmsUtils.SMS_DELIVERED_ACTION);
        isDoneForAutoUnregisterActions = false;
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
                    if (isDoneForAutoUnregisterActions)
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
            isDoneForAutoUnregisterActions = true; // 置该值为true，使超时计时器能够尽快退出
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
            LogManager.logW(SmsSendCallback.class, "unregister receiver failed.", e);
            return false;
        }
    }

}
