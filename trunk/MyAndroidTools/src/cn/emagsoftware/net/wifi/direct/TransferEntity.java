package cn.emagsoftware.net.wifi.direct;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class TransferEntity
{
    private RemoteUser   remoteUser;
    private String       sendPath;
    private String       savingPath;
    private long         size;
    private SelectionKey transferKey;

    TransferEntity()
    {
    }

    public RemoteUser getRemoteUser()
    {
        return remoteUser;
    }

    void setRemoteUser(RemoteUser remoteUser)
    {
        if (remoteUser == null)
            throw new NullPointerException();
        this.remoteUser = remoteUser;
    }

    public String getSendPath()
    {
        return sendPath;
    }

    void setSendPath(String sendPath)
    {
        if (sendPath == null)
            throw new NullPointerException();
        this.sendPath = sendPath;
    }

    public String getSavingPath()
    {
        return savingPath;
    }

    void setSavingPath(String savingPath)
    {
        if (savingPath == null)
            throw new NullPointerException();
        this.savingPath = savingPath;
    }

    public long getSize()
    {
        return size;
    }

    void setSize(long size)
    {
        if (size < 0)
            throw new IllegalArgumentException("size could not less than zero.");
        this.size = size;
    }

    void setSelectionKey(SelectionKey transferKey)
    {
        if (transferKey == null)
            throw new NullPointerException();
        this.transferKey = transferKey;
    }

    SelectionKey getSelectionKey()
    {
        return transferKey;
    }

    void close() throws IOException
    {
        if (transferKey == null)
            return;
        SocketChannel sc = (SocketChannel) transferKey.channel();
        sc.close();
        transferKey.cancel();
        remoteUser.removeTransfer(this);
        transferKey = null;
    }

}
