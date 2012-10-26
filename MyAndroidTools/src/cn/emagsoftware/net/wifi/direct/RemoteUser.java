package cn.emagsoftware.net.wifi.direct;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import android.net.wifi.ScanResult;

public class RemoteUser
{

    private String       name        = null;
    private ScanResult   scanResult  = null;
    private String       ip          = null;
    private SelectionKey key         = null;
    private SelectionKey transferKey = null;

    RemoteUser(String name)
    {
        setName(name);
    }

    void setName(String name)
    {
        if (name == null)
            throw new NullPointerException();
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    void setScanResult(ScanResult scanResult)
    {
        if (scanResult == null)
            throw new NullPointerException();
        this.scanResult = scanResult;
    }

    ScanResult getScanResult()
    {
        return scanResult;
    }

    void setIp(String ip)
    {
        if (ip == null)
            throw new NullPointerException();
        this.ip = ip;
    }

    String getIp()
    {
        return ip;
    }

    void setKey(SelectionKey key)
    {
        if (key == null)
            throw new NullPointerException();
        this.key = key;
    }

    SelectionKey getKey()
    {
        return key;
    }

    void setTransferKey(SelectionKey transferKey)
    {
        if (transferKey == null)
            throw new NullPointerException();
        this.transferKey = transferKey;
    }

    SelectionKey getTransferKey()
    {
        return transferKey;
    }

    boolean isConnectedComplete()
    {
        return key != null && transferKey != null;
    }

    public void close() throws IOException
    {
        try
        {
            if (key != null)
            {
                key.cancel();
                SocketChannel sc = (SocketChannel) key.channel();
                key = null;
                sc.close();
            }
        } finally
        {
            if (transferKey != null)
            {
                transferKey.cancel();
                SocketChannel sc = (SocketChannel) transferKey.channel();
                transferKey = null;
                sc.close();
            }
        }

    }

}
