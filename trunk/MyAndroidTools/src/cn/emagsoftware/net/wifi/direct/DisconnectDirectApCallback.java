package cn.emagsoftware.net.wifi.direct;

/**
 * Created by Wendell on 13-8-5.
 */
public interface DisconnectDirectApCallback
{

    public void onDisconnected(DirectAp ap);

    public void onError(DirectAp ap, Exception e);

}
