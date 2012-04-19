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
     * ����Ϊ-1�����������еĶ��ŷ���
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
            return; // ����Ѿ���ע�ᣬ��ֱ�ӷ���
        String actionStr = arg1.getAction();
        int code = getResultCode();
        int srcToken = arg1.getIntExtra("SMS_TOKEN", -1);
        String to = arg1.getStringExtra("SMS_TO");
        String text = arg1.getStringExtra("SMS_TEXT");
        if (token == -1 || token == srcToken)
        { // ��֤token
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
        { // Ϊ0ʱ��������ʱ
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
                    { // �ѳ�ʱ
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
        isDoneForAutoUnregisterActions = true; // �ڷ�ע��ʱ��Ϊtrue��ʹ��ʱ���ܹ������˳�
        isUnregistered = true;
        try
        {
            context.unregisterReceiver(this);
            return true;
        } catch (IllegalArgumentException e)
        {
            // �ظ���ע����׳����쳣����ͨ������ע���receiver�ڵ�ǰactivity����ʱ���Զ���ע�ᣬ���ٷ�ע�ᣬ�����׳����쳣
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
     * <p>���ý��ն��ŷ��������Ϣ�ĳ�ʱʱ�䣬��ʱʱ���ص�onTimeout�������Զ���ע�� <p>���������Զ���ע��action���ڸ�action����ʱ����ʱ��ʱ������֮�˳������ټ�ʱ
     * 
     * @param timeout ��λΪ���룬��Ϊ0��������ʱ
     */
    public void setTimeout(int timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout could not be below zero.");
        this.timeout = timeout;
    }

}
