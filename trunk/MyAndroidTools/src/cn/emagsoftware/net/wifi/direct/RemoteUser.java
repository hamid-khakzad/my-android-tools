package cn.emagsoftware.net.wifi.direct;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RemoteUser
{

    private String               name       = null;
    private String               ip         = null;
    private SelectionKey         key        = null;
    private List<TransferEntity> transfers  = Collections.synchronizedList(new LinkedList<TransferEntity>());
    private long refreshTime;

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

    void setRefreshTime(long refreshTime)
    {
        this.refreshTime = refreshTime;
    }

    long getRefreshTime()
    {
        return refreshTime;
    }

    void close() throws IOException
    {
        IOException firstExcep = null;
        Object[] transfersArr = transfers.toArray();
        for (Object transfer : transfersArr)
        {
            try
            {
                ((TransferEntity) transfer).close();
            }catch (IOException e)
            {
                if(firstExcep == null)
                    firstExcep = e;
            }
        }
        if (key != null)
        {
            try
            {
                key.cancel();
                SocketChannel sc = (SocketChannel) key.channel();
                sc.close();
                key = null;
            }catch (IOException e)
            {
                if(firstExcep == null)
                    firstExcep = e;
            }
        }
        if(firstExcep != null)
            throw firstExcep;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof RemoteUser))
            return false;
        RemoteUser input = (RemoteUser)o;
        return getIp().equals(input.getIp());
    }

}
