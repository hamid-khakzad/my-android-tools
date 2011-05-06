package cn.emagsoftware.cmcc.wlan;

public abstract class AutoUser {
	
	public static AutoUser getDefaultImpl(){
		return new DefaultAutoUser();
	}
	
	protected String userName = null;
	protected String password = null;
	
	protected AutoUser(){
	}
	
	public void setUserName(String userName){
		if(userName == null) throw new NullPointerException();
		this.userName = userName;
	}
	
	public void setPassword(String password){
		if(password == null) throw new NullPointerException();
		this.password = password;
	}
	
	/**
	 * @return 成功返回null，失败将返回具体描述信息
	 */
	public abstract String requestPassword();
	
	/**
	 * @return 成功返回null，失败将返回具体描述信息
	 */
	public abstract String login();
	
	public abstract void cancelLogin();
	
	/**
	 * @return 成功返回null，失败将返回具体描述信息
	 */
	public abstract String isLogged();
	
	/**
	 * @return 成功返回null，失败将返回具体描述信息
	 */
	public abstract String logout();
	
}
