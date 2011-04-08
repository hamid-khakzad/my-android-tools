package cn.emagsoftware.cmcc.wlan;

public abstract class User {
	
	public static final int RETURN_TRUE = 0;
	public static final int RETURN_FALSE_GENERIC = 1;
	public static final int RETURN_FALSE_NET_ERROR = 2;
	public static final int RETURN_FALSE_CANCEL = 3;
	public static final int RETURN_FALSE_RESPONSE_PARSE_ERROR = 4;
	public static final int RETURN_FALSE_NAME_OR_PWD_WRONG = 5;
	
	public static User getDefaultImpl(String userName,String password){
		return new DefaultUser(userName,password);
	}
	
	protected String userName = null;
	protected String password = null;
	
	protected User(String userName,String password){
		if(userName == null || password == null) throw new NullPointerException();
		this.userName = userName;
		this.password = password;
	}
	
	public void setUserName(String userName){
		if(userName == null) throw new NullPointerException();
		this.userName = userName;
	}
	
	public void setPassword(String password){
		if(password == null) throw new NullPointerException();
		this.password = password;
	}
	
	public abstract int login();
	
	public abstract void cancelLogin();
	
	public abstract int isLogged();
	
	public abstract int logout();
	
}
