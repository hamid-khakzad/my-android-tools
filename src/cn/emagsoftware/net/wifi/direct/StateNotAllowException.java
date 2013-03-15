package cn.emagsoftware.net.wifi.direct;

public class StateNotAllowException extends Exception
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public StateNotAllowException()
    {
        super();
    }

    public StateNotAllowException(String message)
    {
        super(message);
    }

    public StateNotAllowException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public StateNotAllowException(Throwable cause)
    {
        super(cause);
    }

}
