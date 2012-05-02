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

    public static boolean isDualMode()
    {
        try
        {
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            return method.invoke(null, "phone") != null && method.invoke(null, "phone2") != null;
        } catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String getDeviceId(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static String getSubscriberId(int cardIndex)
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
                throw new RuntimeException("can not found the card index " + cardIndex);
            method = Class.forName("com.android.internal.telephony.IPhoneSubInfo$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            return (String) stubObj.getClass().getMethod("getSubscriberId").invoke(stubObj);
        } catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static int getSimState(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
        return tm.getSimState();
    }

    public static boolean isSimCard(Context context)
    {
        return getSimState(context) != TelephonyManager.SIM_STATE_ABSENT;
    }

    public static boolean isSimAndValidCard(Context context)
    {
        return getSimState(context) == TelephonyManager.SIM_STATE_READY;
    }

    public static boolean isChinaMobileCard(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
        String simOperatorName = tm.getSimOperatorName();
        String subscriberId = getSubscriberId(0);
        if ("CMCC".equalsIgnoreCase(simOperatorName) || (!TextUtils.isEmpty(subscriberId) && (subscriberId.contains("46000") || subscriberId.contains("46002") || subscriberId.contains("46007"))))
            return true;
        else
            return false;
    }

    public static boolean isSimAndChinaMobileCard(Context context)
    {
        return isSimCard(context) && isChinaMobileCard(context);
    }

    public static boolean isSimAndValidAndChinaMobileCard(Context context)
    {
        return isSimAndValidCard(context) && isChinaMobileCard(context);
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

}
