package cn.emagsoftware.telephony;

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Wendell on 14-12-22.
 */
final class LollipopDualModeSupport {

    private LollipopDualModeSupport(){}

    public static int getSimCount(Context context) throws ReflectHiddenFuncException {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
        try {
            Method method = TelephonyManager.class.getDeclaredMethod("getSimCount");
            method.setAccessible(true);
            return (Integer)method.invoke(tm);
        }catch (NoSuchMethodException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (IllegalAccessException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new ReflectHiddenFuncException(cause);
        }
    }

    private static long getSubId(int cardIndex) throws ReflectHiddenFuncException {
        int slotId;
        Field field;
        try {
            if(cardIndex == 0) {
                field = Class.forName("com.android.internal.telephony.PhoneConstants").getDeclaredField("SUB1");
            }else if(cardIndex == 1) {
                field = Class.forName("com.android.internal.telephony.PhoneConstants").getDeclaredField("SUB2");
            }else if(cardIndex == 2) {
                field = Class.forName("com.android.internal.telephony.PhoneConstants").getDeclaredField("SUB3");
            }else {
                throw new IllegalArgumentException("cardIndex can only be 0,1,2");
            }
            field.setAccessible(true);
            slotId = field.getInt(null);
            Method method = Class.forName("android.telephony.SubscriptionManager").getDeclaredMethod("getSubId",int.class);
            method.setAccessible(true);
            long[] val = (long[])method.invoke(null,slotId);
            return val[0];
        }catch (ClassNotFoundException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (NoSuchFieldException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (IllegalAccessException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (NoSuchMethodException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new ReflectHiddenFuncException(cause);
        }
    }

    public static String getSubscriberId(Context context,int cardIndex) throws ReflectHiddenFuncException {
        try {
            Method method = TelephonyManager.class.getDeclaredMethod("getSubscriberId",long.class);
            method.setAccessible(true);
            TelephonyManager tm = (TelephonyManager) context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
            return (String)method.invoke(tm,getSubId(cardIndex));
        }catch (NoSuchMethodException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (IllegalAccessException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new ReflectHiddenFuncException(cause);
        }
    }

    private static SmsManager getSmsManager(int cardIndex) throws ReflectHiddenFuncException {
        try {
            Method method = SmsManager.class.getDeclaredMethod("getSmsManagerForSubscriber",long.class);
            method.setAccessible(true);
            return (SmsManager)method.invoke(null,getSubId(cardIndex));
        }catch (NoSuchMethodException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (IllegalAccessException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new ReflectHiddenFuncException(cause);
        }
    }

    public static void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, int cardIndex) throws ReflectHiddenFuncException {
        getSmsManager(cardIndex).sendTextMessage(destinationAddress,scAddress,text,sentIntent,deliveryIntent);
    }

    public static void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, int cardIndex) throws ReflectHiddenFuncException {
        getSmsManager(cardIndex).sendDataMessage(destinationAddress,scAddress,destinationPort,data,sentIntent,deliveryIntent);
    }

}
