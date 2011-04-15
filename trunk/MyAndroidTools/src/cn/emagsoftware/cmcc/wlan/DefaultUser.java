package cn.emagsoftware.cmcc.wlan;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import android.util.Log;

import com.wendell.net.http.HtmlManager;
import com.wendell.net.http.HttpConnectionManager;
import com.wendell.net.http.HttpResponseResult;

class DefaultUser extends User {
	
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
	//for redirection
	protected static final String INDICATOR_REDIRECT_PORTALURL = "portalurl";
	protected static final String INDICATOR_LOGIN_AC_NAME = "wlanacname";
	protected static final String INDICATOR_LOGIN_USER_IP = "wlanuserip";
	//form parameters in cmcc logining page
	protected static final String CMCC_LOGINFORM_NAME = "loginform";
	protected static final String INDICATOR_LOGIN_USERNAME = "USER";
	protected static final String INDICATOR_LOGIN_PASSWORD = "PWD";
	
	protected boolean isCancelLogin = false;
	protected String sessionCookie = null;
	protected String cmccPageHtml = null;
	protected Map<String,String> cmccLoginPageFields = new HashMap<String, String>();
	
	public DefaultUser(String userName,String password){
		super(userName, password);
	}
	
	@Override
	public int login() {
		// TODO Auto-generated method stub
		isCancelLogin = false;
		if(isCancelLogin) return User.RETURN_FALSE_CANCEL;      //判断是否已取消登录
		int result = isLogged();                                //判断是否已经登录
		if(result != User.RETURN_FALSE_GENERIC) return result;  //已登录或产生错误都将返回，除了未登录
		if(isCancelLogin) return User.RETURN_FALSE_CANCEL;      //判断是否已取消登录
		result = parseLoginPage(this.cmccPageHtml);             //解析CMCC登录页面
		if(result != User.RETURN_TRUE) return result;           //任何原因的解析失败，都将返回
		if(isCancelLogin) return User.RETURN_FALSE_CANCEL;      //判断是否已取消登录
		return doLogin();                                       //模拟提交表单，验证当前帐户
	}
	
	protected int parseLoginPage(String pageHtml){
		String keywordLoginReq = KEYWORD_CMCCCS + SEPARATOR + KEYWORD_LOGINREQ;
		if(keywordLoginReq != null && pageHtml.indexOf(keywordLoginReq) != -1){   //当前页面已经是CMCC登录页面
			try{
				doParseLoginPage(pageHtml);
				return User.RETURN_TRUE;
			}catch(ParserException e){
				Log.e("DefaultUser", "parsing cmcc logining page failed.", e);
				return User.RETURN_FALSE_RESPONSE_PARSE_ERROR;
			}
		}else{
			String formatHtml = HtmlManager.removeComment(pageHtml);
			String location = extractPortalUrl(formatHtml);
			if(location == null){
				location = extractHref(formatHtml);
				if(location == null){
					location = extractNextUrl(formatHtml);
					if(location == null || location.trim().length() == 0){
						return User.RETURN_FALSE_RESPONSE_PARSE_ERROR;
					}
				}
			}
			try{
				HttpResponseResult result = doHttpGetContainsRedirect(location, false);
				return parseLoginPage(result.getDataString("gb2312"));
			}catch(IOException e){
				Log.e("DefaultUser", "requesting "+location+" failed.", e);
				return User.RETURN_FALSE_NET_ERROR;
			}
		}
	}
	
	protected void doParseLoginPage(String loginPageHtml) throws ParserException{
		String formatHtml = HtmlManager.removeComment(loginPageHtml);
		Parser mHtmlParser = Parser.createParser(formatHtml, "gb2312");
		FormFilter filter = new FormFilter(CMCC_LOGINFORM_NAME);
		NodeList formList = mHtmlParser.parse(filter);
		if(formList == null || formList.size() == 0) throw new ParserException("could not find the form named '"+CMCC_LOGINFORM_NAME+"'");
		Node tag = formList.elementAt(0);
		FormTag formTag = (FormTag) tag;
		cmccLoginPageFields.clear();
		//获取提交表单的URL
		String formAction = formTag.getFormLocation();
		if(formAction != null && formAction.trim().length() > 0) {
			cmccLoginPageFields.put("action", formAction.trim());
		}
		//获取表单元素
		NodeList inputTags = formTag.getFormInputs();
		for (int j = 0; j < inputTags.size(); j++) {
			Node node = inputTags.elementAt(j);
			InputTag input = (InputTag) node;
			String attrName = input.getAttribute("name");
			String attrValue = input.getAttribute("value");
			if(attrName != null && attrValue != null) {
				cmccLoginPageFields.put(attrName.trim(), attrValue.trim()); 
			}
		}
	}
	
	protected String extractPortalUrl(String html) {
		/**
		 * <input type="hidden" name="wlanacname" value="0019.0010.100.00">
		 * <input type="hidden" name="wlanuserip" value="218.205.219.117">
		 * <input type="hidden" name="portalurl" value="http://221.176.1.140/wlan/index.php">
		 */
		Parser mHtmlParser = Parser.createParser(html.toLowerCase(), "gb2312");
		NodeClassFilter inputFilter = new NodeClassFilter(InputTag.class);
		try{
			NodeList inputList = mHtmlParser.extractAllNodesThatMatch(inputFilter);
			Map<String, String> params = new HashMap<String, String>();
			for(int i=0; i<inputList.size(); ++i) {
				Node tag = inputList.elementAt(i);
				InputTag inputTag = (InputTag)tag;
				//String attrType = inputTag.getAttribute("TYPE");
				String attrName = inputTag.getAttribute("name");
				String attrValue = inputTag.getAttribute("value");
				if(attrName != null && attrValue != null) {
					params.put(attrName.trim(), attrValue.trim()); 
				}
			}
			if (params.size() > 0) {
				String portalUrl = params.get(INDICATOR_REDIRECT_PORTALURL);
				String acname = params.get(INDICATOR_LOGIN_AC_NAME);
				String userip = params.get(INDICATOR_LOGIN_USER_IP);
				if (portalUrl != null && portalUrl.length() > 0 && acname != null && acname.length() > 0 && userip != null && userip.length() > 0) {
					StringBuffer location = new StringBuffer(portalUrl);
					location.append("?");
					location.append(INDICATOR_LOGIN_AC_NAME).append("=").append(acname);
					location.append("&");
					location.append(INDICATOR_LOGIN_USER_IP).append("=").append(userip);
					return location.toString();
				}
			}
			return null;
		}catch(ParserException e){
			return null;
		}
	}
	
	protected String extractHref(String html) {
		/**
		 * <script language="javascript">
		 * window.location.href="http://221.176.1.140/wlan/index.php?wlanacname=1037.0010.100.00&wlanuserip=117.134.26.92&ssid=&vlan=4095"; 
		 * var PORTALLOGINURL="https://221.176.1.140/wlan/bin/login.pl"; 
		 * var PORTALLOGOUTURL="https://221.176.1.140/wlan/bin/logout.pl"; 
		 * </script>
		*/
		String PREFIX = "window.location.href";
		int index = html.indexOf(PREFIX);
		if (index != -1) {
			String temp = html.substring(index + PREFIX.length());
			index = temp.indexOf("=");
			if (index != -1 && index < temp.length() - 1) {
				temp = temp.substring(index + 1).trim();
				if (temp.length() > 0) {
					int start = 0;
					int end = 1;
					if (temp.startsWith("\"")) {
						start = 1;
						end = temp.indexOf("\"", start);
					} else {
						end = temp.indexOf(";", start);
					}
					if (end > start) {
						return temp.substring(start, end);
					}
				}
			}
		}
		return null;
	}
	
	protected String extractNextUrl(String html) {
		/**
		 * redirect html sample:
		 * <HTML>
		 * <HEAD>
		 * <META HTTP-EQUIV="REFRESH" CONTENT="1;URL=http://117.134.32.234   ">
		 * <TITLE>  </TITLE>
		 * </HEAD>
		 * <?xml version="1.0" encoding="UTF-8"?>
		 * <WISPAccessGatewayParam xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://www.acmewisp.com/WISPAccessGatewayParam.xsd">
		 * <Proxy>
		 * <MessageType>110</MessageType>
		 * <NextURL>http://221.176.1.140/wlan/index.php?wlanacip=117.134.32.234&wlanuserip=117.133.202.211&wlanacname=0099.0010.100.00</NextURL>              
		 * <ResponseCode>200</ResponseCode>
		 * </Proxy>
		 * </WISPAccessGatewayParam>
		 * </HTML>
		 */
		int startTagNextURL = html.toLowerCase().indexOf("<nexturl>");
		int endTagNextURL = html.toLowerCase().indexOf("</nexturl>");
		
		// extract "NextURL" from temp HTML and re-direct 
		if (startTagNextURL != -1 && endTagNextURL != -1) {
			return html.substring(startTagNextURL + "<nexturl>".length(), endTagNextURL);
		}
		return null;
	}
	
	protected int doLogin(){
		String action = cmccLoginPageFields.remove("action");
		if(action == null || action.trim().length() == 0) action = CMCC_PORTAL_URL;
		boolean isSSL = action.startsWith(PREFIX_HTTPS);
		cmccLoginPageFields.put(INDICATOR_LOGIN_USERNAME, super.userName);
		cmccLoginPageFields.put(INDICATOR_LOGIN_PASSWORD, super.password);
		try{
			HttpResponseResult result = doHttpPostContainsRedirect(action, isSSL, cmccLoginPageFields);
			String html = result.getDataString("gb2312");
			String keywordLoginRes = KEYWORD_CMCCCS + SEPARATOR + KEYWORD_LOGINRES;
			if(keywordLoginRes == null || html.indexOf(keywordLoginRes) == -1) return User.RETURN_FALSE_RESPONSE_PARSE_ERROR;
			//获取登录后的code，通过code可以判断出登录结果
			int code = -1;
			if (!keywordLoginRes.endsWith(SEPARATOR)) keywordLoginRes += SEPARATOR;
			int start = html.indexOf(keywordLoginRes) + keywordLoginRes.length();
			String temp = html.substring(start);
			int end = temp.indexOf(SEPARATOR);
			if(end == -1) return User.RETURN_FALSE_RESPONSE_PARSE_ERROR;
			temp = temp.substring(0, end);
			try {
				code = Integer.valueOf(temp);
			} catch (NumberFormatException e){
				Log.e("DefaultUser", "parsing code from logining result page failed.", e);
				return User.RETURN_FALSE_RESPONSE_PARSE_ERROR;
			}
			if (code != 0) return User.RETURN_FALSE_NAME_OR_PWD_WRONG;   //用户名或密码有误
			try{
				//when it is login response html, extract the parameters
				doParseLoginPage(html);
			}catch(ParserException e){
				Log.e("DefaultUser", "parsing parameters from logining result page failed.", e);
			}
			return User.RETURN_TRUE;
		}catch(IOException e){
			Log.e("DefaultUser", "logining failed.", e);
			return User.RETURN_FALSE_NET_ERROR;
		}
	}
	
	@Override
	public void cancelLogin() {
		// TODO Auto-generated method stub
		isCancelLogin = true;
	}
	
	@Override
	public int isLogged() {
		// TODO Auto-generated method stub
		try{
			HttpResponseResult result = doHttpGetContainsRedirect(GUIDE_URL,false);
			String host = result.getResponseURL().getHost();
			String html = result.getDataString("gb2312");
			if(GUIDE_HOST.equalsIgnoreCase(host) && html.indexOf(GUIDE_HOST) >= 0) {   //若能访问到原始站点，证明已登录
				return User.RETURN_TRUE;
			}
			//若不能访问原始站点，即重定向到了CMCC页面
			this.cmccPageHtml = html;
			return User.RETURN_FALSE_GENERIC;
		}catch(IOException e){
			Log.e("DefaultUser", "requesting "+GUIDE_URL+" failed.", e);
			return User.RETURN_FALSE_NET_ERROR;
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
	public int logout() {
		// TODO Auto-generated method stub
		String action = cmccLoginPageFields.remove("action");
		if(action == null || action.trim().length() == 0) action = CMCC_PORTAL_URL;
		boolean isSSL = action.startsWith(PREFIX_HTTPS);
		try{
			HttpResponseResult result = doHttpPostContainsRedirect(action, isSSL, cmccLoginPageFields);
			String html = result.getDataString("gb2312");
			String keywordLoginRes = KEYWORD_CMCCCS + SEPARATOR + KEYWORD_OFFLINERES;
			if(keywordLoginRes == null || html.indexOf(keywordLoginRes) == -1) return User.RETURN_FALSE_RESPONSE_PARSE_ERROR;
			//获取登录后的code，通过code可以判断出登录结果
			int code = -1;
			if (!keywordLoginRes.endsWith(SEPARATOR)) keywordLoginRes += SEPARATOR;
			int start = html.indexOf(keywordLoginRes) + keywordLoginRes.length();
			String temp = html.substring(start);
			int end = temp.indexOf(SEPARATOR);
			if(end == -1) return User.RETURN_FALSE_RESPONSE_PARSE_ERROR;
			temp = temp.substring(0, end);
			try {
				code = Integer.valueOf(temp);
			} catch (NumberFormatException e){
				Log.e("DefaultUser", "parsing code from logouting result page failed.", e);
				return User.RETURN_FALSE_RESPONSE_PARSE_ERROR;
			}
			if (code != 0) return User.RETURN_FALSE_GENERIC;
			return User.RETURN_TRUE;
		}catch(IOException e){
			Log.e("DefaultUser", "logouting failed.", e);
			return User.RETURN_FALSE_NET_ERROR;
		}
	}
	
	protected class FormFilter extends NodeClassFilter{
		private static final long serialVersionUID = 1L;
		protected String formName = null;
		public FormFilter(String formName) {
			super(FormTag.class);
			this.formName = formName;
		}
		@Override
		public boolean accept(Node node) {
			if (super.accept(node)) {
				if (node instanceof FormTag) {
					FormTag form = (FormTag) node;
					if (formName != null & formName.equals(form.getFormName())) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
}
