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
                    return; // ����Ѿ���ע�ᣬ��ֱ�ӷ���
                String actionStr = intent.getAction();
                int code = getResultCode();
                int srcToken = intent.getIntExtra("SMS_TOKEN", -1);
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
     * ����Ϊ-1�����������еĶ��ŷ���
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
     * <p>ע�Ტָ���ȴ���ʱʱ�䣬��ʱʱ���ص�onTimeout�������Զ���ע�� <p>���ڳ�ʱ֮ǰ�Ѿ���ע�ᣬ�򽫲��ټ��㳬ʱ
     * 
     * @param timeout ��λΪ���룬��Ϊ0��������ʱ
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
            isDoneForAutoUnregisterActions = true; // �ø�ֵΪtrue��ʹ��ʱ��ʱ���ܹ������˳�
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
            LogManager.logW(SmsSendCallback.class, "unregister receiver failed.", e);
            return false;
        }
    }

}
