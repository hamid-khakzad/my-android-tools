package cn.emagsoftware.net.wifi.direct;

public interface ConnectToRemoteApCallback
{

    public void onConnected(RemoteAp ap);

    public void onError(RemoteAp ap);

}
