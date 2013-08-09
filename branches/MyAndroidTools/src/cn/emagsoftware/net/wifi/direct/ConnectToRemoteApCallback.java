package cn.emagsoftware.net.wifi.direct;

public interface ConnectToRemoteApCallback
{

    public void onConnected(RemoteUser user);

    public void onError(RemoteUser user);

}
