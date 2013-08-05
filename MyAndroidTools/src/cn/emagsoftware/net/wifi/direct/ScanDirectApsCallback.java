package cn.emagsoftware.net.wifi.direct;

import java.util.List;

/**
 * Created by Wendell on 13-8-5.
 */
public interface ScanDirectApsCallback
{

    public void onScanned(List<DirectAp> aps);

    public void onError();

}
