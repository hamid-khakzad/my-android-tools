package cn.emagsoftware.net.wifi.direct;

import java.io.IOException;

public class DuplicateAlreadyConnectedException extends IOException
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DuplicateAlreadyConnectedException()
    {
        super();
    }

    public DuplicateAlreadyConnectedException(String message)
    {
        super(message);
    }

}
