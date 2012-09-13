package cn.emagsoftware.net;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

import cn.emagsoftware.net.http.HttpConnectionManager;
import cn.emagsoftware.net.http.HttpResponseResult;
import cn.emagsoftware.util.LogManager;

public final class NetManager
{

    private NetManager()
    {
    }

    public static boolean isNetConnected(Context context)
    {
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info != null)
            return info.getState() == NetworkInfo.State.CONNECTED;
        return false;
    }

    public static boolean isNetUseful(int timeout, int tryTimes)
    {
        if (tryTimes <= 0)
            throw new IllegalArgumentException("trying times should be greater than zero.");
        int th = 1;
        while (th <= tryTimes)
        {
            try
            {
                HttpResponseResult result = HttpConnectionManager.doGet("http://www.baidu.com", null, true, timeout, null);
                String host = result.getResponseURL().getHost();
                String content = result.getDataString("utf-8");
                if ("www.baidu.com".equalsIgnoreCase(host) && content.indexOf("baidu.com") >= 0)
                { // 若能访问到原始站点内容，证明网络有效
                    return true;
                } else
                {
                    return false;
                }
            } catch (IOException e)
            {
                LogManager.logE(NetManager.class, "the " + th + " time to check net for method of isNetUseful failed.", e);
            }
            th++;
        }
        LogManager.logE(NetManager.class, "checking net for method of isNetUseful has all failed,will return false.");
        return false;
    }

    public static NetworkInfo getActiveNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    public static NetworkInfo getMobileNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    }

    public static NetworkInfo getWifiNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    }

    public static NetworkInfo[] getAllNetworkInfo(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getAllNetworkInfo();
    }

    /**
     * <p>以大写形式返回指定网络的详细类型
     * 
     * @param info
     * @return WIFI、CMNET、CMWAP等，返回null表示网络类型未知
     */
    public static String getNetworkInfoType(NetworkInfo info)
    {
        String type = info.getTypeName();
        if (type.equals("WIFI"))
            return "WIFI";
        else
        {
            String extraInfo = info.getExtraInfo();
            if (extraInfo == null)
                return type;
            else
            {
                extraInfo = extraInfo.toUpperCase();
                if (extraInfo.indexOf("CMNET") != -1)
                    return "CMNET"; // 有些机型返回的是不精确的类型，如CMNET:GSM
                else if (extraInfo.indexOf("CMWAP") != -1)
                    return "CMWAP"; // 有些机型返回的是不精确的类型，如CMWAP:GSM
                else
                    return extraInfo;
            }
        }
    }

    /**
     * <p>是否处于飞行模式
     * 
     * @param context
     * @return
     */
    public static boolean isInAirplaneMode(Context context)
    {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * <p>是否存在多个已连接的网络
     * 
     * @param context
     * @return
     */
    public static boolean isAvailableMultiConnectedNets(Context context)
    {
        boolean wifiConnected = false;
        boolean otherConnected = false;
        NetworkInfo[] infos = getAllNetworkInfo(context);
        if (infos != null)
        {
            for (int i = 0; i < infos.length; i++)
            {
                if (infos[i].getState() == NetworkInfo.State.CONNECTED)
                {
                    if (infos[i].getType() == ConnectivityManager.TYPE_WIFI)
                    {
                        wifiConnected = true;
                    } else
                    {
                        otherConnected = true;
                    }
                }
            }
        }
        return wifiConnected && otherConnected;
    }

    /**
     * <p>打开系统网络设置的Activity
     * 
     * @param context
     */
    public static void startWirelessSettingsActivity(Context context)
    {
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        context.startActivity(intent);
    }

}
