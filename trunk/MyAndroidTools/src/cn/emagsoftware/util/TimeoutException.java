package cn.emagsoftware.util;

/**
 * Created by Wendell on 14-7-14.
 */
public class TimeoutException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public TimeoutException()
    {
        super();
    }

    public TimeoutException(String message)
    {
        super(message);
    }

    public TimeoutException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TimeoutException(Throwable cause)
    {
        super(cause);
    }

}
