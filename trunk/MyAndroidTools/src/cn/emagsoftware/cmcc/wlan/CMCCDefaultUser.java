package cn.emagsoftware.cmcc.wlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.chinamobile.g3wlan.export.G3WlanStatus;
import com.chinamobile.g3wlan.export.ServiceCore;
import com.chinamobile.g3wlan.export.ServiceInterface;

public class CMCCDefaultUser {
	protected static final String TAG = "CMCCDefaultUser";
	protected static final String GUIDE_URL = "http://www.baidu.com";
	protected static final String GUIDE_HOST = "www.baidu.com";
	protected static final String GD_JSESSIONID = "JSESSIONID=";
	protected static final String BJ_PHPSESSID = "PHPSESSID=";
	protected static final String KEYWORD_CMCCCS = "cmcccs";
	protected static final String KEYWORD_LOGINREQ = "login_req";
	protected static final String KEYWORD_LOGINRES = "login_res";
	protected static final String KEYWORD_OFFLINERES = "offline_res";
	protected static final String SEPARATOR = "|";
	protected static final String CMCC_PORTAL_URL = "https://221.176.1.140/wlan/index.php";
	protected static final String PREFIX_HTTPS = "https";
	// for redirection
	protected static final String INDICATOR_REDIRECT_PORTALURL = "portalurl";
	protected static final String INDICATOR_LOGIN_AC_NAME = "wlanacname";
	protected static final String INDICATOR_LOGIN_USER_IP = "wlanuserip";
	// form parameters in cmcc logining page
	protected static final String CMCC_LOGINFORM_NAME = "loginform";
	protected static final String INDICATOR_LOGIN_USERNAME = "USER";
	protected static final String INDICATOR_LOGIN_PASSWORD = "PWD";

	protected boolean isCancelLogin = false;
	protected String sessionCookie = null;
	protected String cmccPageHtml = null;
	protected Map<String, String> cmccLoginPageFields = new HashMap<String, String>();

	Handler msgHandler = null;
	private ServiceInterface serviceCore = null;

	public CMCCDefaultUser(String userName, String password, Context context) {

		serviceCore = new ServiceCore(context);

		InitHelper w = new InitHelper();
		Thread t = new Thread(w);
		t.start();
	}

	class InitHelper implements Runnable {

		public void run() {
			init();
		}

		void init() {

			System.out.println("init...");
			int ready = serviceCore.initialize();
			if (G3WlanStatus.READY != ready) {
				exit("service not ready: " + ready);
				return;
			}
			System.out.println("service ready");

		}

		void exit(String txt) {
			System.out.println("EXIT: " + txt);
		}
	}

	public int login(String IMSI, String user, String password) {
//		LoginHelper helper = new LoginHelper(IMSI, user, password);
//		Thread t = new Thread(helper);
//		t.start();
		return serviceCore.login(IMSI, user, password, 0);
	}

	public static boolean isNullStr(String str) {
		if (null == str || "null".equals(str) || "".equals(str.trim())) {
			return true;
		}
		return false;
	}

	class LoginHelper implements Runnable {
		/**
		 * @param iMSI
		 * @param user
		 * @param password
		 */
		public LoginHelper(String iMSI, String user, String password) {
			super();
			IMSI = iMSI;
			this.user = user;
			this.password = password;
		}

		String IMSI;
		String user;
		String password;

		public void run() {

			if (isNullStr(user) || isNullStr(password)) {
				System.out.println("Checking profile");
				List<String> profile = serviceCore.getProfile(IMSI);
				System.out.println("current profile " + profile);
				if (profile.size() > 0) {
					System.out.println("Profile already registered");
				} else {
					System.out.println("Exit: Profile not registered");
					// return;
				}
			}

			System.out.println("Logging in...");

			int ret = serviceCore.login(IMSI, user, password, 0);
			System.out.println("login result " + ret);

			checkLoginStatus();
		}
	}

	int waitStatus(ServiceInterface serviceCore, Integer[] states, long timeout) {

		long now = System.currentTimeMillis();
		long s = now;
		long e = s + timeout;
		int st = -1;
		while (now < e) {

			st = serviceCore.getStatus();
			for (int i = 0; i < states.length; i++) {
				if (st == states[i]) {
					return st;
				}
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			now = System.currentTimeMillis();
		}
		return st;
	}

	private int status;
	public int getStatus(){
		return status;
	}
	
	void checkLoginStatus() {
		Log.d(TAG, "wait status");
		status = waitStatus(serviceCore, new Integer[] {
				G3WlanStatus.LOGGED_IN, G3WlanStatus.AUTH_DATA_REQUIRED,
				G3WlanStatus.LOGIN_FAIL, G3WlanStatus.SERVICE_UPGRADING,
				G3WlanStatus.READY // in case cancelled
				}, 15000);

		Log.d(TAG, "now status " + status);

		if (G3WlanStatus.LOGGING_IN == status
				|| G3WlanStatus.LOGIN_FAIL == status) {
			/*
			 * waited long time still logging in, should check error and cancel
			 * login
			 */
			// print("Timeout, cancel login");
			// serviceCore.cancelLogin();
			System.out.println("Login timeout");

		} else if (G3WlanStatus.LOGGED_IN == status) {
			/*
			 * logged in, client can go ahead for service logic
			 */
			System.out.println("Logged in");
		} else if (G3WlanStatus.AUTH_DATA_REQUIRED == status) {
			/*
			 * user/password incorrect, client should prompt for new
			 * user/password, then call modify() and login() again
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
		else if (G3WlanStatus.SERVICE_UPGRADING == status) {
			/*
			 * service is being upgraded. client should prompt user to try login
			 * later
			 */
			System.out.println("Service upgrading. please try again later");
		} else if (G3WlanStatus.READY == status) {
			System.out.println("Login cancelled");
		}
	}
	
	public void cancelLogin(){
		CancelLoginHelper c = new CancelLoginHelper();
		(new Thread(c)).start();
	}
	
	class CancelLoginHelper implements Runnable{

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			int ret = serviceCore.cancelLogin();
			System.out.println("cancellogin "+ret);
			checkLoginStatus();
		}
		
	}

}
