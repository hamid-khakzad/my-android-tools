package cn.emagsoftware.net.wifi.direct;

import java.util.List;

public interface ScanUsersCallback
{

    public void onScanned(List<RemoteUser> users);

    public void onError(Exception e);

}
