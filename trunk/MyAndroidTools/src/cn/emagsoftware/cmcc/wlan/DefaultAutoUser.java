package cn.emagsoftware.cmcc.wlan;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import android.content.Context;
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
	protected static final String CMCC_LOGOUTFORM_NAME = "portal";
	
	protected Context context = null;
	protected boolean isCancelLogin = false;
	protected String sessionCookie = null;
	protected String cmccPageUrl = null;
	protected String cmccPageHtml = null;
	protected String cmccLoginPageHtml = null;
	protected Map<String,String> cmccLogoutPageFields = new HashMap<String, String>();
	
	public DefaultAutoUser(Context context){
		super();
		if(context == null) throw new NullPointerException();
		this.context = context;
	}
	
	@Override
	public String requestPassword() {
		// TODO Auto-generated method stub
		if(super.userName == null) return context.getString(context.getResources().getIdentifier("DefaultAutoUser_username_empty", "string", context.getPackageName()));
		try{
			boolean isLogged = isLogged();
			if(isLogged) return context.getString(context.getResources().getIdentifier("DefaultAutoUser_requestpwd_alreadylogin", "string", context.getPackageName()));
			Parser mHtmlParser = Parser.createParser(cmccPageHtml.toLowerCase(), "gb2312");
			NodeClassFilter frameFilter = new NodeClassFilter(FrameTag.class);
			NodeList nl = mHtmlParser.parse(frameFilter);
			if(nl == null || nl.size() == 0) throw new ParserException();
			FrameTag ft = (FrameTag)nl.elementAt(0);
			String loginUrl = ft.getAttribute("src");
			if(loginUrl == null || loginUrl.equals("")) throw new ParserException();
			this.cmccLoginPageHtml = doHttpGetContainsRedirect(loginUrl).getDataString("gb2312");
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
			String responseText = doHttpGetContainsRedirect(url).getDataString("gb2312");
			String [] responseArr = responseText.split("@");
			if(responseArr.length != 2) throw new ParserException();
			if("rtn_0000".equalsIgnoreCase(responseArr[0])) return null;    //请求成功
			else return responseArr[1];
		}catch(IOException e){
			Log.e("DefaultAutoUser", "requestPassword failed.", e);
			return context.getString(context.getResources().getIdentifier("DefaultAutoUser_net_error", "string", context.getPackageName()));
		}catch(ParserException e){
			Log.e("DefaultAutoUser", "requestPassword failed.", e);
			return context.getString(context.getResources().getIdentifier("DefaultAutoUser_parse_error", "string", context.getPackageName()));
		}
	}
	
	@Override
	public String login() {
		// TODO Auto-generated method stub
		isCancelLogin = false;
		if(isCancelLogin) return context.getString(context.getResources().getIdentifier("DefaultAutoUser_login_cancel", "string", context.getPackageName()));
		if(super.userName == null) return context.getString(context.getResources().getIdentifier("DefaultAutoUser_username_empty", "string", context.getPackageName()));
		if(super.password == null) return context.getString(context.getResources().getIdentifier("DefaultAutoUser_password_empty", "string", context.getPackageName()));
		try{
			boolean isLogged = isLogged();
			if(isLogged) return null;    //已经登录，将直接返回null表示登录成功
			if(isCancelLogin) return context.getString(context.getResources().getIdentifier("DefaultAutoUser_login_cancel", "string", context.getPackageName()));
			Parser mHtmlParser = Parser.createParser(cmccPageHtml.toLowerCase(), "gb2312");
			NodeClassFilter frameFilter = new NodeClassFilter(FrameTag.class);
			NodeList nl = mHtmlParser.parse(frameFilter);
			if(nl == null || nl.size() == 0) throw new ParserException();
			FrameTag ft = (FrameTag)nl.elementAt(0);
			String loginUrl = ft.getAttribute("src");
			if(loginUrl == null || loginUrl.equals("")) throw new ParserException();
			this.cmccLoginPageHtml = doHttpGetContainsRedirect(loginUrl).getDataString("gb2312");
			if(isCancelLogin) return context.getString(context.getResources().getIdentifier("DefaultAutoUser_login_cancel", "string", context.getPackageName()));
			mHtmlParser = Parser.createParser(cmccLoginPageHtml.toLowerCase(), "gb2312");
			FormFilter filter = new FormFilter("autologin");
			NodeList formList = mHtmlParser.parse(filter);
			if(formList == null || formList.size() == 0) throw new ParserException();
			FormTag formTag = (FormTag)formList.elementAt(0);
			String submitUrl = formTag.getFormLocation();
			if(submitUrl == null || submitUrl.equals("")) throw new ParserException();
			//获取表单元素
			Map<String,String> params = new HashMap<String, String>();
			NodeList inputTags = formTag.getFormInputs();
			for (int j = 0; j < inputTags.size(); j++) {
				Node node = inputTags.elementAt(j);
				InputTag input = (InputTag) node;
				String attrName = input.getAttribute("name");
				String attrValue = input.getAttribute("value");
				if(attrName != null && attrValue != null) {
					params.put(attrName.trim(), attrValue.trim()); 
				}
			}
			params.put("autousername", super.userName);
			params.put("autopassword", super.password);
			if(isCancelLogin) return context.getString(context.getResources().getIdentifier("DefaultAutoUser_login_cancel", "string", context.getPackageName()));
			String loginResult = doHttpPostContainsRedirect(submitUrl,params).getDataString("gb2312");
			return parseLoginResult(loginResult);
		}catch(IOException e){
			Log.e("DefaultAutoUser", "logining failed.", e);
			return context.getString(context.getResources().getIdentifier("DefaultAutoUser_net_error", "string", context.getPackageName()));
		}catch(ParserException e){
			Log.e("DefaultAutoUser", "logining failed.", e);
			return context.getString(context.getResources().getIdentifier("DefaultAutoUser_parse_error", "string", context.getPackageName()));
		}
	}
	
	protected String parseLoginResult(String loginResult) throws ParserException,IOException{
		int alertIndex = loginResult.indexOf("alert");
		int confirmIndex = loginResult.indexOf("confirm");
		if(alertIndex == -1 && confirmIndex == -1) {    //登录成功
/*			try{
				//获取表单参数，为下线提供条件
				Parser mParser = Parser.createParser(loginResult.toLowerCase(), "gb2312");
				NodeClassFilter fFilter = new NodeClassFilter(FrameTag.class);
				NodeList nodeL = mParser.parse(fFilter);
				if(nodeL == null || nodeL.size() == 0) throw new ParserException();
				FrameTag fTag = (FrameTag)nodeL.elementAt(0);
				String frameUrl = fTag.getAttribute("src");
				if(frameUrl == null || frameUrl.equals("")) throw new ParserException();
				String offLinePage = doHttpGetContainsRedirect(frameUrl).getDataString("gb2312");
				mParser = Parser.createParser(offLinePage, "gb2312");
				FormFilter formFilter = new FormFilter(CMCC_LOGOUTFORM_NAME);
				NodeList formLi = mParser.parse(formFilter);
				if(formLi == null || formLi.size() == 0) throw new ParserException("could not find the form named '"+CMCC_LOGOUTFORM_NAME+"'");
				Node tag = formLi.elementAt(0);
				FormTag form = (FormTag) tag;
				cmccLogoutPageFields.clear();
				//获取提交表单的URL
				String formAction = form.getFormLocation();
				if(formAction == null || formAction.trim().length() == 0){
					int index = offLinePage.indexOf("thisform.action");
					if(index == -1) throw new ParserException();
					int beginAction = offLinePage.indexOf("\'", index);
					if(beginAction == -1){
						beginAction = offLinePage.indexOf("\"", index);
						if(beginAction == -1) throw new ParserException();
					}
					int endAction = offLinePage.indexOf("\'", beginAction + 1);
					if(endAction == -1){
						endAction = offLinePage.indexOf("\"", beginAction + 1);
						if(endAction == -1) throw new ParserException();
					}
					String action = offLinePage.substring(beginAction + 1, endAction).trim();
					if(action.toLowerCase().startsWith("http")){
						formAction = action;
					}else{
						int urlIndex = frameUrl.lastIndexOf("/");
						if(urlIndex == -1) throw new ParserException();
						formAction = frameUrl.substring(0, urlIndex) + "/" + action;
					}
				}
				cmccLogoutPageFields.put("action", formAction.trim());
				//获取表单元素
				NodeList inputs = form.getFormInputs();
				for (int j = 0; j < inputs.size(); j++) {
					Node node = inputs.elementAt(j);
					InputTag input = (InputTag) node;
					String type = input.getAttribute("type");
					if("checkbox".equalsIgnoreCase(type) || "button".equalsIgnoreCase(type)) continue;
					String attrName = input.getAttribute("name");
					String attrValue = input.getAttribute("value");
					if(attrName != null && attrValue != null) {
						cmccLogoutPageFields.put(attrName.trim(), attrValue.trim()); 
					}
				}
				//暂时直接写死该项的值，以后将作修改
				cmccLogoutPageFields.put("logouttype", "TYPESUBMIT");
			}catch(ParserException e){
				Log.e("DefaultAutoUser", "deal logining result page failed.", e);
			}catch(IOException e){
				Log.e("DefaultAutoUser", "deal logining result page failed.", e);
			}catch(RuntimeException e){
				Log.e("DefaultAutoUser", "deal logining result page failed.", e);
			}*/
			return null;
		}else if(confirmIndex != -1){    //当前登录的用户已在线
			int locationIndex = loginResult.indexOf("window.location",confirmIndex);
			if(locationIndex == -1) throw new ParserException();
			int beginIndex = loginResult.indexOf("\'",locationIndex);
			if(beginIndex == -1){
				beginIndex = loginResult.indexOf("\"",locationIndex);
				if(beginIndex == -1) throw new ParserException();
			}
			int endIndex = loginResult.indexOf("\'",beginIndex + 1);
			if(endIndex == -1){
				endIndex = loginResult.indexOf("\"",beginIndex + 1);
				if(endIndex == -1) throw new ParserException();
			}
			String secondLoginUrl = loginResult.substring(beginIndex + 1, endIndex);
			String secondLoginResult = doHttpGetContainsRedirect(secondLoginUrl).getDataString("gb2312");
			return parseLoginResult(secondLoginResult);
		}else{    //登录失败
			int begin = loginResult.indexOf("\"",alertIndex);
			if(begin == -1){
				begin = loginResult.indexOf("\'",alertIndex);
				if(begin == -1) throw new ParserException();
			}
			int end = loginResult.indexOf("\"",begin + 1);
			if(end == -1){
				end = loginResult.indexOf("\'",begin + 1);
				if(end == -1) throw new ParserException();
			}
			return loginResult.substring(begin + 1, end);
		}		
	}
	
	@Override
	public void cancelLogin() {
		// TODO Auto-generated method stub
		isCancelLogin = true;
	}
	
	@Override
	public boolean isLogged() throws IOException {
		// TODO Auto-generated method stub
		HttpResponseResult result = doHttpGetContainsRedirect(GUIDE_URL);
		URL url = result.getResponseURL();
		String host = url.getHost();
		String html = result.getDataString("gb2312");
		if(GUIDE_HOST.equalsIgnoreCase(host) && html.indexOf(GUIDE_HOST) >= 0) {   //若能访问到原始站点，证明已登录
			return true;
		}
		//若不能访问原始站点，即重定向到了CMCC页面
		this.cmccPageUrl = url.toString();
		this.cmccPageHtml = html;
		return false;
	}
	
	protected HttpResponseResult doHttpGetContainsRedirect(String url) throws IOException {
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
		if(sessionCookie != null){
			values = new ArrayList<String>();
			values.add(sessionCookie);
			requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_COOKIE, values);			
		}
		HttpResponseResult result = HttpConnectionManager.doGet(url, "gb2312", false, 15000, requestHeaders);
		int code = result.getResponseCode();
		while(code != HttpURLConnection.HTTP_OK && code == HttpURLConnection.HTTP_MOVED_TEMP){
			List<String> headerValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_LOCATION.toLowerCase());
			String location = headerValues.get(0);
			result = HttpConnectionManager.doGet(location, "gb2312", false, 15000, requestHeaders);
			code = result.getResponseCode();
		}
		if(code != HttpURLConnection.HTTP_OK) throw new IOException("requesting url returns code:"+code);
		//以下获取cookie
		List<String> setCookieValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_SET_COOKIE.toLowerCase());
		if(setCookieValues != null){
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
		}
		return result;
	}
	
	protected HttpResponseResult doHttpPostContainsRedirect(String url,Map<String,String> params) throws IOException{
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
		if(sessionCookie != null){
			values = new ArrayList<String>();
			values.add(sessionCookie);
			requestHeaders.put(HttpConnectionManager.HEADER_REQUEST_COOKIE, values);			
		}
		HttpResponseResult result = HttpConnectionManager.doPost(url, "gb2312", false, 15000, requestHeaders, params);
		int code = result.getResponseCode();
		while(code != HttpURLConnection.HTTP_OK && code == HttpURLConnection.HTTP_MOVED_TEMP){
			List<String> headerValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_LOCATION.toLowerCase());
			String location = headerValues.get(0);
			result = HttpConnectionManager.doGet(location, "gb2312", false, 15000, requestHeaders);
			code = result.getResponseCode();
		}
		if(code != HttpURLConnection.HTTP_OK) throw new IOException("requesting url returns code:"+code);
		//以下获取cookie
		List<String> setCookieValues = result.getResponseHeaders().get(HttpConnectionManager.HEADER_RESPONSE_SET_COOKIE.toLowerCase());
		if(setCookieValues != null){
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
		}
		return result;
	}
	
	@Override
	public String logout() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("this method has not supported yet.");
/*		String action = cmccLogoutPageFields.get("action");
		try{
			HttpResponseResult result = doHttpPostContainsRedirect(action, cmccLogoutPageFields);
			String html = result.getDataString("gb2312");
			
			Log.i("DefaultAutoUser",html);
			
			return null;
		}catch(IOException e){
			Log.e("DefaultAutoUser", "logouting failed.", e);
			return context.getString(context.getResources().getIdentifier("DefaultAutoUser_net_error", "string", context.getPackageName()));
		}*/
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
