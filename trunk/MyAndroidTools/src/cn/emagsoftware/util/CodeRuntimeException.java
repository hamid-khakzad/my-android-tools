package cn.emagsoftware.util;

public class CodeRuntimeException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String            code             = null;

    public CodeRuntimeException(String code)
    {
        if (code == null)
            throw new NullPointerException();
        this.code = code;
    }

    public CodeRuntimeException(String code, String detailMessage)
    {
        super(detailMessage);
        if (code == null)
            throw new NullPointerException();
        this.code = code;
    }

    public CodeRuntimeException(String code, Throwable throwable)
    {
        super(throwable);
        if (code == null)
            throw new NullPointerException();
        this.code = code;
    }

    public CodeRuntimeException(String code, String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
        if (code == null)
            throw new NullPointerException();
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }

    public String getCodeAndMessage() {
        return StringUtilities.toStringWhenNull(getLocalizedMessage(), "") + "[code:" + code + "]";
    }

    @Override
    public String toString()
    {
        String name = getClass().getName();
        return name + ": " + getCodeAndMessage();
    }

}
