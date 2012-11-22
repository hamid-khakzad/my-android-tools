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
                { // ���ܷ��ʵ�ԭʼվ�����ݣ�֤��������Ч
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
     * <p>�Դ�д��ʽ����ָ���������ϸ����
     * 
     * @param info
     * @return WIFI��CMNET��CMWAP��
     */
    public static String getNetworkDetailType(NetworkInfo info)
    {
        if (info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        String extraInfo = info.getExtraInfo(); // ͨ��Ϊ��ǰѡ�е�APN������
        if (extraInfo == null)
            return info.getTypeName().toUpperCase();
        extraInfo = extraInfo.toUpperCase();
        if (extraInfo.contains("CMNET"))
            return "CMNET";
        else if (extraInfo.contains("CMWAP"))
            return "CMWAP";
        else if (extraInfo.contains("UNINET"))
            return "UNINET";
        else if (extraInfo.contains("UNIWAP"))
            return "UNIWAP";
        else if (extraInfo.contains("CTNET"))
            return "CTNET";
        else if (extraInfo.contains("CTWAP"))
            return "CTWAP";
        else
            return extraInfo;
    }

    /**
     * <p>�Դ�д��ʽ���ص�ǰ�������ϸ����
     * 
     * @param context
     * @return WIFI��CMNET��CMWAP�ȣ�����null��ʾ��ǰ����δ֪
     */
    public static String getCurNetworkDetailType(Context context)
    {
        NetworkInfo ni = getActiveNetworkInfo(context);
        if (ni == null)
            return null;
        return getNetworkDetailType(ni);
    }

    /**
     * <p>�Ƿ��ڷ���ģʽ
     * 
     * @param context
     * @return
     */
    public static boolean isInAirplaneMode(Context context)
    {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * <p>�Ƿ���ڶ�������ӵ�����
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
     * <p>��ϵͳ�������õ�Activity
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
