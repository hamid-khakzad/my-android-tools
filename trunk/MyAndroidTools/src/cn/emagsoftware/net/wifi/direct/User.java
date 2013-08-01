package cn.emagsoftware.net.wifi.direct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import cn.emagsoftware.net.wifi.WifiCallback;
import cn.emagsoftware.net.wifi.WifiUtils;
import cn.emagsoftware.telephony.ReflectHiddenFuncException;
import cn.emagsoftware.telephony.TelephonyMgr;
import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.StringUtilities;

public class User
{

    private static final int  WIFI_TIMEOUT    = 20000;
    static final int          SOCKET_TIMEOUT  = 20000;
    static final int MAX_NAME_LENGTH = 6;

    private static final int  LISTENING_PORT  = 7001;
    static final int  LISTENING_PORT_UDP  = 7002;

    private String            name            = null;
    private RemoteCallback    callback        = null;
    private WifiManager.WifiLock wifiLock = null;
    private Selector          selector        = null;

    private WifiConfiguration preApConfig     = null;
    private int               preWifiStaticIp = -1;
    private boolean           preWifiEnabled  = false;

    private int scanToken = -1;
    Map<Integer,List<RemoteUser>> scanUsers = new HashMap<Integer, List<RemoteUser>>();

    private Handler           handler         = new Handler();

    public User(String name, RemoteCallback callback) throws NameOutOfRangeException, IOException
    {
        if (name == null || callback == null)
            throw new NullPointerException();
        if (name.length() > MAX_NAME_LENGTH)
            throw new NameOutOfRangeException("name length can not great than " + MAX_NAME_LENGTH + ".");
        this.name = name;
        this.callback = callback;
        if(TelephonyMgr.getSDKVersion() < 12)
        {
            wifiLock = callback.wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL,getClass().getName());
        }else
        {
            int lockType;
            try
            {
                Field field = WifiManager.class.getDeclaredField("WIFI_MODE_FULL_HIGH_PERF");
                field.setAccessible(true);
                lockType = field.getInt(null);
            }catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            wifiLock = callback.wifiManager.createWifiLock(lockType,getClass().getName());
        }
        wifiLock.acquire();
        try
        {
            selector = Selector.open();
        }catch (IOException e)
        {
            if(wifiLock.isHeld())
                wifiLock.release();
            throw e;
        }
        try
        {
            callback.bindSelector(selector);
        }catch (RuntimeException e)
        {
            if(wifiLock.isHeld())
                wifiLock.release();
            selector.close();
            throw e;
        }
        SelectionKey serverKey = null;
        ServerSocketChannel serverChannel = null;
        try
        {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(LISTENING_PORT));
            serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        }catch (IOException e)
        {
            try
            {
                if(wifiLock.isHeld())
                    wifiLock.release();
                selector.close();
            }finally
            {
                if(serverChannel != null)
                    serverChannel.close();
            }
            throw e;
        }
        DatagramChannel channel = null;
        try
        {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(LISTENING_PORT_UDP));
            channel.register(selector,SelectionKey.OP_READ,new Object[]{this});
        }catch (IOException e)
        {
            try
            {
                if(wifiLock.isHeld())
                    wifiLock.release();
                selector.close();
            }finally
            {
                try
                {
                    serverKey.cancel();
                    serverChannel.close();
                }finally
                {
                    if(channel != null)
                        channel.close();
                }
            }
            throw e;
        }
        new Thread(callback).start();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name) throws NameOutOfRangeException, StateNotAllowException
    {
        if (name == null)
            throw new NullPointerException();
        if (name.length() > 6)
            throw new NameOutOfRangeException("name length can not great than 6.");
        if (preApConfig != null)
            throw new StateNotAllowException("can not set name while listening.");
        this.name = name;
    }

    public void openAp(Context context, final OpenApCallback callback)
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        final boolean isFirst = preApConfig == null;
        try
        {
            if (isFirst)
            {
                preApConfig = wifiUtils.getWifiApConfiguration();
                try
                {
                    preWifiStaticIp = Settings.System.getInt(context.getContentResolver(), "wifi_static_ip");
                } catch (SettingNotFoundException e)
                {
                }
                preWifiEnabled = wifiUtils.isWifiEnabled();
            }
            wifiUtils.setWifiApEnabled(createDirectApConfig(context), true, new WifiCallback(context)
            {
                @Override
                public void onWifiApEnabled()
                {
                    super.onWifiApEnabled();
                    callback.onOpen();
                }

                @Override
                public void onTimeout()
                {
                    super.onTimeout();
                    if (isFirst)
                    {
                        preApConfig = null;
                        preWifiStaticIp = -1;
                        preWifiEnabled = false;
                    }
                    callback.onError(new RuntimeException("open ap failed by 'WifiCallback.onTimeout()'."));
                }

                @Override
                public void onWifiApFailed()
                {
                    super.onWifiApFailed();
                    if (isFirst)
                    {
                        preApConfig = null;
                        preWifiStaticIp = -1;
                        preWifiEnabled = false;
                    }
                    callback.onError(new RuntimeException("open ap failed by 'WifiCallback.onWifiApFailed()'."));
                }
            }, WIFI_TIMEOUT);
        } catch (ReflectHiddenFuncException e)
        {
            if (isFirst)
            {
                preApConfig = null;
                preWifiStaticIp = -1;
                preWifiEnabled = false;
            }
            callback.onError(e);
        }
    }

    private WifiConfiguration createDirectApConfig(Context context) throws ReflectHiddenFuncException
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        WifiConfiguration apconfig = new WifiConfiguration();
        String apName = null;
        try
        {
            apName = "MYFY" + StringUtilities.bytesToHexString(name.getBytes("UTF-16"));
        } catch (UnsupportedEncodingException e)
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
        Field apProfileField = null;
        try
        {
            apProfileField = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
        } catch (NoSuchFieldException e)
        {
        }
        if (apProfileField != null)
        {
            try
            {
                apProfileField.setAccessible(true);
                Object apProfile = apProfileField.get(apconfig);
                if (apProfile != null)
                {
                    Field ssidField = apProfile.getClass().getField("SSID");
                    ssidField.setAccessible(true);
                    ssidField.set(apProfile, apconfig.SSID);
                    Field bssidField = apProfile.getClass().getField("BSSID");
                    bssidField.setAccessible(true);
                    bssidField.set(apProfile, apconfig.BSSID);
                    Field typeField = apProfile.getClass().getField("secureType");
                    typeField.setAccessible(true);
                    typeField.set(apProfile, "open");
                    Field dhcpField = apProfile.getClass().getField("dhcpEnable");
                    dhcpField.setAccessible(true);
                    dhcpField.set(apProfile, 1);
                }
            } catch (IllegalAccessException e)
            {
                throw new ReflectHiddenFuncException(e);
            } catch (NoSuchFieldException e)
            {
                throw new ReflectHiddenFuncException(e);
            }
        }
        return apconfig;
    }

    public void closeAp(final Context context, final CloseApCallback callback)
    {
        final WifiUtils wifiUtils = new WifiUtils(context);
        try
        {
            wifiUtils.setWifiApEnabled(null, false, new WifiCallback(context)
            {
                @Override
                public void onWifiApDisabled()
                {
                    super.onWifiApDisabled();
                    if (preApConfig != null)
                    {
                        try
                        {
                            wifiUtils.setWifiApConfiguration(preApConfig);
                        } catch (ReflectHiddenFuncException e)
                        {
                            LogManager.logE(User.class, "restore ap config failed.", e);
                        }
                        if (preWifiStaticIp != -1)
                            Settings.System.putInt(context.getContentResolver(), "wifi_static_ip", preWifiStaticIp);
                        if (preWifiEnabled)
                            wifiUtils.setWifiEnabled(true, null, WIFI_TIMEOUT);
                        preApConfig = null;
                        preWifiStaticIp = -1;
                        preWifiEnabled = false;
                    }
                    callback.onClosed();
                }

                @Override
                public void onTimeout()
                {
                    super.onTimeout();
                    callback.onError(new RuntimeException("close ap failed by 'WifiCallback.onTimeout()'."));
                }

                @Override
                public void onWifiApFailed()
                {
                    super.onWifiApFailed();
                    callback.onError(new RuntimeException("close ap failed by 'WifiCallback.onWifiApFailed()'."));
                }
            }, WIFI_TIMEOUT);
        }catch (ReflectHiddenFuncException e)
        {
            callback.onError(e);
        }
    }

    public void scanRemoteAps(final Context context, final ScanRemoteApsCallback callback)
    {
        final WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.setWifiEnabled(true, new WifiCallback(context)
        {
            @Override
            public void onWifiEnabled()
            {
                super.onWifiEnabled();
                wifiUtils.startScan(new WifiCallback(context)
                {
                    @Override
                    public void onScanResults(List<ScanResult> scanResults)
                    {
                        super.onScanResults(scanResults);
                        List<RemoteAp> callbackVal = new ArrayList<RemoteAp>();
                        for (ScanResult result : scanResults)
                        {
                            String ssid = result.SSID;
                            String name = null;
                            if (ssid.startsWith("MYFY"))
                            {
                                String userStr = ssid.substring(4);
                                try
                                {
                                    name = new String(StringUtilities.hexStringToBytes(userStr), "UTF-16");
                                } catch (Exception e)
                                {
                                    LogManager.logW(User.class, "decode scanned ap name failed.", e);
                                }
                            }
                            if (name != null)
                            {
                                RemoteAp ap = new RemoteAp(name);
                                ap.setScanResult(result);
                                callbackVal.add(ap);
                            }
                        }
                        callback.onScanned(callbackVal);
                    }

                    @Override
                    public void onTimeout()
                    {
                        super.onTimeout();
                        callback.onError();
                    }

                    @Override
                    public void onScanFailed()
                    {
                        super.onScanFailed();
                        callback.onError();
                    }
                }, WIFI_TIMEOUT);
            }

            @Override
            public void onWifiFailed()
            {
                super.onWifiFailed();
                callback.onError();
            }

            @Override
            public void onTimeout()
            {
                super.onTimeout();
                callback.onError();
            }
        }, WIFI_TIMEOUT);
    }

    public void connectToRemoteAp(final Context context, final RemoteAp ap, final ConnectToRemoteApCallback callback)
    {
        final WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.setWifiEnabled(true, new WifiCallback(context)
        {
            @Override
            public void onWifiEnabled()
            {
                super.onWifiEnabled();
                wifiUtils.connect(ap.getScanResult(), null, new WifiCallback(context)
                {
                    @Override
                    public void onNetworkConnected(WifiInfo wifiInfo)
                    {
                        super.onNetworkConnected(wifiInfo);
                        callback.onConnected(ap);
                    }

                    @Override
                    public void onNetworkDisconnected(WifiInfo wifiInfo)
                    {
                        super.onNetworkDisconnected(wifiInfo);
                        callback.onError(ap);
                    }

                    @Override
                    public void onTimeout()
                    {
                        super.onTimeout();
                        callback.onError(ap);
                    }

                    @Override
                    public void onNetworkFailed(WifiInfo wifiInfo)
                    {
                        super.onNetworkFailed(wifiInfo);
                        callback.onError(ap);
                    }
                }, WIFI_TIMEOUT);
            }

            @Override
            public void onWifiFailed()
            {
                super.onWifiFailed();
                callback.onError(ap);
            }

            @Override
            public void onTimeout()
            {
                super.onTimeout();
                callback.onError(ap);
            }
        }, WIFI_TIMEOUT);
    }

    public void disconnectRemoteAp(Context context, RemoteAp ap, DisconnectRemoteApCallback callback)
    {
        try
        {
            ScanResult sr = ap.getScanResult();
            WifiUtils wifiUtils = new WifiUtils(context);
            List<WifiConfiguration> wcList = wifiUtils.getConfiguration(sr, true);
            if (wcList != null && wcList.size() != 0)
            {
                WifiManager wm = wifiUtils.getWifiManager();
                wm.removeNetwork(wcList.get(0).networkId);
                wm.saveConfiguration();
            }
        }catch (RuntimeException e)
        {
            callback.onError(ap,e);
            return;
        }
        callback.onDisconnected(ap);
    }

    public void scanUsers(Context context, final ScanUsersCallback scanCallback)
    {
        try
        {
            if(new WifiUtils(context).isWifiApEnabled())
            {
                scanCallback.onError(new RuntimeException("can not support multi networks(mobile and AP),in this case,you want to scan in AP,but it scans in mobile factly"));
                return;
            }
        }catch (ReflectHiddenFuncException e)
        {
            scanCallback.onError(e);
            return;
        }
        if(scanToken == Integer.MAX_VALUE)
            scanToken = -1;
        final int curToken = ++scanToken;
        final LinkedList<RemoteUser> users = new LinkedList<RemoteUser>();
        scanUsers.put(curToken,users);
        DatagramChannel dc = null;
        try
        {
            dc = DatagramChannel.open();
            dc.configureBlocking(false);
            dc.socket().setBroadcast(true);
            callback.setSleepForConflict(true);
            try
            {
                dc.register(selector, SelectionKey.OP_WRITE, new Object[] { curToken });
            } finally
            {
                callback.setSleepForConflict(false);
            }
        }catch (IOException e)
        {
            try
            {
                if (dc != null)
                    dc.close();
            } catch (IOException e1)
            {
                LogManager.logE(User.class, "close datagram channel failed.", e1);
            }
            scanUsers.remove(curToken);
            scanCallback.onError(e);
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LinkedList<RemoteUser> copy = null;
                synchronized (User.this)
                {
                    scanUsers.remove(curToken);
                    copy = (LinkedList<RemoteUser>)users.clone();
                }
                scanCallback.onScanned(copy);
            }
        },4000);
    }

    public void connectToUser(final RemoteUser user)
    {
        SocketChannel sc = null;
        try
        {
            sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress(user.getIp(), LISTENING_PORT));
            callback.setSleepForConflict(true);
            try
            {
                sc.register(selector, SelectionKey.OP_CONNECT, new Object[] { user, "connect", this });
            } finally
            {
                callback.setSleepForConflict(false);
            }
        } catch (final IOException e)
        {
            try
            {
                if (sc != null)
                    sc.close();
            } catch (IOException e1)
            {
                LogManager.logE(User.class, "close socket channel failed.", e1);
            }
            callback.onConnectedFailed(user, e);
        }
    }

    public void disconnectUser(RemoteUser user)
    {
        try
        {
            user.close();
            callback.onDisconnected(user);
        } catch (IOException e)
        {
            callback.onDisconnectedFailed(user, e);
        }
    }

    public void sendTransferRequest(RemoteUser user, String description)
    {
        SelectionKey key = user.getKey();
        if (key == null)
            throw new IllegalStateException("the input user has not been connected already.");
        Object[] objs = (Object[]) key.attachment();
        key.attach(new Object[] { objs[0], "transfer_request", description });
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void replyTransferRequest(RemoteUser user, boolean allow, String description)
    {
        SelectionKey key = user.getKey();
        if (key == null)
            throw new IllegalStateException("the input user has not been connected already.");
        Object[] objs = (Object[]) key.attachment();
        key.attach(new Object[] { objs[0], "transfer_reply", allow, description });
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void sendTransfer(RemoteUser user, File file, String extraDescription)
    {
        if (user.getKey() == null)
            throw new IllegalStateException("the input user has not been connected already.");
        final TransferEntity transfer = new TransferEntity();
        transfer.setRemoteUser(user);
        transfer.setSendPath(file.getAbsolutePath());
        transfer.setSize(file.length());
        transfer.setSender(true);
        transfer.setExtraDescription(extraDescription);
        user.addTransfer(transfer);
        callback.onTransferProgress(transfer, 0);
        String ip = user.getIp();
        SocketChannel sc = null;
        try
        {
            if (!file.isFile())
                throw new FileNotFoundException("the input file is invalid.");
            sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress(ip, LISTENING_PORT));
            sc.socket().setSoTimeout(SOCKET_TIMEOUT);
            callback.setSleepForConflict(true);
            try
            {
                sc.register(selector, SelectionKey.OP_CONNECT, new Object[] { user, "transfer_connect", transfer });
            } finally
            {
                callback.setSleepForConflict(false);
            }
        } catch (final IOException e)
        {
            try
            {
                user.removeTransfer(transfer);
                if (sc != null)
                    sc.close();
            } catch (IOException e1)
            {
                LogManager.logE(User.class, "close socket channel failed.", e1);
            }
            callback.onTransferFailed(transfer, e);
        }
    }

    public void cancelTransfer(TransferEntity transfer) throws IOException
    {
        transfer.close();
    }

    public void close(final Context context, final CloseCallback callback)
    {
        Exception firstExcep = null;
        Set<SelectionKey> skeys = null;
        try
        {
            this.callback.setSleepForConflict(true);
            try
            {
                skeys = selector.keys();
            } finally
            {
                this.callback.setSleepForConflict(false);
            }
        } catch (ClosedSelectorException e)
        {
            if (firstExcep == null)
                firstExcep = e;
        }
        try
        {
            selector.close();
        } catch (IOException e)
        {
            if (firstExcep == null)
                firstExcep = e;
        }
        if (skeys != null)
        {
            for (SelectionKey curKey : skeys)
            {
                try
                {
                    curKey.cancel();
                    curKey.channel().close();
                } catch (IOException e)
                {
                    if (firstExcep == null)
                        firstExcep = e;
                }
            }
        }
        final Exception firstExcepPoint = firstExcep;
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(wifiLock.isHeld())
                    wifiLock.release();
                WifiUtils wifiUtils = new WifiUtils(context);
                WifiManager wm = wifiUtils.getWifiManager();
                List<WifiConfiguration> wcs = wifiUtils.getConfigurations();
                if (wcs != null)
                {
                    for (WifiConfiguration wc : wcs)
                    {
                        String ssid = wc.SSID;
                        if (ssid != null && ssid.startsWith("\"MYFY"))
                        {
                            wm.removeNetwork(wc.networkId);
                        }
                    }
                    wm.saveConfiguration();
                }
                closeAp(context,new CloseApCallback() {
                    @Override
                    public void onClosed() {
                        if (firstExcepPoint == null)
                            callback.onClosed();
                        else
                            callback.onError(firstExcepPoint);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (firstExcepPoint == null)
                            callback.onError(e);
                        else
                            callback.onError(firstExcepPoint);
                    }
                });
            }
        }, 1000);
    }

}
