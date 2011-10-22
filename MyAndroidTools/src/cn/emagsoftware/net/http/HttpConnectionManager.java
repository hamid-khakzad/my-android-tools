package cn.emagsoftware.net.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

/**
 * Http Connection Manager
 * @author Wendell
 * @version 2.91
 */
public final class HttpConnectionManager {
	
	public static final String HTTPS_PREFIX = "https";
	
	public static final String HEADER_REQUEST_ACCEPT_LANGUAGE = "Accept-Language";
	public static final String HEADER_REQUEST_CONNECTION = "Connection";
	public static final String HEADER_REQUEST_CACHE_CONTROL = "Cache-Control";
	public static final String HEADER_REQUEST_ACCEPT_CHARSET = "Accept-Charset";
	public static final String HEADER_REQUEST_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_REQUEST_USER_AGENT = "User-Agent";
	public static final String HEADER_REQUEST_COOKIE = "Cookie";
	
	public static final String HEADER_RESPONSE_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_RESPONSE_LOCATION = "Location";
	public static final String HEADER_RESPONSE_SET_COOKIE = "Set-Cookie";
	
	public static final int REDIRECT_MAX_COUNT = 10;
	public static final int CMWAP_CHARGEPAGE_MAX_COUNT = 5;
	
	private static boolean isKeepSession = false;
	private static Map<String, List<String>> sessions = Collections.synchronizedMap(new HashMap<String, List<String>>());
	
	private static boolean isUseCMWap = false;
	
	private HttpConnectionManager(){}
	
	public static void setKeepSession(boolean isKeepSession){
		HttpConnectionManager.isKeepSession = isKeepSession;
	}
	
	public static void clearSessions(){
		sessions.clear();
	}
	
	/**
	 * <p>设置是否使用了中国移动CMWap接入
	 * @param isUseCMWap
	 */
	public static void setUseCMWap(boolean isUseCMWap){
		HttpConnectionManager.isUseCMWap = isUseCMWap;
	}
	
	/**
	 * 进行http get请求
	 * @param url 请求的url
	 * @param urlEnc URL编码的字符集，将会以此字符集自动进行URL编码
	 * @param followRedirects 是否自动重定向
	 * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
	 * @param requestHeaders 请求头，不需要时可传null
	 * @return HttpResponseResultStream实例
	 * @throws IOException
	 */
	public static HttpResponseResultStream doGetForStream(String url,String urlEnc,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders) throws IOException{
		HttpURLConnection httpConn = null;
		InputStream input = null;
		try{
			httpConn = openConnection(url, urlEnc, "GET", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, null);
			HttpResponseResultStream result = new HttpResponseResultStream();
			result.setResponseURL(httpConn.getURL());
			int rspCode = httpConn.getResponseCode();
			result.setResponseCode(rspCode);
			result.setResponseHeaders(httpConn.getHeaderFields());
			if(isKeepSession) saveSession(result);
			input = httpConn.getInputStream();
			result.setResultStream(input);
			result.setHttpURLConn(httpConn);
			return result;
		}catch(IOException e){
			try{
				if(input != null) input.close();
			}finally{
				if(httpConn != null) httpConn.disconnect();
			}
			throw e;
		}
	}
	
	public static HttpResponseResult doGet(String url,String urlEnc,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders) throws IOException{
		HttpResponseResultStream result = doGetForStream(url,urlEnc,followRedirects,connOrReadTimeout,requestHeaders);
		result.generateData();
		return result;
	}
	
	/**
	 * 进行http post请求，将以值为application/x-www-form-urlencoded的Content-Type来提交键值对参数
	 * @param url 请求的url
	 * @param urlEnc URL编码的字符集，将会以此字符集自动进行URL编码
	 * @param followRedirects 是否自动重定向
	 * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
	 * @param requestHeaders 请求头，不需要时可传null
	 * @param postParams 提交的POST参数，不需要时可传null
	 * @return HttpResponseResultStream实例
	 * @throws IOException
	 */
	public static HttpResponseResultStream doPostForStream(String url,String urlEnc,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders,Map<String,String> postParams) throws IOException{
		HttpURLConnection httpConn = null;
		InputStream input = null;
		try{
			if(requestHeaders == null) requestHeaders = new HashMap<String, List<String>>();
			List<String> contentTypes = requestHeaders.get(HEADER_REQUEST_CONTENT_TYPE);
			if(contentTypes == null) contentTypes = new ArrayList<String>();
			contentTypes.add("application/x-www-form-urlencoded");
			requestHeaders.put(HEADER_REQUEST_CONTENT_TYPE, contentTypes);
			byte[] paramsData = null;
			if(postParams != null){
				String postParamsStr = HttpManager.concatParams(postParams);
				postParamsStr = HttpManager.encodeParams(postParamsStr, urlEnc);    //post参数时，需对参数进行url编码
				paramsData = postParamsStr.getBytes();    //经过url编码之后的参数只含有英文字符，可用任意字符集对其编码
			}
			httpConn = openConnection(url, urlEnc, "POST", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, paramsData);
			HttpResponseResultStream result = new HttpResponseResultStream();
			result.setResponseURL(httpConn.getURL());
			int rspCode = httpConn.getResponseCode();
			result.setResponseCode(rspCode);
			result.setResponseHeaders(httpConn.getHeaderFields());
			if(isKeepSession) saveSession(result);
			input = httpConn.getInputStream();
			result.setResultStream(input);
			result.setHttpURLConn(httpConn);
			return result;
		}catch(IOException e){
			try{
				if(input != null) input.close();
			}finally{
				if(httpConn != null) httpConn.disconnect();
			}
			throw e;
		}
	}
	
	public static HttpResponseResult doPost(String url,String urlEnc,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders,Map<String,String> postParams) throws IOException{
		HttpResponseResultStream result = doPostForStream(url,urlEnc,followRedirects,connOrReadTimeout,requestHeaders,postParams);
		result.generateData();
		return result;
	}
	
	/**
	 * 进行http post请求，将以值为application/octet-stream的Content-Type来提交数据
	 * @param url 请求的url
	 * @param urlEnc URL编码的字符集，将会以此字符集自动进行URL编码
	 * @param followRedirects 是否自动重定向
	 * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
	 * @param requestHeaders 请求头，不需要时可传null
	 * @param postData 提交的POST数据，不需要时可传null
	 * @return HttpResponseResultStream实例
	 * @throws IOException
	 */
	public static HttpResponseResultStream doPostForStream(String url,String urlEnc,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders,byte[] postData) throws IOException{
		HttpURLConnection httpConn = null;
		InputStream input = null;
		try{
			if(requestHeaders == null) requestHeaders = new HashMap<String, List<String>>();
			List<String> contentTypes = requestHeaders.get(HEADER_REQUEST_CONTENT_TYPE);
			if(contentTypes == null) contentTypes = new ArrayList<String>();
			contentTypes.add("application/octet-stream");
			requestHeaders.put(HEADER_REQUEST_CONTENT_TYPE, contentTypes);
			if(postData == null) postData = new byte[]{};    //貌似针对application/octet-stream的情况必须这样处理，否则会抛出FileNotFoundException，这可能是Android的底层实现有缺陷
			httpConn = openConnection(url, urlEnc, "POST", followRedirects, connOrReadTimeout, 0, 0, requestHeaders, postData);
			HttpResponseResultStream result = new HttpResponseResultStream();
			result.setResponseURL(httpConn.getURL());
			int rspCode = httpConn.getResponseCode();
			result.setResponseCode(rspCode);
			result.setResponseHeaders(httpConn.getHeaderFields());
			if(isKeepSession) saveSession(result);
			input = httpConn.getInputStream();
			result.setResultStream(input);
			result.setHttpURLConn(httpConn);
			return result;
		}catch(IOException e){
			try{
				if(input != null) input.close();
			}finally{
				if(httpConn != null) httpConn.disconnect();
			}
			throw e;
		}
	}
	
	public static HttpResponseResult doPost(String url,String urlEnc,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders,byte[] postData) throws IOException{
		HttpResponseResultStream result = doPostForStream(url,urlEnc,followRedirects,connOrReadTimeout,requestHeaders,postData);
		result.generateData();
		return result;
	}
	
	/**
	 * 返回HttpURLConnection实例
	 * @param url 请求的url
	 * @param urlEnc URL编码的字符集，将会以此字符集自动进行URL编码
	 * @param method 请求的方式，如GET,POST
	 * @param followRedirects 是否自动重定向
	 * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
	 * @param currentRedirectCount 当前是第几次重定向
	 * @param currentCMWapChargePageCount 当前是第几次出现CMWap资费提示页面
	 * @param requestHeaders 请求头，不需要时可传null
	 * @param postData method为POST时提交的数据，不需要时可传null
	 * @return HttpURLConnection实例
	 * @throws IOException
	 */
	private static HttpURLConnection openConnection(String url,String urlEnc,String method,boolean followRedirects,int connOrReadTimeout,int currentRedirectCount,int currentCMWapChargePageCount,Map<String,List<String>> requestHeaders,byte[] postData) throws IOException{
		if(currentRedirectCount < 0) throw new IllegalArgumentException("current redirect count can not set to below zero.");
		if(currentRedirectCount > REDIRECT_MAX_COUNT) throw new IOException("too many redirect times.");
		if(currentCMWapChargePageCount < 0) throw new IllegalArgumentException("current CMWap charge page count can not set to below zero.");
		if(currentCMWapChargePageCount > CMWAP_CHARGEPAGE_MAX_COUNT) throw new IOException("too many showing CMWap charge page times.");
		String packUrl = HttpManager.encodeURL(url, urlEnc);
		URL myUrl = new URL(packUrl);
		String prefix = null;
		if(isUseCMWap){
			prefix = myUrl.getAuthority();
			packUrl = "http://10.0.0.172".concat(myUrl.getPath());
			String query = myUrl.getQuery();
			if(query != null) packUrl = packUrl.concat("?").concat(query);
			myUrl = new URL(packUrl);
		}
		boolean isSSL = packUrl.toLowerCase().startsWith(HTTPS_PREFIX);
		HttpURLConnection httpConn = null;
		OutputStream output = null;
		try{
			if(isSSL) {
				SSLContext sslCont = SSLContext.getInstance("TLS");
				sslCont.init(null, new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sslCont.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier(myUrl.getHost()));
				httpConn = (HttpsURLConnection)myUrl.openConnection();
			} else {
				httpConn = (HttpURLConnection)myUrl.openConnection();
			}
			httpConn.setRequestMethod(method);
			HttpURLConnection.setFollowRedirects(false);
			httpConn.setInstanceFollowRedirects(false);
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setReadTimeout(connOrReadTimeout);
			httpConn.setConnectTimeout(connOrReadTimeout);
			if(isKeepSession){
				String session = querySession(myUrl);
				if(session != null){
					httpConn.addRequestProperty(HEADER_REQUEST_COOKIE, session);
				}
			}
			if(isUseCMWap){
				httpConn.addRequestProperty("X-Online-Host", prefix);
			}
			if(requestHeaders != null){
				Iterator<String> keys = requestHeaders.keySet().iterator();
				while(keys.hasNext()){
					String key = keys.next();
					List<String> values = requestHeaders.get(key);
					for(String value:values){
						httpConn.addRequestProperty(key, value);
					}
				}
			}
			if(method.equalsIgnoreCase("POST") && postData != null){
				output = httpConn.getOutputStream();
				BufferedOutputStream buffOutput = new BufferedOutputStream(output);
				buffOutput.write(postData);
				buffOutput.flush();
				output.close();
			}
			int rspCode = httpConn.getResponseCode();
			if(rspCode == HttpURLConnection.HTTP_OK){
				if(isUseCMWap){
					String contentType = httpConn.getHeaderField(HEADER_RESPONSE_CONTENT_TYPE);
					if(contentType != null && contentType.indexOf("vnd.wap.wml") != -1){    //CMWap有时会出现资费提示页面
						InputStream input = null;
						try{
							input = httpConn.getInputStream();
							BufferedInputStream buffInput = new BufferedInputStream(input);
							ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
							byte[] b = new byte[2*1024];
							int len;
							while ((len = buffInput.read(b)) > 0) {
								tempOutput.write(b,0,len);
							}
							String wmlStr = new String(tempOutput.toByteArray(),"UTF-8");
							//解析资费提示页面中的URL
							XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
							XmlPullParser xmlParser = factory.newPullParser();
							xmlParser.setInput(new StringReader(wmlStr));
							String parseUrl = null;
							boolean onEnterForward = false;
							int eventType = xmlParser.getEventType();
							while (eventType != XmlPullParser.END_DOCUMENT) {
								switch (eventType) {
								case XmlPullParser.START_TAG:
									String tagName = xmlParser.getName().toLowerCase();
									if ("onevent".equals(tagName)) {
										String s = xmlParser.getAttributeValue(null, "type").toLowerCase();
										if ("onenterforward".equals(s)) onEnterForward = true;
									} else if ("go".equals(tagName)){
										if(onEnterForward) parseUrl = xmlParser.getAttributeValue(null, "href");
									}
									break;
								}
								if(parseUrl != null) break;
								eventType = xmlParser.next();
							}
							if(parseUrl == null || parseUrl.equals("")) parseUrl = url;
							return openConnection(parseUrl,urlEnc,method,followRedirects,connOrReadTimeout,currentRedirectCount,++currentCMWapChargePageCount,requestHeaders,postData);
						}finally{
							try{
								if(input != null) input.close();
							}finally{
								httpConn.disconnect();
							}
						}
					}
				}
				return httpConn;
			}else if(rspCode == HttpURLConnection.HTTP_MOVED_PERM || rspCode == HttpURLConnection.HTTP_MOVED_TEMP || rspCode == HttpURLConnection.HTTP_SEE_OTHER){
				if(!followRedirects) return httpConn;
				//implements 'followRedirects' by myself,because the method of setFollowRedirects and setInstanceFollowRedirects have existed some problems.
				String location = httpConn.getHeaderField(HEADER_RESPONSE_LOCATION);
				if(location == null) throw new IOException("Redirects failed.Could not find the location header.");
				if(location.toLowerCase().indexOf(myUrl.getProtocol() + "://") < 0) location = myUrl.getProtocol() + "://" + myUrl.getHost() + location;
				httpConn.disconnect();
				return openConnection(location,urlEnc,"GET",followRedirects,connOrReadTimeout,++currentRedirectCount,currentCMWapChargePageCount,requestHeaders,null);
			}else{
				throw new IOException("the http response code is:" + rspCode);
			}
		}catch(IOException e){
			try{
				if(output != null) output.close();
			}finally{
				if(httpConn != null) httpConn.disconnect();
			}
			throw e;
		}catch(Exception e){
			try{
				if(output != null) output.close();
			}finally{
				if(httpConn != null) httpConn.disconnect();
			}
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * <p>根据url从缓存中查找能够维持session的cookie值，若未找到将返回null
	 * @param url
	 * @return
	 */
	private static String querySession(URL url){
		String host = url.getHost().toLowerCase();
		if(host.equals("localhost")) host = "127.0.0.1";
		int port = url.getPort();
		if(port == -1) port = url.getDefaultPort();
		String authority = host.concat(":").concat(String.valueOf(port));
		String path = url.getPath();
		List<String> sessionCookies = null;
		if(path.equals("") || path.equals("/")){
			sessionCookies = sessions.get(authority);
		}else{
			int index = path.indexOf("/", 1);
			if(index != -1) path = path.substring(0, index);
			sessionCookies = sessions.get(authority.concat(path));
			if(sessionCookies == null){
				sessionCookies = sessions.get(authority);
			}
		}
		if(sessionCookies == null) return null;
		StringBuffer sb = new StringBuffer();
		for(int i = 0;i < sessionCookies.size();i++){
			if(i != 0) sb.append(";");
			sb.append(sessionCookies.get(i));
		}
		String sessionCookiesStr = sb.toString();
		Log.i("HttpConnectionManager", "queried session("+sessionCookiesStr+") for url "+url);
		return sessionCookiesStr;
	}
	
	/**
	 * <p>保存当前响应结果中用于维持session的cookie值
	 * @param result
	 */
	private static void saveSession(HttpResponseResult result){
		Map<String, List<String>> headers = result.getResponseHeaders();
		if(headers != null){
			List<String> cookies = headers.get(HEADER_RESPONSE_SET_COOKIE.toLowerCase());    //在Android平台的实现中必须以小写的key来获取以List形式返回的响应头
			if(cookies != null){
				URL url = result.getResponseURL();
				List<String> sessionCookies = new ArrayList<String>();
				for(String cookie:cookies){
					if(cookie != null){
						String[] cookieArr = cookie.split(";");
						String sessionCookie = null;
						for(String perCookie:cookieArr){
							perCookie = perCookie.trim();
							if(perCookie.startsWith("JSESSIONID=") || perCookie.startsWith("PHPSESSID=")){
								sessionCookie = perCookie;
								break;
							}
						}
						if(sessionCookie == null) sessionCookie = cookieArr[0].trim();
						Log.i("HttpConnectionManager", "prepare to save session("+sessionCookie+") for url "+url+"...");
						sessionCookies.add(sessionCookie);
					}
				}
				if(sessionCookies.size() == 0) return;
				String host = url.getHost().toLowerCase();
				if(host.equals("localhost")) host = "127.0.0.1";
				int port = url.getPort();
				if(port == -1) port = url.getDefaultPort();
				String authority = host.concat(":").concat(String.valueOf(port));
				String path = url.getPath();
				if(path.equals("") || path.equals("/")){
					sessions.put(authority, sessionCookies);
				}else{
					int index = path.indexOf("/", 1);
					if(index != -1) path = path.substring(0, index);
					sessions.put(authority.concat(path), sessionCookies);
				}
			}
		}
	}
	
	private static class MyX509TrustManager implements X509TrustManager{
		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			// TODO Auto-generated method stub
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			// TODO Auto-generated method stub
		}
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private static class MyHostnameVerifier implements HostnameVerifier{
		private String hostname;
		public MyHostnameVerifier(String hostname){
			this.hostname = hostname;
		}
		@Override
		public boolean verify(String hostname, SSLSession session) {
			// TODO Auto-generated method stub
			if(this.hostname.equals(hostname)) return true;
			return false;
		}
	}
	
}
