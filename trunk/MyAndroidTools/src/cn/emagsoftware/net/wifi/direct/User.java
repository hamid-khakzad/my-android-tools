package cn.emagsoftware.net.wifi.direct;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import cn.emagsoftware.net.wifi.WifiCallback;
import cn.emagsoftware.net.wifi.WifiUtils;
import cn.emagsoftware.telephony.ReflectHiddenFuncException;
import cn.emagsoftware.util.Base64;
import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.MathUtilities;

public class User
{

    private static final int    WIFI_TIMEOUT    = 20000;
    static final int            SOCKET_TIMEOUT  = 20000;

    private static final String ACTION_CHARSET  = "UTF-8";
    private static final int    LISTENING_PORT  = 7001;

    private String              name            = null;

    private WifiConfiguration   preApConfig     = null;
    private int                 preWifiStaticIp = -1;
    private boolean             preWifiEnabled  = false;

    private Selector            selector        = null;
    private RemoteCallback      callback        = null;

    private SelectionKey        listeningKey    = null;

    private Handler             handler         = new Handler(Looper.getMainLooper());

    public User(String name, RemoteCallback callback) throws IOException
    {
        if (name == null)
            throw new NullPointerException();
        this.name = name;
        selector = Selector.open();
        callback.bindSelector(selector);
        this.callback = callback;
        new Thread(callback).start();
    }

    public String getName()
    {
        return name;
    }

    public void listening(Context context, final ListeningCallback callback)
    {
        try
        {
            openAp(context, new WifiCallback(context)
            {
                @Override
                public void onWifiApEnabled()
                {
                    // TODO Auto-generated method stub
                    super.onWifiApEnabled();
                    try
                    {
                        acceptIfNecessary();
                        callback.onListening();
                    } catch (IOException e)
                    {
                        callback.onError(e);
                    }
                }

                @Override
                public void onTimeout()
                {
                    // TODO Auto-generated method stub
                    super.onTimeout();
                    callback.onError(new RuntimeException("open ap failed by 'WifiCallback.onTimeout()'."));
                }

                @Override
                public void onError()
                {
                    // TODO Auto-generated method stub
                    super.onError();
                    callback.onError(new RuntimeException("open ap failed by 'WifiCallback.onError()'."));
                }
            }, WIFI_TIMEOUT);
        } catch (final ReflectHiddenFuncException e)
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    callback.onError(e);
                }
            });
        }
    }

    private void openAp(Context context, WifiCallback callback, int timeout) throws ReflectHiddenFuncException
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        if (preApConfig == null)
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
        wifiUtils.setWifiApEnabled(createDirectApConfig(context), true, callback, timeout);
    }

    private WifiConfiguration createDirectApConfig(Context context)
    {
        WifiUtils wifiUtils = new WifiUtils(context);
        WifiConfiguration apconfig = new WifiConfiguration();
        String apName = null;
        try
        {
            apName = "GHFY_" + Base64.encode(name.getBytes(ACTION_CHARSET)) + "_" + MathUtilities.Random(10000);
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
        return apconfig;
    }

    void acceptIfNecessary() throws IOException
    {
        if (listeningKey != null)
            return;
        ServerSocketChannel serverChannel = null;
        try
        {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(LISTENING_PORT));
            callback.setSleepForConflict(true);
            try
            {
                listeningKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            } finally
            {
                callback.setSleepForConflict(false);
            }
        } catch (IOException e)
        {
            try
            {
                if (serverChannel != null)
                    serverChannel.close();
            } catch (IOException e1)
            {
                LogManager.logE(User.class, "close server socket channel failed.", e1);
            }
            throw e;
        }
    }

    private void closeAp(Context context, final CloseApCallback callback) throws ReflectHiddenFuncException
    {
        final WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.setWifiApEnabled(null, false, new WifiCallback(context)
        {
            @Override
            public void onWifiApDisabled()
            {
                // TODO Auto-generated method stub
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
                // TODO Auto-generated method stub
                super.onTimeout();
                callback.onError();
            }

            @Override
            public void onError()
            {
                // TODO Auto-generated method stub
                super.onError();
                callback.onError();
            }
        }, WIFI_TIMEOUT);
    }

    private interface CloseApCallback
    {
        public void onClosed();

        public void onError();
    }

    public void finishListening(Context context, final FinishListeningCallback callback)
    {
        Exception firstExcep = null;
        if (listeningKey != null)
        {
            try
            {
                listeningKey.cancel();
                ((ServerSocketChannel) listeningKey.channel()).close();
                listeningKey = null;
            } catch (IOException e)
            {
                if (firstExcep == null)
                    firstExcep = e;
            }
        }
        if (preApConfig != null)
        {
            try
            {
                final Exception firstExcepPoint = firstExcep;
                closeAp(context, new CloseApCallback()
                {
                    @Override
                    public void onClosed()
                    {
                        // TODO Auto-generated method stub
                        if (firstExcepPoint == null)
                            callback.onFinished();
                        else
                            callback.onError(firstExcepPoint);
                    }

                    @Override
                    public void onError()
                    {
                        // TODO Auto-generated method stub
                        if (firstExcepPoint == null)
                            callback.onError(new RuntimeException("close ap failed by 'CloseApCallback.onError()'."));
                        else
                            callback.onError(firstExcepPoint);
                    }
                });
                return;
            } catch (ReflectHiddenFuncException e)
            {
                if (firstExcep == null)
                    firstExcep = e;
            }
        }
        final Exception firstExcepPoint = firstExcep;
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                if (firstExcepPoint == null)
                    callback.onFinished();
                else
                    callback.onError(firstExcepPoint);
            }
        });
    }

    public void scanUsers(Context context, final ScanUsersCallback callback)
    {
        final WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.setWifiEnabled(true, new WifiCallback(context)
        {
            @Override
            public void onWifiEnabled()
            {
                // TODO Auto-generated method stub
                super.onWifiEnabled();
                wifiUtils.startScan(new WifiCallback(context)
                {
                    @Override
                    public void onScanResults(List<ScanResult> scanResults)
                    {
                        // TODO Auto-generated method stub
                        super.onScanResults(scanResults);
                        List<RemoteUser> callbackVal = new ArrayList<RemoteUser>();
                        for (ScanResult result : scanResults)
                        {
                            String ssid = result.SSID;
                            if (ssid.startsWith("GHFY_"))
                            {
                                String[] ssidInfo = ssid.split("_");
                                if (ssidInfo.length == 3)
                                {
                                    RemoteUser user = null;
                                    try
                                    {
                                        user = new RemoteUser(new String(Base64.decode(ssidInfo[1]), ACTION_CHARSET));
                                    } catch (UnsupportedEncodingException e)
                                    {
                                        throw new RuntimeException(e);
                                    }
                                    user.setScanResult(result);
                                    callbackVal.add(user);
                                }
                            }
                        }
                        callback.onScanned(callbackVal);
                    }

                    @Override
                    public void onTimeout()
                    {
                        // TODO Auto-generated method stub
                        super.onTimeout();
                        callback.onError();
                    }

                    @Override
                    public void onError()
                    {
                        // TODO Auto-generated method stub
                        super.onError();
                        callback.onError();
                    }
                }, WIFI_TIMEOUT);
            }

            @Override
            public void onError()
            {
                // TODO Auto-generated method stub
                super.onError();
                callback.onError();
            }

            @Override
            public void onTimeout()
            {
                // TODO Auto-generated method stub
                super.onTimeout();
                callback.onError();
            }
        }, WIFI_TIMEOUT);
    }

    public void connectToRemoteAp(Context context, final RemoteUser user, final ConnectToRemoteApCallback callback)
    {
        final ScanResult result = user.getScanResult();
        if (result == null)
            throw new IllegalStateException("can only connect to ap which has been found by scanning.");
        final WifiUtils wifiUtils = new WifiUtils(context);
        wifiUtils.setWifiEnabled(true, new WifiCallback(context)
        {
            @Override
            public void onWifiEnabled()
            {
                // TODO Auto-generated method stub
                super.onWifiEnabled();
                wifiUtils.connect(result, null, new WifiCallback(context)
                {
                    @Override
                    public void onNetworkConnected(WifiInfo wifiInfo)
                    {
                        // TODO Auto-generated method stub
                        super.onNetworkConnected(wifiInfo);
                        int apIp = wifiUtils.getWifiManager().getDhcpInfo().serverAddress;
                        String apIpStr = String.format("%d.%d.%d.%d", (apIp & 0xff), (apIp >> 8 & 0xff), (apIp >> 16 & 0xff), (apIp >> 24 & 0xff));
                        user.setIp(apIpStr);
                        callback.onConnected(user);
                    }

                    @Override
                    public void onTimeout()
                    {
                        // TODO Auto-generated method stub
                        super.onTimeout();
                        callback.onError(user);
                    }

                    @Override
                    public void onError()
                    {
                        // TODO Auto-generated method stub
                        super.onError();
                        callback.onError(user);
                    }
                }, WIFI_TIMEOUT);
            }

            @Override
            public void onError()
            {
                // TODO Auto-generated method stub
                super.onError();
                callback.onError(user);
            }

            @Override
            public void onTimeout()
            {
                // TODO Auto-generated method stub
                super.onTimeout();
                callback.onError(user);
            }
        }, WIFI_TIMEOUT);
    }

    public void connectToUser(final RemoteUser user)
    {
        String ip = user.getIp();
        if (ip == null)
            throw new IllegalStateException("ip not found,if the input user is found by scanning,call 'connectToRemoteAp(...)' first.");
        SocketChannel sc = null;
        try
        {
            sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(new InetSocketAddress(ip, LISTENING_PORT));
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
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    callback.onConnectedFailed(user, e);
                }
            });
        }
    }

    public void disconnectUser(final RemoteUser user)
    {
        try
        {
            user.close();
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    callback.onDisconnected(user);
                }
            });
        } catch (final IOException e)
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    callback.onDisconnectedFailed(user, e);
                }
            });
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
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                callback.onTransferProgress(transfer, 0);
            }
        });
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
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    callback.onTransferFailed(transfer, e);
                }
            });
        }
    }

    public void cancelTransfer(TransferEntity transfer)
    {
        transfer.setCancelFlag();
    }

    public void close(Context context, final CloseCallback callback)
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
                    SelectableChannel curChannel = curKey.channel();
                    if (curChannel instanceof SocketChannel) // ServerSocketChannel将由finishListening方法关闭
                        ((SocketChannel) curChannel).close();
                } catch (IOException e)
                {
                    if (firstExcep == null)
                        firstExcep = e;
                }
            }
        }
        final Exception firstExcepPoint = firstExcep;
        finishListening(context, new FinishListeningCallback()
        {
            @Override
            public void onFinished()
            {
                // TODO Auto-generated method stub
                if (firstExcepPoint == null)
                    callback.onClosed();
                else
                    callback.onError(firstExcepPoint);
            }

            @Override
            public void onError(Exception e)
            {
                // TODO Auto-generated method stub
                if (firstExcepPoint == null)
                    callback.onError(e);
                else
                    callback.onError(firstExcepPoint);
            }
        });
    }

}
