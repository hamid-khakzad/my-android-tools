package cn.emagsoftware.net.wifi.direct;

import java.util.List;

public interface ScanRemoteApsCallback
{

    public void onScanned(List<RemoteAp> aps);

    public void onError();

}
