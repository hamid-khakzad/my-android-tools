package cn.emagsoftware.util;

public class CodeException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int               code             = -1;

    public CodeException(int code)
    {
        this.code = code;
    }

    public CodeException(int code, String message)
    {
        super(message);
        this.code = code;
    }

    public CodeException(int code, Throwable throwable)
    {
        super(throwable);
        this.code = code;
    }

    public CodeException(int code, String message, Throwable throwable)
    {
        super(message, throwable);
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }

    @Override
    public String toString()
    {
        // TODO Auto-generated method stub
        String str = StringUtilities.toStringWhenNull(super.toString(), "");
        return str + "(code:" + code + ")";
    }

}
