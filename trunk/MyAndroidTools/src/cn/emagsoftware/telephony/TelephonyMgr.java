package cn.emagsoftware.telephony;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;

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
            String model = Build.MODEL;
            if ("Philips T939".equals(model))
                return method.invoke(null, "phone0") != null && method.invoke(null, "phone1") != null;
            else
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
        String model = Build.MODEL;
        if (cardIndex == 0)
        {
            if ("Philips T939".equals(model))
                name = "iphonesubinfo0";
            else
                name = "iphonesubinfo";
        } else if (cardIndex == 1)
        {
            if ("Philips T939".equals(model))
                name = "iphonesubinfo1";
            else
                name = "iphonesubinfo2";
        } else
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

    public static boolean isChinaMobileCard(String subscriberId)
    {
        if (subscriberId == null)
            return false;
        if (subscriberId.contains("46000") || subscriberId.contains("46002") || subscriberId.contains("46007"))
            return true;
        return false;
    }

    public static String getExternalStorageState()
    {
        return Environment.getExternalStorageState();
    }

    public static long getExternalStorageSize()
    {
        return getFileStorageSize(Environment.getExternalStorageDirectory());
    }

    public static long getFileStorageSize(File file)
    {
        StatFs stat = new StatFs(file.getAbsolutePath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return blockSize * totalBlocks;
    }

    public static long getExternalStorageAvailableSize()
    {
        return getFileStorageAvailableSize(Environment.getExternalStorageDirectory());
    }

    public static long getFileStorageAvailableSize(File file)
    {
        StatFs stat = new StatFs(file.getAbsolutePath());
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
        return Build.VERSION.SDK_INT;
    }

    public static boolean isAndroid4Above()
    {
        return getSDKVersion() >= 14;
    }

    public static boolean isOPhone()
    {
        Class<?> dclass[] = Settings.class.getClasses();
        if (dclass != null)
        {
            for (int i = 0; i < dclass.length; i++)
            {
                Class<?> singleC = dclass[i];
                String name = singleC.getName();
                if (name.indexOf("Data_connection") != -1)
                {
                    return true; // omsver10
                }
            }
        }
        try
        {
            Class.forName("oms.dcm.DataConnectivityConstants");
            return true; // omsver15 above
        } catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    public static boolean isOPhone20()
    {
        return isOPhone() && getSDKVersion() == 7;
    }

    public static boolean isUsingNewButtonPlacementStyle()
    {
        String model = Build.MODEL;
        if ("GT-P3108".equals(model) || "GT-I9050".equals(model) || "BBK S6T".equals(model))
            return false;
        if ("GT-I9108".equals(model) || "GT-I9228".equals(model))
            return getSDKVersion() >= 16;
        return isAndroid4Above();
    }

}
