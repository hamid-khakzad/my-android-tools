package cn.emagsoftware.cmcc.wlan;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.wendell.net.http.HttpConnectionManager;
import com.wendell.net.http.HttpResponseResult;

class DefaultAutoUser extends AutoUser {
	
	protected static final String GUIDE_URL = "http://www.baidu.com";
	protected static final String GUIDE_HOST = "www.baidu.com";
	protected static final String GD_JSESSIONID = "JSESSIONID=";
	protected static final String BJ_PHPSESSID = "PHPSESSID=";
	
	protected String sessionCookie = null;
	protected String cmccPageHtml = null;
	protected String cmccLoginPageHtml = null;
	
	public DefaultAutoUser(){
		super();
	}
	
	@Override
	public String requestPassword() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String login() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void cancelLogin() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String isLogged() {
		// TODO Auto-generated method stub
		try{
			HttpResponseResult result = doHttpGetContainsRedirect(GUIDE_URL,false);
			String host = result.getResponseURL().getHost();
			String html = result.getDataString("gb2312");
			if(GUIDE_HOST.equalsIgnoreCase(host) && html.indexOf(GUIDE_HOST) >= 0) {   //若能访问到原始站点，证明已登录
				return null;
			}
			//若不能访问原始站点，即重定向到了CMCC页面
			this.cmccPageHtml = html;
			return "当前未登录";
		}catch(IOException e){
			Log.e("DefaultAutoUser", "requesting "+GUIDE_URL+" failed.", e);
			return "网络错误";
		}
	}
	
	protected HttpResponseResult doHttpGetContainsRedirect(String url,boolean isSSL) throws IOException {
		Map<String,List<String>> requestHeaders = new HashMap<String,List<String>>();
		List<String> values = new ArrayList<String>();
		values.add("gb2312");
		requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_ACCEPT_CHARSET, values);
		values = new ArrayList<String>();
		values.add("application/x-www-form-urlencoded");
		requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_CONTENT_TYPE, values);
		values = new ArrayList<String>();
		values.add("G3WLAN");
		requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_USER_AGENT, values);
		HttpResponseResult result = HttpConnectionManager.doGet(url, isSSL, false, 15000, requestHeaders);
		int code = result.getResponseCode();
		while(code != HttpURLConnection.HTTP_OK && code == HttpURLConnection.HTTP_MOVED_TEMP){
			List<String> headerValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_LOCATION.toLowerCase());
			String location = headerValues.get(0);
			result = HttpConnectionManager.doGet(location, false, false, 15000, requestHeaders);
			code = result.getResponseCode();
		}
		if(code != HttpURLConnection.HTTP_OK) throw new IOException("requesting url returns code:"+code);
		//以下获取cookie
		List<String> setCookieValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_SET_COOKIE.toLowerCase());
		String setCookieValue = setCookieValues.get(0);
		if(setCookieValue != null) {
			String[] setCookieGroup = setCookieValue.split(";");
			for(String tmp:setCookieGroup) {
				if(tmp.trim().startsWith(GD_JSESSIONID) //for Guangdong: "JSESSIONID="
				   || tmp.trim().startsWith(BJ_PHPSESSID) //for Beijing: "PHPSESSID="
				){
					this.sessionCookie = tmp.trim();
					break;
				}
			}
		}
		return result;
	}
	
	protected HttpResponseResult doHttpPostContainsRedirect(String url,boolean isSSL,Map<String,String> params) throws IOException{
		Map<String,List<String>> requestHeaders = new HashMap<String,List<String>>();
		List<String> values = new ArrayList<String>();
		values.add("gb2312");
		requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_ACCEPT_CHARSET, values);
		values = new ArrayList<String>();
		values.add("application/x-www-form-urlencoded");
		requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_CONTENT_TYPE, values);
		values = new ArrayList<String>();
		values.add("G3WLAN");
		requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_USER_AGENT, values);
		HttpResponseResult result = HttpConnectionManager.doPost(url, isSSL, false, 15000, requestHeaders, params, "gb2312");
		int code = result.getResponseCode();
		while(code != HttpURLConnection.HTTP_OK && code == HttpURLConnection.HTTP_MOVED_TEMP){
			List<String> headerValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_LOCATION.toLowerCase());
			String location = headerValues.get(0);
			values = new ArrayList<String>();
			values.add(sessionCookie);
			requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_COOKIE, values);
			result = HttpConnectionManager.doGet(location, false, false, 15000, requestHeaders);
			code = result.getResponseCode();
		}
		if(code != HttpURLConnection.HTTP_OK) throw new IOException("requesting url returns code:"+code);
		return result;
	}
	
	@Override
	public String logout() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
