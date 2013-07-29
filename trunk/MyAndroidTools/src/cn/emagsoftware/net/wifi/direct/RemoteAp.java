package cn.emagsoftware.net.wifi.direct;

import android.net.wifi.ScanResult;

/**
 * Created by Wendell on 13-7-25.
 */
public class RemoteAp
{

    private String               name       = null;
    private ScanResult scanResult = null;

    RemoteAp(String name)
    {
        if (name == null)
            throw new NullPointerException();
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    void setScanResult(ScanResult scanResult)
    {
        if (scanResult == null)
            throw new NullPointerException();
        this.scanResult = scanResult;
    }

    ScanResult getScanResult()
    {
        return scanResult;
    }

}
