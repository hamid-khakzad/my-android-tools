package cn.emagsoftware.cmcc.wlan;

import java.util.List;

import android.content.Context;

import cn.emagsoftware.util.LogManager;

import com.chinamobile.g3wlan.export.G3WlanStatus;
import com.chinamobile.g3wlan.export.ServiceCore;
import com.chinamobile.g3wlan.export.ServiceInterface;

public class CMCCImplUser
{

    protected Context          context     = null;
    protected ServiceInterface serviceCore = null;

    public CMCCImplUser(Context context)
    {
        this.context = context;
        serviceCore = new ServiceCore(context);
    }

    /**
     * 初始化。检查是否安装了认证模块。若未安装，将提示用户安装，该方法将一直堵塞直到用户安装完毕、选择不安装或等待超时。
     * 
     * @return true表示认证模块安装成功，false表示用户选择了不安装或等待超时。
     */
    public boolean initialize()
    {
        System.out.println("init...");
        int ready = serviceCore.initialize();
        if (G3WlanStatus.READY != ready)
        {
            System.out.println("EXIT: service not ready: " + ready);
            return false;
        }
        System.out.println("service ready");
        return true;
    }

    public void uninitialize()
    {
        serviceCore.uninitialize();
    }

    public int login(String IMSI, String user, String password)
    {
        // TODO Auto-generated method stub
        if (isNullStr(user) || isNullStr(password))
        {
            System.out.println("Checking profile");
            List<String> profile = serviceCore.getProfile(IMSI);
            System.out.println("current profile " + profile);
            if (profile.size() > 0)
            {
                System.out.println("Profile already registered");
            } else
            {
                System.out.println("Exit: Profile not registered");
                // return;
            }
        }
        System.out.println("Logging in...");
        int ret = serviceCore.login(IMSI, user, password, 0);
        System.out.println("login result " + ret);
        return checkLoginStatus();
    }

    public static boolean isNullStr(String str)
    {
        if (null == str || "null".equals(str) || "".equals(str.trim()))
        {
            return true;
        }
        return false;
    }

    int waitStatus(ServiceInterface serviceCore, Integer[] states, long timeout)
    {
        long now = System.currentTimeMillis();
        long s = now;
        long e = s + timeout;
        int st = -1;
        while (now < e)
        {
            st = serviceCore.getStatus();
            for (int i = 0; i < states.length; i++)
            {
                if (st == states[i])
                {
                    return st;
                }
            }
            try
            {
                Thread.sleep(500);
            } catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
            now = System.currentTimeMillis();
        }
        return st;
    }

    int checkLoginStatus()
    {
        LogManager.logD(CMCCImplUser.class, "wait status");
        int status = waitStatus(serviceCore, new Integer[] { G3WlanStatus.LOGGED_IN, G3WlanStatus.AUTH_DATA_REQUIRED, G3WlanStatus.LOGIN_FAIL, G3WlanStatus.SERVICE_UPGRADING, G3WlanStatus.READY // in
                // case
                // cancelled
                }, 15000);

        LogManager.logD(CMCCImplUser.class, "now status " + status);

        if (G3WlanStatus.LOGGING_IN == status || G3WlanStatus.LOGIN_FAIL == status)
        {
            /*
             * waited long time still logging in, should check error and cancel login
             */
            // print("Timeout, cancel login");
            // serviceCore.cancelLogin();
            System.out.println("Login timeout");

        } else if (G3WlanStatus.LOGIN_FAIL == status)
        {

            List reason = serviceCore.getReason();
            System.out.println("Login failed. Reason: " + reason);

        } else if (G3WlanStatus.LOGGED_IN == status)
        {
            /*
             * logged in, client can go ahead for service logic
             */
            System.out.println("Logged in");
        } else if (G3WlanStatus.AUTH_DATA_REQUIRED == status)
        {
            /*
             * user/password incorrect, client should prompt for new user/password, then call modify() and login() again
             */
            System.out.println("Need to prompt for new user/password");
        }
        // else if(
        // G3WlanStatus.LOGIN_FAIL==status
        // || G3WlanStatus.LOGGING_IN==status
        // ){
        // /*
        // * not logged in, could be network error, etc.
        // * client can call getReason() to fetch details
        // */
        // List<String> reason = new ArrayList<String>();
        // serviceCore.getReason(reason);
        // print("not logged in. reason: "+reason.get(0));
        // }
        else if (G3WlanStatus.SERVICE_UPGRADING == status)
        {
            /*
             * service is being upgraded. client should prompt user to try login later
             */
            System.out.println("Service upgrading. please try again later");
        } else if (G3WlanStatus.READY == status)
        {
            System.out.println("Login cancelled");
        }
        return status;
    }

    public void cancelLogin()
    {
        int ret = serviceCore.cancelLogin();
        System.out.println("cancellogin " + ret);
    }

    public boolean isLogged()
    {
        boolean ret = ((ServiceCore) serviceCore).getCaller().isOnline();
        System.out.println("isonline " + ret);
        return ret;
    }

    /**
     * @param force 为ture将强制下线，否则执行一般下线指令
     * @return
     */
    public int logout(boolean force)
    {
        int ret = -1;
        if (force)
        {
            System.out.println("force logging out...");
            ret = serviceCore.eWalkLogout();
            System.out.println("logout result " + ret);
        } else
        {
            System.out.println("logging out...");
            ret = serviceCore.logout();
            System.out.println("logout result " + ret);
        }
        int status = waitStatus(serviceCore, new Integer[] { G3WlanStatus.LOGGED_IN, G3WlanStatus.INIT, G3WlanStatus.READY, }, 5000);

        LogManager.logD(CMCCImplUser.class, "logout status " + status);

        switch (status)
        {
            case G3WlanStatus.INIT:
            case G3WlanStatus.READY:
                System.out.println("logout successful");
                break;
            default:
                System.out.println("logout failed");
                break;
        }
        return status;
    }

    public List getReason()
    {
        return serviceCore.getReason();
    }

}
