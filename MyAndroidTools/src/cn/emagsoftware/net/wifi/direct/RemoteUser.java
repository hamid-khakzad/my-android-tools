package cn.emagsoftware.net.wifi.direct;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.List;

public class RemoteUser
{

    private String               name       = null;
    private String               ip         = null;
    private SelectionKey         key        = null;
    private List<TransferEntity> transfers  = new LinkedList<TransferEntity>();
    private long refreshTime;
    int state = 1; //0:连接中；1:已断开；2:已连接

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

    public String getIp()
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

    List<TransferEntity> getTransfers()
    {
        return transfers;
    }

    void setRefreshTime(long refreshTime)
    {
        this.refreshTime = refreshTime;
    }

    long getRefreshTime()
    {
        return refreshTime;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof RemoteUser))
            return false;
        RemoteUser input = (RemoteUser)o;
        return getIp().equals(input.getIp());
    }

}
