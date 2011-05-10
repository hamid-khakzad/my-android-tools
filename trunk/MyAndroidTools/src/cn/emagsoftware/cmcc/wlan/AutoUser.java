package cn.emagsoftware.cmcc.wlan;

import java.io.IOException;

import android.content.Context;

public abstract class AutoUser {
	
	public static AutoUser getDefaultImpl(Context context){
		return new DefaultAutoUser(context);
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
	 * @return �ɹ�����null��ʧ�ܽ����ؾ���������Ϣ
	 */
	public abstract String requestPassword();
	
	/**
	 * @return �ɹ�����null��ʧ�ܽ����ؾ���������Ϣ
	 */
	public abstract String login();
	
	public abstract void cancelLogin();
	
	/**
	 * @return �ѵ�¼����true�����򷵻�false
	 * @throws IOException
	 */
	public abstract boolean isLogged() throws IOException;
	
	/**
	 * @return �ɹ�����null��ʧ�ܽ����ؾ���������Ϣ
	 */
	public abstract String logout();
	
}
