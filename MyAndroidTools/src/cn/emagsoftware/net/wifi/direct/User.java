package cn.emagsoftware.net.wifi.direct;

import java.io.UnsupportedEncodingException;

import cn.emagsoftware.net.wifi.WifiCallback;
import cn.emagsoftware.net.wifi.WifiUtils;
import cn.emagsoftware.telephony.ReflectHiddenFuncException;
import cn.emagsoftware.util.Base64;
import cn.emagsoftware.util.MathUtilities;
import android.content.Context;
import android.net.wifi.WifiConfiguration;

public class User
{
    
    private static final String ACTION_CHARSET = "UTF-8";
    
    private String name = null;
    private WifiConfiguration preWifiConfig = null;
    
    public User(String name)
    {
        if(name == null) throw new NullPointerException();
        this.name = name;
    }
    
    public void enabledWifiAp(Context context,WifiCallback callback,int timeout) throws ReflectHiddenFuncException
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        if(preWifiConfig == null) preWifiConfig = wifiUtils.getWifiApConfiguration();
        wifiUtils.setWifiApEnabled(createDirectApConfiguration(context), true, callback, timeout);
    }
    
    public void disabledWifiAp(Context context,WifiCallback callback,int timeout) throws ReflectHiddenFuncException
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.setWifiApConfiguration(preWifiConfig);
        preWifiConfig = null;
        wifiUtils.setWifiApEnabled(null, false, callback, timeout);
    }
    
    private WifiConfiguration createDirectApConfiguration(Context context)
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        WifiConfiguration apconfig = new WifiConfiguration();
        String apName = null;
        try
        {
            apName = "GHFY_" + Base64.encode(name.getBytes(ACTION_CHARSET)) + "_" + MathUtilities.Random(10000);
        }catch(UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        String apPassword = null;
        apconfig.SSID = apName;
        apconfig.BSSID = wifiUtils.getConnectionInfo().getMacAddress();
        apconfig.preSharedKey = apPassword;
        apconfig.allowedAuthAlgorithms.clear();
        apconfig.allowedAuthAlgorithms.set(0);
        apconfig.allowedProtocols.clear();
        apconfig.allowedProtocols.set(1);
        apconfig.allowedProtocols.set(0);
        apconfig.allowedKeyManagement.clear();
        if (apPassword == null)
            apconfig.allowedKeyManagement.set(0);
        else
            apconfig.allowedKeyManagement.set(1);
        apconfig.allowedPairwiseCiphers.clear();
        apconfig.allowedPairwiseCiphers.set(2);
        apconfig.allowedPairwiseCiphers.set(1);
        apconfig.allowedGroupCiphers.clear();
        apconfig.allowedGroupCiphers.set(3);
        apconfig.allowedGroupCiphers.set(2);
        apconfig.hiddenSSID = false;
        return apconfig;
    }
    
}
