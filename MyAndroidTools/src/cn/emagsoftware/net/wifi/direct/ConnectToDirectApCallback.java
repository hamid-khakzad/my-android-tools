package cn.emagsoftware.net.wifi.direct;

/**
 * Created by Wendell on 13-8-5.
 */
public interface ConnectToDirectApCallback
{

    public void onConnected(DirectAp ap);

    public void onError(DirectAp ap);

}
