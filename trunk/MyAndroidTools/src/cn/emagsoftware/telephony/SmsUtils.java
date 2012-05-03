package cn.emagsoftware.telephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import cn.emagsoftware.telephony.receiver.SmsInterceptor;
import cn.emagsoftware.telephony.receiver.SmsReceiver;
import cn.emagsoftware.telephony.receiver.SmsSendCallback;

public final class SmsUtils
{

    public static final String SMS_SENT_ACTION      = "cn.emagsoftware.telephony.SMS_SENT";
    public static final String SMS_DELIVERED_ACTION = "cn.emagsoftware.telephony.SMS_DELIVERED";

    private static int         sendMessageToken     = 0;
    private static SmsManager  smsManager           = SmsManager.getDefault();

    private SmsUtils()
    {
    }

    /**
     * <p>发送短信
     * 
     * @param context
     * @param to
     * @param text
     * @param ssc
     * @param timeout 单位为毫秒，设为0将永不超时
     * @param cardIndex 卡位置，0或1
     * @throws ReflectHiddenFuncException
     */
    public static void sendMessage(Context context, String to, String text, SmsSendCallback ssc, int timeout, int cardIndex) throws ReflectHiddenFuncException
    {
        if (cardIndex != 0 && cardIndex != 1)
            throw new IllegalArgumentException("cardIndex can only be 0 or 1");
        boolean isDualMode = TelephonyMgr.isDualMode();
        if (!isDualMode && cardIndex == 1)
            return;
        sendMessageToken = sendMessageToken + 1;
        Intent sentIntent = new Intent(SMS_SENT_ACTION);
        sentIntent.putExtra("SMS_TOKEN", sendMessageToken);
        sentIntent.putExtra("SMS_TO", to);
        sentIntent.putExtra("SMS_TEXT", text);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_ONE_SHOT);
        /*
         * Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION); deliveredIntent.putExtra("SMS_TOKEN", sendMessageToken); deliveredIntent.putExtra("SMS_TO", to);
         * deliveredIntent.putExtra("SMS_TEXT", text); PendingIntent deliveredPI = PendingIntent.getBroadcast(context,0,deliveredIntent,PendingIntent.FLAG_ONE_SHOT);
         */
        if (ssc != null)
        {
            ssc.setToken(sendMessageToken);
            ssc.setAutoUnregisterActions(new int[] { SmsSendCallback.ACTION_SENT });
            ssc.setTimeout(timeout);
            ssc.registerMe();
        }
        if (isDualMode)
        {
            try
            {
                Method method = smsManager.getClass().getMethod("sendTextMessage", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, int.class);
                method.invoke(smsManager, to, null, text, sentPI, null, cardIndex); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
            } catch (NoSuchMethodException e)
            {
                throw new ReflectHiddenFuncException(e);
            } catch (InvocationTargetException e)
            {
                throw new ReflectHiddenFuncException(e);
            } catch (IllegalAccessException e)
            {
                throw new ReflectHiddenFuncException(e);
            }
        } else
        {
            smsManager.sendTextMessage(to, null, text, sentPI, null); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
        }
    }

    /**
     * <p>接收短信
     * 
     * @param sr
     * @param interruptWhenReceive
     * @param timeout 单位为毫秒，设为0将永不超时，此时可能需要手工反注册
     */
    public static void receiveMessage(SmsReceiver sr, boolean interruptWhenReceive, int timeout)
    {
        if (sr == null)
            throw new NullPointerException();
        sr.setAutoUnregisterWhenReceive(interruptWhenReceive);
        sr.setTimeout(timeout);
        sr.registerMe();
    }

    /**
     * <p>拦截短信
     * 
     * @param si
     * @param interruptWhenIntercept
     * @param timeout 单位为毫秒，设为0将永不超时，此时可能需要手工反注册
     */
    public static void interceptMessage(SmsInterceptor si, boolean interruptWhenIntercept, int timeout)
    {
        if (si == null)
            throw new NullPointerException();
        si.setAutoUnregisterWhenIntercept(interruptWhenIntercept);
        si.setTimeout(timeout);
        si.registerMe(1000);
    }

}
