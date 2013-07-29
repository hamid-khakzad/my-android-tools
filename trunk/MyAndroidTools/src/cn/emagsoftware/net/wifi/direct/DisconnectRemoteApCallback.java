package cn.emagsoftware.net.wifi.direct;

public interface DisconnectRemoteApCallback
{

    public void onDisconnected(RemoteAp ap);

    public void onError(RemoteAp ap, Exception e);

}
