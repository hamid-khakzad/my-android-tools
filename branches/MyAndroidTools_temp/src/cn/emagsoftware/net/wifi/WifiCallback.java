package cn.emagsoftware.net.wifi;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import cn.emagsoftware.util.LogManager;

/**
 * <p>Wifi操作的广播回调类，该类实例是非线程安全的 <p>该类可独立使用，也可与WifiUtils类配合作为方法回调类使用
 * 
 * @author Wendell
 * @version 4.2
 */
public abstract class WifiCallback
{

    public static final int           ACTION_WIFI_ENABLED            = 0;
    public static final int           ACTION_WIFI_ENABLING           = 1;
    public static final int           ACTION_WIFI_DISABLED           = 2;
    public static final int           ACTION_WIFI_DISABLING          = 3;
    public static final int           ACTION_WIFI_FAILED             = 4;
    public static final int           ACTION_SCAN_RESULTS            = 5;
    public static final int           ACTION_NETWORK_IDLE            = 6;
    public static final int           ACTION_NETWORK_SCANNING        = 7;
    public static final int           ACTION_NETWORK_OBTAININGIP     = 8;
    public static final int           ACTION_NETWORK_DISCONNECTED    = 9;
    public static final int           ACTION_NETWORK_CONNECTED       = 10;
    public static final int           ACTION_NETWORK_FAILED          = 11;
    public static final int           ACTION_WIFI_AP_ENABLED         = 12;
    public static final int           ACTION_WIFI_AP_ENABLING        = 13;
    public static final int           ACTION_WIFI_AP_DISABLED        = 14;
    public static final int           ACTION_WIFI_AP_DISABLING       = 15;
    public static final int           ACTION_WIFI_AP_FAILED          = 16;

    private static String             WIFI_AP_STATE_CHANGED_ACTION   = null;
    private static String             EXTRA_WIFI_AP_STATE            = null;
    private static int                WIFI_AP_STATE_DISABLING        = -1;
    private static int                WIFI_AP_STATE_DISABLED         = -1;
    private static int                WIFI_AP_STATE_ENABLING         = -1;
    private static int                WIFI_AP_STATE_ENABLED          = -1;
    private static int                WIFI_AP_STATE_FAILED           = -1;

    private Context                   context                        = null;
    private BroadcastReceiver         receiver                       = null;
    private boolean                   ignoreInitialNetworkActions    = false;
    private boolean                   isInitialNetworkAction         = true;
    private NetworkInfo.DetailedState prevNetworkDetailed            = null;
    private int[]                     autoUnregisterActions          = new int[] {};
    private boolean                   isDoneForAutoUnregisterActions = false;
    private Handler                   handler                        = new Handler();
    private boolean                   isUnregistered                 = true;
    private boolean                   isUnregisteredCompletely       = true;
    private int                       curTimeout                     = -1;

    public WifiCallback(Context ctx)
    {
        if (ctx == null)
            throw new NullPointerException();
        context = ctx;
        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // TODO Auto-generated method stub
                if (isUnregistered)
                    return; // 如果已经反注册，将直接返回
                String action = intent.getAction();
                if (isInitialStickyBroadcast() && autoUnregisterActions.length > 0)
                {
                    if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
                    {
                        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                        {
                            prevNetworkDetailed = networkInfo.getDetailedState();
                        }
                    }
                    LogManager.logD(WifiCallback.class, "ignore initial sticky state");
                    return;
                }
                final WifiUtils wifiUtils = new WifiUtils(context);
                if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
                {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    if (state == WifiManager.WIFI_STATE_ENABLED)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_ENABLED");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLED) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiEnabled();
                    } else if (state == WifiManager.WIFI_STATE_ENABLING)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_ENABLING");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_ENABLING) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiEnabling();
                    } else if (state == WifiManager.WIFI_STATE_DISABLED)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_DISABLED");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLED) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiDisabled();
                    } else if (state == WifiManager.WIFI_STATE_DISABLING)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_DISABLING");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_DISABLING) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiDisabling();
                    } else if (state == WifiManager.WIFI_STATE_UNKNOWN)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi state -> WIFI_STATE_UNKNOWN");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_FAILED) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiFailed();
                    }
                } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
                {
                    LogManager.logD(WifiCallback.class, "receive scan state -> SCAN_RESULTS_AVAILABLE");
                    if (Arrays.binarySearch(autoUnregisterActions, ACTION_SCAN_RESULTS) > -1)
                    {
                        isDoneForAutoUnregisterActions = true;
                        if (!unregisterMe())
                            return;
                    }
                    final List<ScanResult> results = wifiUtils.getWifiManager().getScanResults();
                    if (results != null)
                    {
                        // 使WLAN热点按信号由强到弱排序(采用冒泡排序算法)
                        for (int i = 0; i < results.size(); i++)
                        {
                            ScanResult curr = results.get(i);
                            for (int j = i - 1; j >= 0; j--)
                            {
                                ScanResult pre = results.get(j);
                                if (curr.level <= pre.level)
                                { // 当前的WLAN热点已经不能再向前排了
                                    if (i != j + 1)
                                    {
                                        results.remove(i);
                                        results.add(j + 1, curr);
                                    }
                                    break;
                                } else if (j == 0)
                                { // 当前的WLAN热点信号是最强的，已经排到了第一位
                                    if (i != 0)
                                    {
                                        results.remove(i);
                                        results.add(0, curr);
                                    }
                                }
                            }
                        }
                        // 移除搜索到的相同WLAN热点
                        for (int i = 0; i < results.size(); i++)
                        {
                            ScanResult curr = results.get(i);
                            for (int j = 0; j < i; j++)
                            {
                                ScanResult pre = results.get(j);
                                if (curr.SSID.equals(pre.SSID) && wifiUtils.getScanResultSecurity(curr).equals(wifiUtils.getScanResultSecurity(pre)))
                                {
                                    results.remove(i);
                                    i--;
                                    break;
                                }
                            }
                        }
                    }
                    onScanResults(results);
                } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
                {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                    {
                        NetworkInfo.DetailedState detailed = networkInfo.getDetailedState();
                        if (ignoreInitialNetworkActions)
                        {
                            if (isInitialNetworkAction)
                            {
                                if (detailed == NetworkInfo.DetailedState.IDLE)
                                {
                                    // 该状态直接认为有效
                                    isInitialNetworkAction = false;
                                } else if (detailed == NetworkInfo.DetailedState.SCANNING)
                                {
                                    // 只有当上一个状态为IDLE或该状态的后续状态时，才表明新的连接已经发起，才认为该状态有效
                                    if (prevNetworkDetailed == null || prevNetworkDetailed == NetworkInfo.DetailedState.SCANNING)
                                    {
                                        prevNetworkDetailed = detailed;
                                        LogManager.logD(WifiCallback.class, "ignore initial network state -> NETWORK_STATE_SCANNING");
                                        return;
                                    }
                                    isInitialNetworkAction = false;
                                } else if (detailed == NetworkInfo.DetailedState.OBTAINING_IPADDR)
                                {
                                    // 只有当上一个状态为IDLE或该状态的后续状态时，才表明新的连接已经发起，才认为该状态有效
                                    if (prevNetworkDetailed == null || prevNetworkDetailed == NetworkInfo.DetailedState.SCANNING || prevNetworkDetailed == NetworkInfo.DetailedState.OBTAINING_IPADDR)
                                    {
                                        prevNetworkDetailed = detailed;
                                        LogManager.logD(WifiCallback.class, "ignore initial network state -> NETWORK_STATE_OBTAININGIP");
                                        return;
                                    }
                                    isInitialNetworkAction = false;
                                } else if (detailed == NetworkInfo.DetailedState.DISCONNECTED)
                                {
                                    // 由于已经舍弃了初始粘性状态，该状态的发生表明旧的连接已经结束，将认为之后的状态有效
                                    isInitialNetworkAction = false;
                                    LogManager.logD(WifiCallback.class, "ignore initial network state -> NETWORK_STATE_DISCONNECTED");
                                    return;
                                } else if (detailed == NetworkInfo.DetailedState.CONNECTED)
                                {
                                    // 由于已经舍弃了初始粘性状态，该状态直接认为有效
                                    isInitialNetworkAction = false;
                                } else if (detailed == NetworkInfo.DetailedState.FAILED)
                                {
                                    // 由于已经舍弃了初始粘性状态，该状态的发生表明旧的连接已经结束，将认为之后的状态有效
                                    isInitialNetworkAction = false;
                                    LogManager.logD(WifiCallback.class, "ignore initial network state -> NETWORK_STATE_FAILED");
                                    return;
                                }
                            }
                        }
                        if (detailed == NetworkInfo.DetailedState.IDLE)
                        {
                            LogManager.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_IDLE");
                            if (Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_IDLE) > -1)
                            {
                                isDoneForAutoUnregisterActions = true;
                                if (!unregisterMe())
                                    return;
                            }
                            onNetworkIdle(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.SCANNING)
                        {
                            LogManager.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_SCANNING");
                            if (Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_SCANNING) > -1)
                            {
                                isDoneForAutoUnregisterActions = true;
                                if (!unregisterMe())
                                    return;
                            }
                            onNetworkScanning(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.OBTAINING_IPADDR)
                        {
                            LogManager.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_OBTAININGIP");
                            if (Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_OBTAININGIP) > -1)
                            {
                                isDoneForAutoUnregisterActions = true;
                                if (!unregisterMe())
                                    return;
                            }
                            onNetworkObtainingIp(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.DISCONNECTED)
                        {
                            LogManager.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_DISCONNECTED");
                            if (Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_DISCONNECTED) > -1)
                            {
                                isDoneForAutoUnregisterActions = true;
                                if (!unregisterMe())
                                    return;
                            }
                            onNetworkDisconnected(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.CONNECTED)
                        {
                            LogManager.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_CONNECTED");
                            if (Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_CONNECTED) > -1)
                            {
                                isDoneForAutoUnregisterActions = true;
                                if (!unregisterMe())
                                    return;
                            }
                            onNetworkConnected(wifiUtils.getConnectionInfo());
                        } else if (detailed == NetworkInfo.DetailedState.FAILED)
                        {
                            LogManager.logD(WifiCallback.class, "receive network state -> NETWORK_STATE_FAILED");
                            if (Arrays.binarySearch(autoUnregisterActions, ACTION_NETWORK_FAILED) > -1)
                            {
                                isDoneForAutoUnregisterActions = true;
                                if (!unregisterMe())
                                    return;
                            }
                            onNetworkFailed(wifiUtils.getConnectionInfo());
                        }
                    }
                } else if (action.equals(WIFI_AP_STATE_CHANGED_ACTION))
                {
                    if (EXTRA_WIFI_AP_STATE == null)
                        return;
                    int state = intent.getIntExtra(EXTRA_WIFI_AP_STATE, 0);
                    if (state == WIFI_AP_STATE_ENABLED)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_ENABLED");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_AP_ENABLED) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiApEnabled();
                    } else if (state == WIFI_AP_STATE_ENABLING)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_ENABLING");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_AP_ENABLING) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiApEnabling();
                    } else if (state == WIFI_AP_STATE_DISABLED)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_DISABLED");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_AP_DISABLED) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiApDisabled();
                    } else if (state == WIFI_AP_STATE_DISABLING)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_DISABLING");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_AP_DISABLING) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiApDisabling();
                    } else if (state == WIFI_AP_STATE_FAILED)
                    {
                        LogManager.logD(WifiCallback.class, "receive wifi ap state -> WIFI_AP_STATE_FAILED");
                        if (Arrays.binarySearch(autoUnregisterActions, ACTION_WIFI_AP_FAILED) > -1)
                        {
                            isDoneForAutoUnregisterActions = true;
                            if (!unregisterMe())
                                return;
                        }
                        onWifiApFailed();
                    }
                }
            }
        };
        Arrays.sort(autoUnregisterActions);
        // 以下参数在不同Android版本中的值不一致，故需要通过反射来获取
        try
        {
            Field field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_CHANGED_ACTION");
            field.setAccessible(true);
            WIFI_AP_STATE_CHANGED_ACTION = (String) field.get(null);
            field = WifiManager.class.getDeclaredField("EXTRA_WIFI_AP_STATE");
            field.setAccessible(true);
            EXTRA_WIFI_AP_STATE = (String) field.get(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_DISABLING");
            field.setAccessible(true);
            WIFI_AP_STATE_DISABLING = field.getInt(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_DISABLED");
            field.setAccessible(true);
            WIFI_AP_STATE_DISABLED = field.getInt(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_ENABLING");
            field.setAccessible(true);
            WIFI_AP_STATE_ENABLING = field.getInt(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_ENABLED");
            field.setAccessible(true);
            WIFI_AP_STATE_ENABLED = field.getInt(null);
            field = WifiManager.class.getDeclaredField("WIFI_AP_STATE_FAILED");
            field.setAccessible(true);
            WIFI_AP_STATE_FAILED = field.getInt(null);
        } catch (NoSuchFieldException e)
        {
            LogManager.logW(WifiCallback.class, "reflect wifi ap field failed.", e);
        } catch (IllegalAccessException e)
        {
            LogManager.logW(WifiCallback.class, "reflect wifi ap field failed.", e);
        }
    }

    public void onCheckWifiExist()
    {
    }

    public void onCheckWifiNotExist()
    {
    }

    public void onWifiEnabled()
    {
    }

    public void onWifiEnabling()
    {
    }

    public void onWifiDisabled()
    {
    }

    public void onWifiDisabling()
    {
    }

    public void onWifiFailed()
    {
    }

    public void onScanResults(List<ScanResult> scanResults)
    {
    }

    public void onScanFailed()
    {
    }

    public void onNetworkIdle(WifiInfo wifiInfo)
    {
    }

    public void onNetworkScanning(WifiInfo wifiInfo)
    {
    }

    public void onNetworkObtainingIp(WifiInfo wifiInfo)
    {
    }

    public void onNetworkDisconnected(WifiInfo wifiInfo)
    {
    }

    public void onNetworkConnected(WifiInfo wifiInfo)
    {
    }

    public void onNetworkFailed(WifiInfo wifiInfo)
    {
    }

    public void onWifiApEnabled()
    {
    }

    public void onWifiApEnabling()
    {
    }

    public void onWifiApDisabled()
    {
    }

    public void onWifiApDisabling()
    {
    }

    public void onWifiApFailed()
    {
    }

    public void onTimeout()
    {
    }

    public void setAutoUnregisterActions(int[] actions)
    {
        if (actions == null)
            throw new NullPointerException();
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.autoUnregisterActions = actions.clone();
        Arrays.sort(autoUnregisterActions);
    }

    public void ignoreInitialNetworkActions(boolean ignore)
    {
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        this.ignoreInitialNetworkActions = ignore;
    }

    /**
     * <p>注册并指定等待超时时间，超时时将回调onTimeout方法并自动反注册 <p>若在超时之前已经反注册，则将不再计算超时
     * 
     * @param timeout 单位为毫秒，设为0将永不超时
     */
    public void registerMe(int timeout)
    {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout could not be below zero.");
        if (!isUnregisteredCompletely)
            throw new IllegalStateException("please call this method after it has been unregistered completely.");
        IntentFilter wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (WIFI_AP_STATE_CHANGED_ACTION != null)
            wifiIntentFilter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        isDoneForAutoUnregisterActions = false;
        isUnregistered = false;
        isUnregisteredCompletely = false;
        context.registerReceiver(receiver, wifiIntentFilter, null, handler);
        curTimeout = timeout;
        if (curTimeout > 0)
        { // 为0时将永不超时
            new Timer().schedule(new TimerTask()
            {
                protected long timeCount = 0;

                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    timeCount = timeCount + 100;
                    if (isDoneForAutoUnregisterActions)
                    {
                        cancel();
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // TODO Auto-generated method stub
                                isUnregisteredCompletely = true;
                            }
                        });
                    } else if (timeCount >= curTimeout)
                    { // 已超时
                        cancel();
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // TODO Auto-generated method stub
                                if (unregisterMe())
                                {
                                    onTimeout();
                                }
                                isUnregisteredCompletely = true;
                            }
                        });
                    }
                }
            }, 100, 100);
        }
    }

    public boolean unregisterMe()
    {
        isInitialNetworkAction = true;
        prevNetworkDetailed = null;
        if (curTimeout > 0)
            isDoneForAutoUnregisterActions = true; // 置该值为true，使超时计时器能够尽快退出
        else
            isUnregisteredCompletely = true;
        isUnregistered = true;
        try
        {
            context.unregisterReceiver(receiver);
            return true;
        } catch (IllegalArgumentException e)
        {
            // 重复反注册会抛出该异常，如通过代码注册的receiver在当前activity销毁时会自动反注册，若再反注册，即会抛出该异常
            LogManager.logW(WifiCallback.class, "unregister receiver failed.", e);
            return false;
        }
    }

}
