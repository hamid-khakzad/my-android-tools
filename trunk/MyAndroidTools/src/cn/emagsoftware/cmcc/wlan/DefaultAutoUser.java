package cn.emagsoftware.cmcc.wlan;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import android.util.Log;

import com.wendell.net.http.HttpConnectionManager;
import com.wendell.net.http.HttpResponseResult;
import com.wendell.util.MathUtilities;
import com.wendell.util.StringUtilities;

class DefaultAutoUser extends AutoUser {
	
	protected static final String GUIDE_URL = "http://www.baidu.com";
	protected static final String GUIDE_HOST = "www.baidu.com";
	protected static final String GD_JSESSIONID = "JSESSIONID=";
	protected static final String BJ_PHPSESSID = "PHPSESSID=";
	
	protected String sessionCookie = null;
	protected String cmccPageUrl = null;
	protected String cmccPageHtml = null;
	protected String cmccLoginPageHtml = null;
	
	public DefaultAutoUser(){
		super();
	}
	
	@Override
	public String requestPassword() {
		// TODO Auto-generated method stub
		if(super.userName == null) return "用户名不能为空";
		String result = isLogged();
		if(result == null) return "用户已登录，无法获取动态密码";
		Parser mHtmlParser = Parser.createParser(cmccPageHtml.toLowerCase(), "gb2312");
		NodeClassFilter frameFilter = new NodeClassFilter(FrameTag.class);
		try{
			NodeList nl = mHtmlParser.parse(frameFilter);
			if(nl == null || nl.size() == 0) throw new ParserException();
			FrameTag ft = (FrameTag)nl.elementAt(0);
			String loginUrl = ft.getAttribute("src");
			if(loginUrl == null || loginUrl.equals("")) throw new ParserException();
			boolean isSSL = loginUrl.toLowerCase().startsWith("https");
			this.cmccLoginPageHtml = doHttpGetContainsRedirect(loginUrl,isSSL).getDataString("gb2312");
			
			mHtmlParser = Parser.createParser(cmccLoginPageHtml.toLowerCase(), "gb2312");
			NodeClassFilter scriptFilter = new NodeClassFilter(ScriptTag.class);
			nl = mHtmlParser.parse(scriptFilter);
			if(nl == null || nl.size() == 0) throw new ParserException();
			ScriptTag st = (ScriptTag)nl.elementAt(0);
			String scriptCode = st.getScriptCode();
			int index = 0;
			while(true){    //排除showurl被注释掉的情况
				index = scriptCode.indexOf("showurl",index);
				if(index == -1) throw new ParserException();
				index = index + 1;
				String beforeShowurl = scriptCode.substring(0, index);
				int lineIndex = beforeShowurl.lastIndexOf("\n");
				if(lineIndex == -1){
					if(beforeShowurl.contains("//")) continue;    //不考虑字符串中含有//的情况
					else break;
				}else{
					if(beforeShowurl.substring(lineIndex).contains("//")) continue;    //不考虑字符串中含有//的情况
					else break;
				}
			}
			int begin = scriptCode.indexOf("\"", index);
			if(begin == -1){
				begin = scriptCode.indexOf("\'", index);
				if(begin == -1) throw new ParserException();
			}
			int end = scriptCode.indexOf(";", index);
			if(end == -1) throw new ParserException();
			String url = scriptCode.substring(begin+1, end);
			url = StringUtilities.replaceWordsAll(url, " ", "");
			url = StringUtilities.replaceWordsAll(url, "\"", "");
			url = StringUtilities.replaceWordsAll(url, "\'", "");
			url = StringUtilities.replaceWordsAll(url, "+username", super.userName);
			url = StringUtilities.replaceWordsAll(url, "+math.random()", String.valueOf(MathUtilities.Random(10000)));
			url = StringUtilities.replaceWordsAll(url, "+", "");
			if(url.startsWith("./")){    //解析路径
				int httpIndex = this.cmccPageUrl.indexOf("://");
				if(httpIndex == -1) throw new ParserException();
				int mainIndex = this.cmccPageUrl.substring(httpIndex + 3).lastIndexOf("/");
				if(mainIndex == -1) throw new ParserException();
				mainIndex = this.cmccPageUrl.substring(httpIndex + 3,mainIndex + httpIndex + 3).lastIndexOf("/");
				if(mainIndex == -1) throw new ParserException();
				url = this.cmccPageUrl.substring(0, mainIndex + httpIndex + 3) + url.substring(1);
			}else if(url.startsWith("/")){
				int httpIndex = this.cmccPageUrl.indexOf("://");
				if(httpIndex == -1) throw new ParserException();
				int mainIndex = this.cmccPageUrl.substring(httpIndex + 3).indexOf("/");
				if(mainIndex == -1) url = this.cmccPageUrl + url;
				else url = this.cmccPageUrl.substring(0, mainIndex + httpIndex + 3) + url;
			}else if(!url.startsWith("http")){
				int httpIndex = this.cmccPageUrl.indexOf("://");
				if(httpIndex == -1) throw new ParserException();
				int mainIndex = this.cmccPageUrl.substring(httpIndex + 3).lastIndexOf("/");
				if(mainIndex == -1) url = this.cmccPageUrl + "/" + url;
				else url = this.cmccPageUrl.substring(0, mainIndex + httpIndex + 3) + "/" + url;
			}
			isSSL = url.toLowerCase().startsWith("https");
			String responseText = doHttpGetContainsRedirect(url,isSSL).getDataString("gb2312");
			String [] responseArr = responseText.split("@");
			if(responseArr.length != 2) throw new ParserException();
			if("rtn_0000".equalsIgnoreCase(responseArr[0])) return null;    //请求成功
			else return responseArr[1];
		}catch(ParserException e){
			return "解析错误";
		}catch(IOException e){
			return "网络错误";
		}
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
			URL url = result.getResponseURL();
			String host = url.getHost();
			String html = result.getDataString("gb2312");
			if(GUIDE_HOST.equalsIgnoreCase(host) && html.indexOf(GUIDE_HOST) >= 0) {   //若能访问到原始站点，证明已登录
				return null;
			}
			//若不能访问原始站点，即重定向到了CMCC页面
			this.cmccPageUrl = url.toString();
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
