package cn.emagsoftware.telephony;

import android.app.PendingIntent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Wendell on 14-10-23.
 */
final class MX4DualModeSupport {

    private MX4DualModeSupport() {
    }

    public static boolean isDualMode() throws ReflectHiddenFuncException {
        try {
            Class clz = Class.forName("android.os.BuildExt");
            Field field = clz.getDeclaredField("IS_M1_NOTE");
            field.setAccessible(true);
            return field.getBoolean(null);
        }catch (Exception e) {
            return false;
        }
    }

    public static String getSubscriberId(int cardIndex) throws ReflectHiddenFuncException {
        try {
            Class clz = Class.forName("android.telephony.TelephonyManagerEx");
            Method method = clz.getDeclaredMethod("getDefault");
            method.setAccessible(true);
            Object obj = method.invoke(null);
            method = clz.getDeclaredMethod("getSubscriberId",int.class);
            method.setAccessible(true);
            return (String)method.invoke(obj,cardIndex);
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

    private static Object getSmsManagerExDefault() throws ReflectHiddenFuncException {
        try {
            Class clz = Class.forName("com.mediatek.telephony.SmsManagerEx");
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

    public static void sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, int cardIndex) throws ReflectHiddenFuncException {
        try {
            Object obj = getSmsManagerExDefault();
            Method method = obj.getClass().getDeclaredMethod("sendTextMessage",String.class,String.class,String.class,PendingIntent.class,PendingIntent.class,int.class);
            method.setAccessible(true);
            method.invoke(obj,destinationAddress,scAddress,text,sentIntent,deliveryIntent,cardIndex);
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
            Object obj = getSmsManagerExDefault();
            Method method = obj.getClass().getDeclaredMethod("sendDataMessage",String.class,String.class,short.class,byte[].class,PendingIntent.class,PendingIntent.class,int.class);
            method.setAccessible(true);
            method.invoke(obj,destinationAddress,scAddress,destinationPort,data,sentIntent,deliveryIntent,cardIndex);
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
