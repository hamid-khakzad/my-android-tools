package cn.emagsoftware.net.wifi.direct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import cn.emagsoftware.net.wifi.WifiCallback;
import cn.emagsoftware.net.wifi.WifiUtils;
import cn.emagsoftware.telephony.ReflectHiddenFuncException;
import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.StringUtilities;

public class User
{

    private static final int  WIFI_TIMEOUT    = 20000;
    private static final int          CONNECT_TIMEOUT  = 20000;
    static final int MAX_NAME_LENGTH = 6;

    private static final int  LISTENING_PORT  = 7001;
    static final int  LISTENING_PORT_UDP  = 7002;
    private static final int  LISTENING_PORT_UDP_OWNER = 7003;

    private String            name            = null;
    private RemoteCallback    callback        = null;
    private PowerManager.WakeLock wakeLock = null;
    private Selector          selector        = null;

    private WifiConfiguration preApConfig     = null;
    private int               preWifiStaticIp = -1;
    private boolean           preWifiEnabled  = false;

    LinkedList<RemoteUser> scanUsers = new LinkedList<RemoteUser>();

    private Handler           handler         = new Handler();

    public User(String name, RemoteCallback callback) throws NameOutOfRangeException, IOException
    {
        if (name == null || callback == null)
            throw new NullPointerException();
        if (name.length() > MAX_NAME_LENGTH)
            throw new NameOutOfRangeException("name length can not great than " + MAX_NAME_LENGTH + ".");
        this.name = name;
        this.callback = callback;
        PowerManager powerManager = (PowerManager) callback.appContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,getClass().getName());
        wakeLock.acquire();
        try
        {
            selector = Selector.open();
        }catch (IOException e)
        {
            if(wakeLock.isHeld())
                wakeLock.release();
            throw e;
        }
        try
        {
            callback.bindSelector(selector);
        }catch (RuntimeException e)
        {
            if(wakeLock.isHeld())
                wakeLock.release();
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
                if(wakeLock.isHeld())
                    wakeLock.release();
                selector.close();
            }finally
            {
                if(serverChannel != null)
                    serverChannel.close();
            }
            throw e;
        }
        SelectionKey key = null;
        DatagramChannel channel = null;
        try
        {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(LISTENING_PORT_UDP));
            key = channel.register(selector,SelectionKey.OP_READ,new Object[]{this});
        }catch (IOException e)
        {
            try
            {
                if(wakeLock.isHeld())
                    wakeLock.release();
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
        SelectionKey ownerKey = null;
        DatagramChannel ownerChannel = null;
        try
        {
            ownerChannel = DatagramChannel.open();
            ownerChannel.configureBlocking(false);
            ownerChannel.socket().bind(new InetSocketAddress(LISTENING_PORT_UDP_OWNER));
            ownerKey = ownerChannel.register(selector,SelectionKey.OP_READ,new Object[]{this});
        }catch (IOException e)
        {
            try
            {
                if(wakeLock.isHeld())
                    wakeLock.release();
                selector.close();
            }finally
            {
                try
                {
                    serverKey.cancel();
                    serverChannel.close();
                }finally
                {
                    try
                    {
                        key.cancel();
                        channel.close();
                    }finally
                    {
                        if(ownerChannel != null)
                            ownerChannel.close();
                    }
                }
            }
            throw e;
        }
        final SelectionKey ownerKeyPoint = ownerKey;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true)
                {
                    try
                    {
                        ownerKeyPoint.interestOps(SelectionKey.OP_WRITE);
                    }catch (CancelledKeyException e)
                    {
                        break;
                    }
                    long curTime = System.currentTimeMillis();
                    synchronized (scanUsers)
                    {
                        Iterator<RemoteUser> users = scanUsers.iterator();
                        while (users.hasNext())
                        {
                            RemoteUser curUser = users.next();
                            if(curTime - curUser.getRefreshTime() > 20000)
                                users.remove();
                        }
                    }
                    try
                    {
                        Thread.sleep(4000);
                    }catch (InterruptedException e)
                    {
                    }
                }
            }
        }).start();
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

    public void openDirectAp(Context context, final OpenDirectApCallback callback)
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

    public void closeDirectAp(final Context context, final CloseDirectApCallback callback)
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

    public void scanDirectAps(final Context context, final ScanDirectApsCallback callback)
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
                        List<DirectAp> callbackVal = new ArrayList<DirectAp>();
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
                                DirectAp ap = new DirectAp(name);
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

    public void connectToDirectAp(final Context context, final DirectAp ap, final ConnectToDirectApCallback callback)
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
                        RemoteUser user = new RemoteUser(ap.getName());
                        int apIp = wifiUtils.getWifiManager().getDhcpInfo().serverAddress;
                        String apIpStr = String.format("%d.%d.%d.%d", (apIp & 0xff), (apIp >> 8 & 0xff), (apIp >> 16 & 0xff), (apIp >> 24 & 0xff));
                        user.setIp(apIpStr);
                        user.state = 1;
                        callback.onConnected(ap,user);
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

    public void disconnectDirectAp(Context context, DirectAp ap, DisconnectDirectApCallback callback)
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

    public void scanUsers(final ScanUsersCallback scanCallback)
    {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LinkedList<RemoteUser> users = null;
                synchronized (scanUsers)
                {
                    users = (LinkedList<RemoteUser>)scanUsers.clone();
                }
                scanCallback.onScanned(users);
            }
        },6000);
    }

    public void connectToUser(final RemoteUser user)
    {
        callback.post(new Runnable() {
            @Override
            public void run() {
                if(user.state != 1)
                    return;
                user.state = 0;
                SocketChannel sc = null;
                try
                {
                    sc = SocketChannel.open();
                    sc.configureBlocking(false);
                    sc.connect(new InetSocketAddress(user.getIp(), LISTENING_PORT));
                    final SelectionKey key = sc.register(selector, SelectionKey.OP_CONNECT, new Object[] { user, "connect", User.this });
                    final SocketChannel scPoint = sc;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            callback.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(user.state == 0)
                                    {
                                        try
                                        {
                                            key.cancel();
                                            scPoint.close();
                                        }catch (IOException e)
                                        {
                                            LogManager.logD(User.class,"close socket channel failed.",e);
                                        }
                                        user.state = 1;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onConnectedFailed(user,new SocketTimeoutException("connect time out."));
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    },CONNECT_TIMEOUT);
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
                    user.state = 1;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onConnectedFailed(user, e);
                        }
                    });
                }
            }
        });
    }

    public void disconnectUser(final RemoteUser user)
    {
        callback.post(new Runnable() {
            @Override
            public void run() {
                if(user.state != 2)
                    return;
                Iterator<TransferEntity> transfers = user.getTransfers().iterator();
                while(transfers.hasNext())
                {
                    try
                    {
                        final TransferEntity transfer = transfers.next();
                        SelectionKey key = transfer.getSelectionKey();
                        key.cancel();
                        key.channel().close();
                        transfers.remove();
                        transfer.state = 1;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onTransferFailed(transfer,new RuntimeException("transfer is cancelled."));
                            }
                        });
                    }catch (IOException e)
                    {
                        LogManager.logE(User.class,"close socket channel failed.",e);
                    }
                }
                try
                {
                    SelectionKey userKey = user.getKey();
                    userKey.cancel();
                    userKey.channel().close();
                    user.state = 1;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onDisconnected(user);
                        }
                    });
                }catch (final IOException e)
                {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onDisconnectedFailed(user,e);
                        }
                    });
                }
            }
        });
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

    public void sendTransfer(final RemoteUser user, final File file, final String extraDescription)
    {
        callback.post(new Runnable() {
            @Override
            public void run() {
                final TransferEntity transfer = new TransferEntity();
                transfer.setRemoteUser(user);
                transfer.setSendPath(file.getAbsolutePath());
                transfer.setSize(file.length());
                transfer.setSender(true);
                transfer.setExtraDescription(extraDescription);
                transfer.state = 0;
                user.getTransfers().add(transfer);
                SocketChannel sc = null;
                try
                {
                    if(user.state != 2)
                        throw new IOException("the input user has not been connected already.");
                    if (!file.isFile())
                        throw new FileNotFoundException("the input file is invalid.");
                    sc = SocketChannel.open();
                    sc.configureBlocking(false);
                    sc.connect(new InetSocketAddress(user.getIp(), LISTENING_PORT));
                    SelectionKey key = sc.register(selector, SelectionKey.OP_CONNECT, new Object[] { user, "transfer_connect", transfer });
                    transfer.setSelectionKey(key);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onTransferProgress(transfer, 0);
                        }
                    });
                } catch (final IOException e)
                {
                    final SocketChannel scPoint = sc;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onTransferProgress(transfer, 0); // 协议规定为0时一定要通知到
                            callback.post(new Runnable() {
                                @Override
                                public void run() {
                                    try
                                    {
                                        transfer.state = 1;
                                        user.getTransfers().remove(transfer);
                                        if (scPoint != null)
                                            scPoint.close();
                                    } catch (IOException e)
                                    {
                                        LogManager.logE(User.class, "close socket channel failed.", e);
                                    }
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onTransferFailed(transfer, e);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    public void cancelTransfer(final TransferEntity transfer)
    {
        callback.post(new Runnable() {
            @Override
            public void run() {
                SelectionKey key = transfer.getSelectionKey();
                if(key == null) //sendTransfer强制要求通知进度为0导致的一种情况，这种情况随后会紧跟onTransferFailed，故可以忽略；若onTransferFailed已经执行，则仍可忽略，因为这符合已取消的被取消时无回调的原则
                    return;
                if(transfer.state == 1)
                    return;
                try
                {
                    transfer.state = 1;
                    transfer.getRemoteUser().getTransfers().remove(transfer);
                    key.cancel();
                    key.channel().close();
                }catch (IOException e)
                {
                    LogManager.logE(User.class, "close socket channel failed.", e);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onTransferFailed(transfer,new RuntimeException("transfer is cancelled."));
                    }
                });
            }
        });
    }

    public void close(final Context context, final CloseCallback callback)
    {
        this.callback.post(new Runnable() {
            @Override
            public void run() {
                Exception firstExcep = null;
                try
                {
                    Set<SelectionKey> skeys = selector.keys();
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
                final Exception firstExcepPoint = firstExcep;
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(wakeLock.isHeld())
                            wakeLock.release();
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
                        closeDirectAp(context, new CloseDirectApCallback() {
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
        });
    }

}
