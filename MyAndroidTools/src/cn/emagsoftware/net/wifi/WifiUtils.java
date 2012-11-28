package cn.emagsoftware.net.wifi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.Looper;
import cn.emagsoftware.net.NetManager;
import cn.emagsoftware.net.wifi.support.Wifi;
import cn.emagsoftware.telephony.ReflectHiddenFuncException;
import cn.emagsoftware.util.LogManager;

public final class WifiUtils
{

    private Context     context     = null;
    private WifiManager wifiManager = null;
    private WifiLock    wifiLock    = null;
    private Handler     handler     = new Handler(Looper.getMainLooper());

    public WifiUtils(Context context)
    {
        if (context == null)
            throw new NullPointerException();
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock("cn.emagsoftware.net.wifi.WifiUtils");
    }

    /**
     * <p>Wifi是否已打开
     * 
     * @return true,Wifi已打开;false,Wifi不存在或已关闭
     */
    public boolean isWifiEnabled()
    {
        return wifiManager.isWifiEnabled();
    }

    /**
     * <p>Wifi是否已连接
     * 
     * @return
     */
    public boolean isWifiConnected()
    {
        NetworkInfo wifiNetworkInfo = NetManager.getWifiNetworkInfo(context);
        if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected())
            return true;
        return false;
    }

    /**
     * <p>Wifi是否可用，即是否能请求互联网
     * 
     * @param timeout
     * @param tryTimes
     * @return
     */
    public boolean isWifiUseful(int timeout, int tryTimes)
    {
        return isWifiConnected() && NetManager.isNetUseful(timeout, tryTimes);
    }

    public WifiInfo getConnectionInfo()
    {
        return wifiManager.getConnectionInfo();
    }

    public void lockWifi()
    {
        wifiLock.acquire();
    }

    public void unlockWifi()
    {
        if (!wifiLock.isHeld())
        {
            wifiLock.release();
        }
    }

    public List<WifiConfiguration> getConfigurations()
    {
        return wifiManager.getConfiguredNetworks();
    }

    public WifiConfiguration getConfiguration(ScanResult sr, boolean compareSecurity)
    {
        return Wifi.getWifiConfiguration(wifiManager, sr, compareSecurity);
    }

    public WifiConfiguration getConfiguration(WifiConfiguration wc, boolean compareSecurity)
    {
        return Wifi.getWifiConfiguration(wifiManager, wc, compareSecurity);
    }

    public String getScanResultSecurity(ScanResult sr)
    {
        return Wifi.getScanResultSecurity(sr);
    }

    public void setupSecurity(WifiConfiguration wc, String security, String password)
    {
        Wifi.setupSecurity(wc, security, password);
    }

    /**
     * <p>获取Wifi信号的等级，共1,2,3,4,5五个等级，1表示信号最强
     * 
     * @param dbmLevel 一般在-95至-35之间
     * @return
     */
    public int getLevelGrade(int dbmLevel)
    {
        if (dbmLevel >= -47)
            return 1;
        else if (dbmLevel >= -59)
            return 2;
        else if (dbmLevel >= -71)
            return 3;
        else if (dbmLevel >= -83)
            return 4;
        else
            return 5;
    }

    public boolean disconnect()
    {
        return wifiManager.disconnect();
    }

    public WifiManager getWifiManager()
    {
        return wifiManager;
    }

    /**
     * <p>检测Wifi是否存在
     * 
     * @param callback
     * @param timeout 单位为毫秒，设为0将永不超时
     */
    public void checkWifiExist(final WifiCallback callback, final int timeout)
    {
        if (callback == null)
            return;
        if (isWifiEnabled())
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    callback.onWifiExist();
                }
            });
            return;
        }
        setWifiEnabled(true, new WifiCallback(context)
        {
            @Override
            public void onWifiEnabled()
            {
                // TODO Auto-generated method stub
                LogManager.logD(WifiUtils.class, "revert to previous wifi state...");
                setWifiEnabled(false, new WifiCallback(context)
                {
                    @Override
                    public void onWifiDisabled()
                    {
                        // TODO Auto-generated method stub
                        LogManager.logD(WifiUtils.class, "revert to previous wifi state successfully.");
                        callback.onWifiExist();
                    }

                    @Override
                    public void onError()
                    {
                        // TODO Auto-generated method stub
                        LogManager.logD(WifiUtils.class, "revert to previous wifi state unsuccessfully.");
                        callback.onWifiExist();
                    }

                    @Override
                    public void onTimeout()
                    {
                        // TODO Auto-generated method stub
                        LogManager.logD(WifiUtils.class, "revert to previous wifi state time out.");
                        callback.onWifiExist();
                    }
                }, timeout);
            }

            @Override
            public void onError()
            {
                // TODO Auto-generated method stub
                callback.onWifiNotExist();
            }

            @Override
            public void onTimeout()
            {
                // TODO Auto-generated method stub
                LogManager.logD(WifiUtils.class, "revert to previous wifi state...");
                setWifiEnabled(false, new WifiCallback(context)
                {
                    @Override
                    public void onWifiDisabled()
                    {
                        // TODO Auto-generated method stub
                        LogManager.logD(WifiUtils.class, "revert to previous wifi state successfully.");
                        callback.onTimeout();
                    }

                    @Override
                    public void onError()
                    {
                        // TODO Auto-generated method stub
                        LogManager.logD(WifiUtils.class, "revert to previous wifi state unsuccessfully.");
                        callback.onTimeout();
                    }

                    @Override
                    public void onTimeout()
                    {
                        // TODO Auto-generated method stub
                        LogManager.logD(WifiUtils.class, "revert to previous wifi state time out.");
                        callback.onTimeout();
                    }
                }, timeout);
            }
        }, timeout);
    }

    /**
     * <p>打开或关闭Wifi
     * 
     * @param enabled
     * @param callback
     * @param timeout 单位为毫秒，设为0将永不超时
     */
    public void setWifiEnabled(final boolean enabled, final WifiCallback callback, final int timeout)
    {
        if (!isWifiEnabled() && !enabled)
        { // 如果Wifi不存在或已关闭时执行关闭Wifi，在开机第一次调用将不会广播到Receiver，所以这里统一在外部回调
            if (callback != null)
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        callback.onWifiDisabled(); // 尽管如果Wifi不存在时，应回调onError，但判断Wifi是否存在比较耗时，所以这里统一回调onWifiDisabled
                    }
                });
            }
            return;
        }
        if (enabled)
        {
            try
            {
                int thisTimeout = timeout;
                if (callback == null)
                    thisTimeout = 20000;
                setWifiApEnabled(null, false, new WifiCallback(context)
                {
                    @Override
                    public void onWifiApDisabled()
                    {
                        // TODO Auto-generated method stub
                        super.onWifiApDisabled();
                        setWifiEnabledImpl(enabled, callback, timeout);
                    }

                    @Override
                    public void onError()
                    {
                        // TODO Auto-generated method stub
                        super.onError();
                        if (callback != null)
                            callback.onError();
                    }

                    @Override
                    public void onTimeout()
                    {
                        // TODO Auto-generated method stub
                        super.onTimeout();
                        if (callback != null)
                            callback.onTimeout();
                    }
                }, thisTimeout);
            } catch (ReflectHiddenFuncException e)
            {
                LogManager.logW(WifiUtils.class, "disable wifi ap failed.", e);
                setWifiEnabledImpl(enabled, callback, timeout);
            }
        } else
        {
            setWifiEnabledImpl(enabled, callback, timeout);
        }
    }

    private void setWifiEnabledImpl(boolean enabled, final WifiCallback callback, int timeout)
    {
        if (callback != null)
        {
            if (enabled)
                callback.setAutoUnregisterActions(new int[] { WifiCallback.ACTION_WIFI_ENABLED, WifiCallback.ACTION_ERROR });
            else
                callback.setAutoUnregisterActions(new int[] { WifiCallback.ACTION_WIFI_DISABLED, WifiCallback.ACTION_ERROR });
            callback.setTimeout(timeout);
            callback.registerMe();
        }
        boolean circs = wifiManager.setWifiEnabled(enabled);
        if (!circs)
            if (callback != null)
            {
                if (callback.unregisterMe())
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            callback.onError();
                        }
                    });
                }
            }
    }

    /**
     * <p>查找Wifi热点
     * 
     * @param callback
     * @param timeout 单位为毫秒，设为0将永不超时
     */
    public void startScan(final WifiCallback callback, int timeout)
    {
        if (!isWifiEnabled())
        { // 如果Wifi不存在或已关闭
            if (callback != null)
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        callback.onError();
                    }
                });
            }
            return;
        }
        if (callback != null)
        {
            callback.setAutoUnregisterActions(new int[] { WifiCallback.ACTION_ERROR, WifiCallback.ACTION_SCAN_RESULTS });
            callback.setTimeout(timeout);
            callback.registerMe();
        }
        boolean circs = wifiManager.startScan();
        if (!circs)
            if (callback != null)
            {
                if (callback.unregisterMe())
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            callback.onError();
                        }
                    });
                }
            }
    }

    /**
     * <p>连接到已保存的Wifi热点
     * 
     * @param wc
     * @param callback
     * @param timeout 单位为毫秒，设为0将永不超时
     */
    public void connect(WifiConfiguration wc, final WifiCallback callback, int timeout)
    {
        if (!isWifiEnabled())
        { // 如果Wifi不存在或已关闭
            if (callback != null)
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        callback.onError();
                    }
                });
            }
            return;
        }
        if (isWifiConnected())
        { // 如果要连接的Wifi热点已经连接，将直接回调onNetworkConnected方法，因为某些设备在指定热点已连接的情况下重新连接将不起作用，如SAMSUNG GT-I9008L
            final WifiInfo info = getConnectionInfo();
            String ssid = info == null ? null : info.getSSID();
            String bssid = info == null ? null : info.getBSSID();
            if (info != null && ssid != null && Wifi.convertToQuotedString(ssid).equals(wc.SSID) && bssid != null && (wc.BSSID == null || bssid.equals(wc.BSSID)))
            {
                if (callback != null)
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            callback.onNetworkConnected(info);
                        }
                    });
                }
                return;
            }
        }
        if (callback != null)
        {
            callback.setNetCallbackUntilNew(true);
            callback.setAutoUnregisterActions(new int[] { WifiCallback.ACTION_ERROR, WifiCallback.ACTION_NETWORK_CONNECTED, WifiCallback.ACTION_NETWORK_DISCONNECTED });
            callback.setTimeout(timeout);
            callback.registerMe();
        }
        boolean circs = Wifi.connectToConfiguredNetwork(context, wifiManager, wc, true);
        if (!circs)
            if (callback != null)
            {
                if (callback.unregisterMe())
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            callback.onError();
                        }
                    });
                }
            }
    }

    /**
     * <p>连接到查找到的Wifi热点
     * 
     * @param sr
     * @param password
     * @param callback
     * @param timeout 单位为毫秒，设为0将永不超时
     */
    public void connect(ScanResult sr, String password, final WifiCallback callback, int timeout)
    {
        if (!isWifiEnabled())
        { // 如果Wifi不存在或已关闭
            if (callback != null)
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        callback.onError();
                    }
                });
            }
            return;
        }
        if (isWifiConnected())
        { // 如果要连接的Wifi热点已经连接，将直接回调onNetworkConnected方法，因为某些设备在指定热点已连接的情况下重新连接将不起作用，如SAMSUNG GT-I9008L
            final WifiInfo info = getConnectionInfo();
            String ssid = info == null ? null : info.getSSID();
            String bssid = info == null ? null : info.getBSSID();
            if (info != null && ssid != null && ssid.equals(sr.SSID) && bssid != null && bssid.equals(sr.BSSID))
            {
                if (callback != null)
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            callback.onNetworkConnected(info);
                        }
                    });
                }
                return;
            }
        }
        WifiConfiguration old = getConfiguration(sr, false);
        if (old != null)
        {
            String security = getScanResultSecurity(sr);
            setupSecurity(old, security, password);
            if (!wifiManager.saveConfiguration())
            {
                if (callback != null)
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            callback.onError();
                        }
                    });
                }
                return;
            }
            connect(old, callback, timeout);
            return;
        }
        if (callback != null)
        {
            callback.setNetCallbackUntilNew(true);
            callback.setAutoUnregisterActions(new int[] { WifiCallback.ACTION_ERROR, WifiCallback.ACTION_NETWORK_CONNECTED, WifiCallback.ACTION_NETWORK_DISCONNECTED });
            callback.setTimeout(timeout);
            callback.registerMe();
        }
        boolean circs = Wifi.connectToNewNetwork(context, wifiManager, sr, password, Integer.MAX_VALUE);
        if (!circs)
            if (callback != null)
            {
                if (callback.unregisterMe())
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            callback.onError();
                        }
                    });
                }
            }
    }

    public boolean isWifiApEnabled() throws ReflectHiddenFuncException
    {
        try
        {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled", new Class[0]);
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager, new Object[0]);
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

    public WifiConfiguration getWifiApConfiguration() throws ReflectHiddenFuncException
    {
        try
        {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration", new Class[0]);
            method.setAccessible(true);
            return (WifiConfiguration) method.invoke(wifiManager, new Object[0]);
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

    public boolean setWifiApConfiguration(WifiConfiguration apConfig) throws ReflectHiddenFuncException
    {
        try
        {
            try
            {
                // 兼容htc等机器
                Method method = wifiManager.getClass().getMethod("setWifiApConfig", WifiConfiguration.class);
                method.setAccessible(true);
                return (Integer) method.invoke(wifiManager, apConfig) > 0;
            } catch (NoSuchMethodException e)
            {
                Method method = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
                method.setAccessible(true);
                return (Boolean) method.invoke(wifiManager, apConfig);
            }
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

    public void setWifiApEnabled(final WifiConfiguration apConfig, final boolean enabled, final WifiCallback callback, final int timeout) throws ReflectHiddenFuncException
    {
        if (!isWifiApEnabled() && !enabled)
        { // 如果Wifi Ap不存在或已关闭时执行关闭Wifi Ap，在开机第一次调用将不会广播到Receiver，所以这里统一在外部回调
            if (callback != null)
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        callback.onWifiApDisabled(); // 尽管如果Wifi Ap不存在时，应回调onError，但判断Wifi Ap是否存在比较麻烦，所以这里统一回调onWifiApDisabled
                    }
                });
            }
            return;
        }
        Method method = null;
        try
        {
            method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            method.setAccessible(true);
        } catch (NoSuchMethodException e)
        {
            throw new ReflectHiddenFuncException(e);
        }
        if (enabled)
        {
            int thisTimeout = timeout;
            if (callback == null)
                thisTimeout = 20000;
            final Method methodPoint = method;
            setWifiEnabled(false, new WifiCallback(context)
            {
                @Override
                public void onWifiDisabled()
                {
                    // TODO Auto-generated method stub
                    super.onWifiDisabled();
                    try
                    {
                        setWifiApEnabledImpl(methodPoint, apConfig, enabled, callback, timeout);
                    } catch (ReflectHiddenFuncException e)
                    {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onError()
                {
                    // TODO Auto-generated method stub
                    super.onError();
                    if (callback != null)
                        callback.onError();
                }

                @Override
                public void onTimeout()
                {
                    // TODO Auto-generated method stub
                    super.onTimeout();
                    if (callback != null)
                        callback.onTimeout();
                }
            }, thisTimeout);
        } else
        {
            setWifiApEnabledImpl(method, apConfig, enabled, callback, timeout);
        }
    }

    private void setWifiApEnabledImpl(final Method method, final WifiConfiguration apConfig, final boolean enabled, final WifiCallback callback, final int timeout) throws ReflectHiddenFuncException
    {
        if (isWifiApEnabled() && enabled && apConfig != null)
        {
            int thisTimeout = timeout;
            if (callback == null)
                thisTimeout = 20000;
            setWifiApEnabledImpl(method, null, false, new WifiCallback(context)
            {
                @Override
                public void onWifiApDisabled()
                {
                    // TODO Auto-generated method stub
                    super.onWifiApDisabled();
                    try
                    {
                        setWifiApEnabledImpl(method, apConfig, enabled, callback, timeout);
                    } catch (ReflectHiddenFuncException e)
                    {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onError()
                {
                    // TODO Auto-generated method stub
                    super.onError();
                    if (callback != null)
                        callback.onError();
                }

                @Override
                public void onTimeout()
                {
                    // TODO Auto-generated method stub
                    super.onTimeout();
                    if (callback != null)
                        callback.onTimeout();
                }
            }, thisTimeout);
            return;
        }
        if (callback != null)
        {
            if (enabled)
                callback.setAutoUnregisterActions(new int[] { WifiCallback.ACTION_WIFI_AP_ENABLED, WifiCallback.ACTION_ERROR });
            else
                callback.setAutoUnregisterActions(new int[] { WifiCallback.ACTION_WIFI_AP_DISABLED, WifiCallback.ACTION_ERROR });
            callback.setTimeout(timeout);
            callback.registerMe();
        }
        try
        {
            boolean circs = (Boolean) method.invoke(wifiManager, apConfig, enabled);
            if (!circs)
                if (callback != null)
                {
                    if (callback.unregisterMe())
                    {
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // TODO Auto-generated method stub
                                callback.onError();
                            }
                        });
                    }
                }
        } catch (InvocationTargetException e)
        {
            if (callback != null)
                callback.unregisterMe();
            throw new ReflectHiddenFuncException(e);
        } catch (IllegalAccessException e)
        {
            if (callback != null)
                callback.unregisterMe();
            throw new ReflectHiddenFuncException(e);
        }
    }

}
