package cn.emagsoftware.telephony;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cn.emagsoftware.util.LogManager;

public final class TelephonyMgr
{

    private TelephonyMgr()
    {
    }

    public static boolean isDualMode() throws ReflectHiddenFuncException
    {
        try
        {
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            return (method.invoke(null, "phone") != null && method.invoke(null, "phone2") != null)
                    || (method.invoke(null, "telephony.registry") != null && method.invoke(null, "telephony.registry2") != null);
        } catch (ClassNotFoundException e)
        {
            throw new ReflectHiddenFuncException(e);
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
    }

    public static String getDeviceId(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static String getSubscriberId(int cardIndex) throws ReflectHiddenFuncException
    {
        String name = null;
        if (cardIndex == 0)
            name = "iphonesubinfo";
        else if (cardIndex == 1)
            name = "iphonesubinfo2";
        else
            throw new IllegalArgumentException("cardIndex can only be 0 or 1");
        try
        {
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);
            if (param == null && cardIndex == 1)
                param = method.invoke(null, "iphonesubinfo1");
            if (param == null)
                return null;
            method = Class.forName("com.android.internal.telephony.IPhoneSubInfo$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            return (String) stubObj.getClass().getMethod("getSubscriberId").invoke(stubObj);
        } catch (ClassNotFoundException e)
        {
            throw new ReflectHiddenFuncException(e);
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
    }

    public static int getFirstSimState() throws ReflectHiddenFuncException
    {
        return getSimState("gsm.sim.state");
    }

    public static int getSecondSimState() throws ReflectHiddenFuncException
    {
        return getSimState("gsm.sim.state_2");
    }

    private static int getSimState(String simState) throws ReflectHiddenFuncException
    {
        try
        {
            Method method = Class.forName("android.os.SystemProperties").getDeclaredMethod("get", String.class);
            method.setAccessible(true);
            String prop = (String) method.invoke(null, simState);
            if (prop != null)
                prop = prop.split(",")[0]; // 有些机器返回好几个状态描述
            if ("ABSENT".equals(prop))
            {
                return TelephonyManager.SIM_STATE_ABSENT;
            } else if ("PIN_REQUIRED".equals(prop))
            {
                return TelephonyManager.SIM_STATE_PIN_REQUIRED;
            } else if ("PUK_REQUIRED".equals(prop))
            {
                return TelephonyManager.SIM_STATE_PUK_REQUIRED;
            } else if ("NETWORK_LOCKED".equals(prop))
            {
                return TelephonyManager.SIM_STATE_NETWORK_LOCKED;
            } else if ("READY".equals(prop))
            {
                return TelephonyManager.SIM_STATE_READY;
            } else
            {
                return TelephonyManager.SIM_STATE_UNKNOWN;
            }
        } catch (ClassNotFoundException e)
        {
            throw new ReflectHiddenFuncException(e);
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
    }

    public static boolean isFirstSimValid() throws ReflectHiddenFuncException
    {
        return getFirstSimState() == TelephonyManager.SIM_STATE_READY;
    }

    public static boolean isSecondSimValid() throws ReflectHiddenFuncException
    {
        return getSecondSimState() == TelephonyManager.SIM_STATE_READY;
    }

    public static boolean isChinaMobileCard(int cardIndex) throws ReflectHiddenFuncException
    {
        String subscriberId = getSubscriberId(cardIndex);
        if (!TextUtils.isEmpty(subscriberId) && (subscriberId.contains("46000") || subscriberId.contains("46002") || subscriberId.contains("46007")))
            return true;
        else
            return false;
    }

    public static String getExternalStorageState()
    {
        return Environment.getExternalStorageState();
    }

    public static long getExternalStorageSize()
    {
        String path = Environment.getExternalStorageDirectory().getPath();
        File file = new File(path);
        StatFs stat = new StatFs(file.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return blockSize * totalBlocks;
    }

    public static long getExternalStorageAvailableSize()
    {
        String path = Environment.getExternalStorageDirectory().getPath();
        File file = new File(path);
        StatFs stat = new StatFs(file.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * (availableBlocks - 4);
    }

    public static boolean isExternalStorageValid()
    {
        return getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static int getSDKVersion()
    {
        try
        {
            return Integer.valueOf(Build.VERSION.SDK);
        } catch (NumberFormatException e)
        {
            LogManager.logW(TelephonyMgr.class, "can not convert SDK", e);
            return 0;
        }
    }

    public static boolean isAndroid4Above()
    {
        return getSDKVersion() >= 14;
    }

    public static boolean isUsingNewButtonPlacementStyle()
    {
        String model = Build.MODEL;
        if ("GT-P3108".equals(model) || "GT-I9108".equals(model) || "GT-I9228".equals(model) || "GT-I9050".equals(model) || "vivo PD1203TG3".equals(model))
            return false;
        return isAndroid4Above();
    }

}
