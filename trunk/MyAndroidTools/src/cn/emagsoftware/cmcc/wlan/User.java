package cn.emagsoftware.cmcc.wlan;

import java.io.IOException;

public abstract class User
{

    public static final int LOGIN_TRUE                                = 0;
    public static final int LOGIN_FALSE_NET_ERROR                     = 1;
    public static final int LOGIN_FALSE_CANCEL                        = 2;
    public static final int LOGIN_FALSE_RESPONSE_PARSE_ERROR          = 3;
    public static final int LOGIN_FALSE_NAME_OR_PWD_WRONG             = 4;
    public static final int LOGIN_FALSE_NAME_OR_PWD_INVALID           = 5;
    public static final int LOGIN_FALSE_ALREADY_LOGIN                 = 6;
    public static final int LOGIN_FALSE_GENERIC                       = 7;

    public static final int LOGOUT_TRUE                               = 0;
    public static final int LOGOUT_FALSE_NET_ERROR                    = 1;
    public static final int LOGOUT_FALSE_RESPONSE_PARSE_ERROR         = 2;
    public static final int LOGOUT_FALSE_ALREADY_OFFLINE_CHARGE_SHORT = 3;
    public static final int LOGOUT_FALSE_GENERIC                      = 4;

    public static User getDefaultImpl(String userName, String password)
    {
        return new DefaultUser(userName, password);
    }

    protected String userName = null;
    protected String password = null;

    protected User(String userName, String password)
    {
        if (userName == null || password == null)
            throw new NullPointerException();
        this.userName = userName;
        this.password = password;
    }

    public void setUserName(String userName)
    {
        if (userName == null)
            throw new NullPointerException();
        this.userName = userName;
    }

    public void setPassword(String password)
    {
        if (password == null)
            throw new NullPointerException();
        this.password = password;
    }

    public abstract int login();

    public abstract void cancelLogin();

    public abstract boolean isLogged() throws IOException;

    public abstract int logout();

}
