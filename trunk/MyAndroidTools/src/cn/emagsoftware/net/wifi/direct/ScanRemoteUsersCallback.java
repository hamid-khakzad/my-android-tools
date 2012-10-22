package cn.emagsoftware.net.wifi.direct;

import java.util.List;

public interface ScanRemoteUsersCallback
{

    public void onScanned(List<RemoteUser> users);

    public void onError();

}
