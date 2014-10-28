package cn.emagsoftware.telephony;

import android.app.PendingIntent;
import android.os.Bundle;
import android.telephony.SmsManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Wendell on 14-10-23.
 */
final class HtcDualModeSupport {

    private HtcDualModeSupport() {
    }

    public static boolean isDualMode() throws ReflectHiddenFuncException {
        try {
            Class clz = Class.forName("com.htc.telephony.HtcTelephonyManager");
            Method method = clz.getDeclaredMethod("dualPhoneEnable");
            method.setAccessible(true);
            boolean dualPhoneEnable = (Boolean)method.invoke(null);
            if(dualPhoneEnable) return dualPhoneEnable;
            method = clz.getDeclaredMethod("dualGSMPhoneEnable");
            method.setAccessible(true);
            return (Boolean)method.invoke(null);
        }catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new ReflectHiddenFuncException(cause);
        }catch (Exception e) {
            return false;
        }
    }

    private static Object getHtcTelephonyManagerDefault() throws ReflectHiddenFuncException {
        try {
            Class clz = Class.forName("com.htc.telephony.HtcTelephonyManager");
            Method method = clz.getDeclaredMethod("getDefault");
            method.setAccessible(true);
            return method.invoke(null);
        }catch (ClassNotFoundException e) {
            throw new ReflectHiddenFuncException(e);
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

    private static int getHtcTelephonyManagerPhoneSlot(int cardIndex) throws ReflectHiddenFuncException {
        try {
            Field field;
            Class clz = Class.forName("com.htc.telephony.HtcTelephonyManager");
            if (cardIndex == 0)
            {
                field = clz.getDeclaredField("PHONE_SLOT1");
            } else if (cardIndex == 1)
            {
                field = clz.getDeclaredField("PHONE_SLOT2");
            } else
                throw new IllegalArgumentException("cardIndex can only be 0 or 1");
            field.setAccessible(true);
            return field.getInt(null);
        }catch (ClassNotFoundException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (NoSuchFieldException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (IllegalAccessException e) {
            throw new ReflectHiddenFuncException(e);
        }
    }

    public static String getSubscriberId(int cardIndex) throws ReflectHiddenFuncException {
        try {
            Object obj = getHtcTelephonyManagerDefault();
            return (String)obj.getClass().getMethod("getSubscriberIdExt",int.class).invoke(obj,getHtcTelephonyManagerPhoneSlot(cardIndex));
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

    private static Object newHtcWrapIfSmsManager() throws ReflectHiddenFuncException {
        try {
            Class clz = Class.forName("com.htc.wrap.android.telephony.HtcWrapIfSmsManager");
            Constructor<Object> con = clz.getDeclaredConstructor(SmsManager.class);
            con.setAccessible(true);
            return con.newInstance(SmsManager.getDefault());
        }catch (ClassNotFoundException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (NoSuchMethodException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (InstantiationException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (IllegalAccessException e) {
            throw new ReflectHiddenFuncException(e);
        }catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new ReflectHiddenFuncException(cause);
        }
    }

    public static void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent,Bundle bundle, int cardIndex) throws ReflectHiddenFuncException {
        try {
            Object obj = newHtcWrapIfSmsManager();
            Method method = obj.getClass().getDeclaredMethod("sendTextMessageExt",String.class,String.class,String.class,PendingIntent.class,PendingIntent.class,Bundle.class,int.class);
            method.setAccessible(true);
            method.invoke(obj,destinationAddress,scAddress,text,sentIntent,deliveryIntent,bundle,getHtcTelephonyManagerPhoneSlot(cardIndex));
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

    public static void sendDataMessage(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, int cardIndex) throws ReflectHiddenFuncException {
        try {
            Object obj = newHtcWrapIfSmsManager();
            Method method = obj.getClass().getDeclaredMethod("sendDataMessageExt",String.class,String.class,short.class,byte[].class,PendingIntent.class,PendingIntent.class,int.class);
            method.setAccessible(true);
            method.invoke(obj,destinationAddress,scAddress,destinationPort,data,sentIntent,deliveryIntent,getHtcTelephonyManagerPhoneSlot(cardIndex));
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

}
