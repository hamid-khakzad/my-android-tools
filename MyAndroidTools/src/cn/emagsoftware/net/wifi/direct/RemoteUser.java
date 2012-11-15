package cn.emagsoftware.net.wifi.direct;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.net.wifi.ScanResult;

public class RemoteUser
{

    private String               name       = null;
    private ScanResult           scanResult = null;
    private String               ip         = null;
    private SelectionKey         key        = null;
    private List<TransferEntity> transfers  = Collections.synchronizedList(new LinkedList<TransferEntity>());

    RemoteUser(String name)
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

    void addTransfer(TransferEntity transfer)
    {
        if (transfer == null)
            throw new NullPointerException();
        transfers.add(transfer);
    }

    void removeTransfer(TransferEntity transfer)
    {
        if (transfer == null)
            throw new NullPointerException();
        transfers.remove(transfer);
    }

    void close() throws IOException
    {
        Object[] transfersArr = transfers.toArray();
        for (Object transfer : transfersArr)
        {
            ((TransferEntity) transfer).setCancelFlag();
        }
        if (key != null)
        {
            key.cancel();
            SocketChannel sc = (SocketChannel) key.channel();
            sc.close();
            key = null;
        }
    }
}
