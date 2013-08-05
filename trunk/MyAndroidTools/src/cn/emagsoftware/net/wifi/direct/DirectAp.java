package cn.emagsoftware.net.wifi.direct;

import android.net.wifi.ScanResult;

/**
 * Created by Wendell on 13-8-5.
 */
public class DirectAp
{

    private String               name       = null;
    private ScanResult scanResult = null;

    DirectAp(String name)
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
