package cn.emagsoftware.telephony.receiver;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import cn.emagsoftware.telephony.SmsUtils;
import cn.emagsoftware.util.LogManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

public abstract class SmsSendCallback extends BroadcastReceiver
{

    public static final int ACTION_SENT                    = 0;
    public static final int ACTION_DELIVERED               = 1;

    protected Context       context                        = null;
    protected Handler       handler                        = new Handler(Looper.getMainLooper());
    protected int           token                          = -1;
    protected int[]         autoUnregisterActions          = new int[] {};
    protected int           timeout                        = 0;
    protected boolean       isDoneForAutoUnregisterActions = false;
    protected boolean       isUnregistered                 = true;

    public SmsSendCallback(Context context)
    {
        if (context == null)
            throw new NullPointerException();
        this.context = context;
        Arrays.sort(autoUnregisterActions);
    }

    /**
     * 若设为-1，将监听所有的短信发送
     * 
     * @param token
     */
    public void setToken(int token)
    {
        this.token = token;
    }

    @Override
    public final void onReceive(Context arg0, Intent arg1)
    {
        // TODO Auto-generated method stub
        if (isUnregistered)
            return; // 如果已经反注册，将直接返回
        String actionStr = arg1.getAction();
        int code = getResultCode();
        int srcToken = arg1.getIntExtra("SMS_TOKEN", -1);
        String to = arg1.getStringExtra("SMS_TO");
        String text = arg1.getStringExtra("SMS_TEXT");
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
                    onSendSuccess(to, text);
                } else
                {
                    onSendFailure(to, text);
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
                    onDeliverSuccess(to, text);
                } else
                {
                    onDeliverFailure(to, text);
                }
            }
        }
    }

    public void onDeliverSuccess(String to, String text)
    {
    }

    public void onDeliverFailure(String to, String text)
    {
    }

    public void onSendSuccess(String to, String text)
    {
    }

    public void onSendFailure(String to, String text)
    {
    }

    public void onTimeout()
    {
    }

    public void registerMe()
    {
        IntentFilter smsIntentFilter = new IntentFilter();
        smsIntentFilter.addAction(SmsUtils.SMS_SENT_ACTION);
        smsIntentFilter.addAction(SmsUtils.SMS_DELIVERED_ACTION);
        isDoneForAutoUnregisterActions = false;
        isUnregistered = false;
        context.registerReceiver(this, smsIntentFilter);
        if (timeout > 0)
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
                    } else if (timeCount >= timeout)
                    { // 已超时
                        cancel();
                        if (unregisterMe())
                        {
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    onTimeout();
                                }
                            });
                        }
                    }
                }
            }, 100, 100);
        }
    }

    public boolean unregisterMe()
    {
        isDoneForAutoUnregisterActions = true; // 在反注册时置为true，使计时器能够尽快退出
        isUnregistered = true;
        try
        {
            context.unregisterReceiver(this);
            return true;
        } catch (IllegalArgumentException e)
        {
            // 重复反注册会抛出该异常，如通过代码注册的receiver在当前activity销毁时会自动反注册，若再反注册，即会抛出该异常
            LogManager.logE(SmsSendCallback.class, "unregister receiver failed.", e);
            return false;
        }
    }

    public void setAutoUnregisterActions(int[] actions)
    {
        if (actions == null)
            throw new NullPointerException();
        Arrays.sort(actions);
        this.autoUnregisterActions = actions;
    }

    /**
     * <p>设置接收短信发送完毕消息的超时时间，超时时将回调onTimeout方法并自动反注册 <p>若设置了自动反注册action，在该action触发时，超时计时器将随之退出而不再计时
     * 
     * @param timeout 单位为毫秒，设为0将永不超时
     */
    public void setTimeout(int timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout could not be below zero.");
        this.timeout = timeout;
    }

}
