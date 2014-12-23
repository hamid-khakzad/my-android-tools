package cn.emagsoftware.telephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import cn.emagsoftware.telephony.receiver.SmsInterceptor;
import cn.emagsoftware.telephony.receiver.SmsReceiver;
import cn.emagsoftware.telephony.receiver.SmsSendCallback;

public final class SmsUtils
{

    public static final String SMS_SENT_ACTION      = "cn.emagsoftware.telephony.SMS_SENT";
    public static final String SMS_DELIVERED_ACTION = "cn.emagsoftware.telephony.SMS_DELIVERED";

    private static int         sendMessageToken     = 0;

    private SmsUtils()
    {
    }

    /**
     * <p>发送短信</>
     * @param context
     * @param to
     * @param text
     * @param ssc
     * @param timeout 单位为毫秒，设为0将永不超时
     */
    public static void sendTextMessage(Context context, String to, String text, SmsSendCallback ssc, int timeout) {
        sendMessageImpl(context,to,(short)-1,text,ssc,timeout);
    }

    public static void sendDataMessage(Context context, String to, short port, byte[] data, SmsSendCallback ssc, int timeout) {
        sendMessageImpl(context,to,port,data,ssc,timeout);
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
    public static void sendTextMessage(Context context, String to, String text, SmsSendCallback ssc, int timeout, int cardIndex) throws ReflectHiddenFuncException
    {
        sendMessageImpl(context,to,(short)-1,text,ssc,timeout,cardIndex);
    }

    public static void sendDataMessage(Context context, String to, short port, byte[] data, SmsSendCallback ssc, int timeout, int cardIndex) throws ReflectHiddenFuncException {
        sendMessageImpl(context,to,port,data,ssc,timeout,cardIndex);
    }

    private static void sendMessageImpl(Context context, String to, short port, Object data, SmsSendCallback ssc, int timeout) {
        if(sendMessageToken == Integer.MAX_VALUE) sendMessageToken = 0;
        sendMessageToken = sendMessageToken + 1;
        Intent sentIntent = new Intent(SMS_SENT_ACTION);
        sentIntent.putExtra("SMS_TOKEN", sendMessageToken);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, sendMessageToken, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        /*
         * Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION); deliveredIntent.putExtra("SMS_TOKEN", sendMessageToken);
         * PendingIntent deliveredPI = PendingIntent.getBroadcast(context,0,deliveredIntent,PendingIntent.FLAG_ONE_SHOT);
         */
        if (ssc != null)
        {
            ssc.setToken(sendMessageToken);
            ssc.setAutoUnregisterActions(new int[] { SmsSendCallback.ACTION_SENT });
            ssc.registerMe(timeout);
        }
        try {
            if(data instanceof String){
                SmsManager.getDefault().sendTextMessage(to, null, (String)data, sentPI, null); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
            }else{
                SmsManager.getDefault().sendDataMessage(to, null, port, (byte[])data, sentPI, null); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
            }
        } catch (RuntimeException e)
        {
            if (ssc != null)
                ssc.unregisterMe();
            throw e;
        }
    }

    private static void sendMessageImpl(Context context, String to, short port, Object data, SmsSendCallback ssc, int timeout, int cardIndex) throws ReflectHiddenFuncException {
        boolean isDualMode = TelephonyMgr.isDualMode(context);
        String name = null;
        String model = Build.MODEL;
        if (cardIndex == 0)
        {
            if ("Philips T939".equals(model))
                name = "isms0";
            else
                name = "isms";
        } else if (cardIndex == 1)
        {
            if (!isDualMode)
                return;
            if ("Philips T939".equals(model))
                name = "isms1";
            else
                name = "isms2";
        } else
            throw new IllegalArgumentException("cardIndex can only be 0 or 1");

        if(sendMessageToken == Integer.MAX_VALUE) sendMessageToken = 0;
        sendMessageToken = sendMessageToken + 1;
        Intent sentIntent = new Intent(SMS_SENT_ACTION);
        sentIntent.putExtra("SMS_TOKEN", sendMessageToken);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, sendMessageToken, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        /*
         * Intent deliveredIntent = new Intent(SMS_DELIVERED_ACTION); deliveredIntent.putExtra("SMS_TOKEN", sendMessageToken);
         * PendingIntent deliveredPI = PendingIntent.getBroadcast(context,0,deliveredIntent,PendingIntent.FLAG_ONE_SHOT);
         */
        if (ssc != null)
        {
            ssc.setToken(sendMessageToken);
            ssc.setAutoUnregisterActions(new int[] { SmsSendCallback.ACTION_SENT });
            ssc.registerMe(timeout);
        }
        try
        {
            if (isDualMode)
            {
                if(TelephonyMgr.getSDKVersion() >= Build.VERSION_CODES.LOLLIPOP) {
                    if(data instanceof String){
                        LollipopDualModeSupport.sendTextMessage(to,null,(String)data,sentPI,null,cardIndex); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                    }else {
                        LollipopDualModeSupport.sendDataMessage(to,null,port,(byte[])data,sentPI,null,cardIndex); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                    }
                    return;
                }
                if(HtcDualModeSupport.isDualMode()) {
                    if(data instanceof String){
                        HtcDualModeSupport.sendTextMessage(to, null, (String)data, sentPI, null, null, cardIndex); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                    }else{
                        HtcDualModeSupport.sendDataMessage(to, null, port, (byte[])data, sentPI, null, cardIndex); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                    }
                    return;
                }
                try
                {
                    Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
                    method.setAccessible(true);
                    Object param = method.invoke(null, name);
                    if (param == null)
                        throw new ReflectHiddenFuncException("can not get service which is named '" + name + "'");
                    method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
                    method.setAccessible(true);
                    Object stubObj = method.invoke(null, param);
                    if(data instanceof String){
                        if(TelephonyMgr.getSDKVersion() < 18){
                            method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                            method.invoke(stubObj, to, null, (String)data, sentPI, null); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                        }else{
                            method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                            method.invoke(stubObj, context.getPackageName(), to, null, (String)data, sentPI, null); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                        }
                    }else{
                        if(TelephonyMgr.getSDKVersion() < 18){
                            method = stubObj.getClass().getMethod("sendData", String.class, String.class, int.class, byte[].class, PendingIntent.class, PendingIntent.class);
                            method.invoke(stubObj, to, null, port & 0xFFFF, (byte[])data, sentPI, null); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                        }else{
                            method = stubObj.getClass().getMethod("sendData", String.class, String.class, String.class, int.class, byte[].class, PendingIntent.class, PendingIntent.class);
                            method.invoke(stubObj, context.getPackageName(), to, null, port & 0xFFFF, (byte[])data, sentPI, null); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                        }
                    }
                } catch (ClassNotFoundException e)
                {
                    throw new ReflectHiddenFuncException(e);
                } catch (NoSuchMethodException e)
                {
                    throw new ReflectHiddenFuncException(e);
                } catch (InvocationTargetException e)
                {
                    Throwable cause = e.getCause();
                    if(cause instanceof RuntimeException) throw (RuntimeException)cause;
                    else throw new ReflectHiddenFuncException(cause);
                } catch (IllegalAccessException e)
                {
                    throw new ReflectHiddenFuncException(e);
                }
            } else
            {
                if(data instanceof String){
                    SmsManager.getDefault().sendTextMessage(to, null, (String)data, sentPI, null); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                }else{
                    SmsManager.getDefault().sendDataMessage(to, null, port, (byte[])data, sentPI, null); // 暂时屏蔽了deliveryIntent事件的接收，因为其在某些机器上会弹出回执信息
                }
            }
        } catch (ReflectHiddenFuncException e)
        {
            if (ssc != null)
                ssc.unregisterMe();
            throw e;
        } catch (RuntimeException e)
        {
            if (ssc != null)
                ssc.unregisterMe();
            throw e;
        }
    }

    /**
     * <p>接收短信
     * 
     * @param sr
     * @param interruptWhenReceive
     * @param timeout 单位为毫秒，设为0将永不超时，此时可能需要手工反注册
     */
    /*public static void receiveMessage(SmsReceiver sr, boolean interruptWhenReceive, int timeout)
    {
        if (sr == null)
            throw new NullPointerException();
        sr.setAutoUnregisterWhenReceive(interruptWhenReceive);
        sr.registerMe(timeout);
    }*/

    /**
     * <p>拦截短信
     * 
     * @param si
     * @param interruptWhenIntercept
     * @param timeout 单位为毫秒，设为0将永不超时，此时可能需要手工反注册
     */
    /*public static void interceptMessage(SmsInterceptor si, boolean interruptWhenIntercept, int timeout)
    {
        if (si == null)
            throw new NullPointerException();
        si.setAutoUnregisterWhenIntercept(interruptWhenIntercept);
        si.registerMe(1000, timeout);
    }*/

}
