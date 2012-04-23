package cn.emagsoftware.telephony;

import java.io.File;

import cn.emagsoftware.util.LogManager;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public final class TelephonyMgr
{

    private TelephonyMgr()
    {
    }

    public static String getSubscriberId(Context context)
    {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
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
        String subscriberId = getSubscriberId(context);
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
