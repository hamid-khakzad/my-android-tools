package cn.emagsoftware.net.wifi.direct;

import java.util.List;

/**
 * Created by Wendell on 13-8-12.
 */
public interface GetConnectedUsersCallback
{

    public void onGet(List<RemoteUser> users);

}
