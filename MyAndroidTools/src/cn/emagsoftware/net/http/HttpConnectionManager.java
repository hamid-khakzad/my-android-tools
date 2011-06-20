package cn.emagsoftware.net.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Http Connection Manager
 * @author Wendell
 * @version 1.6
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
	
	public static final String HEADER_RESPONSE_LOCATION = "Location";
	public static final String HEADER_RESPONSE_SET_COOKIE = "Set-Cookie";
	
	public static final int REDIRECT_MAX_COUNT = 10;
	
	private HttpConnectionManager(){}
	
	/**
	 * ����http get����
	 * @param url �����url
	 * @param urlEnc URL������ַ����������Դ��ַ����Զ�����URL����
	 * @param followRedirects �Ƿ��Զ��ض���
	 * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
	 * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
	 * @return HttpResponseResultʵ��
	 * @throws IOException
	 */
	public static HttpResponseResult doGet(String url,String urlEnc,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders) throws IOException{
		HttpURLConnection httpConn = null;
		InputStream input = null;
		try{
			httpConn = openConnection(url, urlEnc, "GET", followRedirects, connOrReadTimeout, 0, requestHeaders, null);
			HttpResponseResult result = new HttpResponseResult();
			result.setResponseURL(httpConn.getURL());
			int rspCode = httpConn.getResponseCode();
			result.setResponseCode(rspCode);
			result.setResponseHeaders(httpConn.getHeaderFields());
			if(rspCode != HttpURLConnection.HTTP_OK) return result;
			input = httpConn.getInputStream();
			BufferedInputStream buffInput = new BufferedInputStream(input);
			ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
			byte[] b = new byte[2*1024];
			int len;
			while ((len = buffInput.read(b)) > 0) {
				tempOutput.write(b,0,len);
			}
			result.setData(tempOutput.toByteArray());
			return result;
		}finally{
			try{
				if(input != null) input.close();
			}finally{
				if(httpConn != null) httpConn.disconnect();
			}
		}
	}
	
	/**
	 * ����http post����
	 * @param url �����url
	 * @param urlEnc URL������ַ����������Դ��ַ����Զ�����URL����
	 * @param followRedirects �Ƿ��Զ��ض���
	 * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
	 * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
	 * @param postParams �ύ��POST���ݣ�����Ҫʱ�ɴ�null�������Դ����urlEnc�ַ������б���
	 * @return HttpResponseResultʵ��
	 * @throws IOException
	 */
	public static HttpResponseResult doPost(String url,String urlEnc,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders,Map<String,String> postParams) throws IOException{
		HttpURLConnection httpConn = null;
		InputStream input = null;
		try{
			httpConn = openConnection(url, urlEnc, "POST", followRedirects, connOrReadTimeout, 0, requestHeaders, postParams);
			HttpResponseResult result = new HttpResponseResult();
			result.setResponseURL(httpConn.getURL());
			int rspCode = httpConn.getResponseCode();
			result.setResponseCode(rspCode);
			result.setResponseHeaders(httpConn.getHeaderFields());
			if(rspCode != HttpURLConnection.HTTP_OK) return result;
			input = httpConn.getInputStream();
			BufferedInputStream buffInput = new BufferedInputStream(input);
			ByteArrayOutputStream tempOutput = new ByteArrayOutputStream();
			byte[] b = new byte[2*1024];
			int len;
			while ((len = buffInput.read(b)) > 0) {
				tempOutput.write(b,0,len);
			}
			result.setData(tempOutput.toByteArray());
			return result;
		}finally{
			try{
				if(input != null) input.close();
			}finally{
				if(httpConn != null) httpConn.disconnect();
			}
		}
	}
	
	/**
	 * ����HttpURLConnectionʵ��
	 * @param url �����url
	 * @param urlEnc URL������ַ����������Դ��ַ����Զ�����URL����
	 * @param method ����ķ�ʽ����GET,POST
	 * @param followRedirects �Ƿ��Զ��ض���
	 * @param connOrReadTimeout ���ӺͶ�ȡ�ĳ�ʱʱ�䣬�Ժ���Ϊ��λ����Ϊ0��ʾ������ʱ
	 * @param currentRedirectCount ��ǰ�ǵڼ����ض���
	 * @param requestHeaders ����ͷ������Ҫʱ�ɴ�null
	 * @param postParams methodΪPOSTʱ�ύ�����ݣ�����Ҫʱ�ɴ�null�������Դ����urlEnc�ַ������б���
	 * @return HttpURLConnectionʵ��
	 * @throws IOException
	 */
	private static HttpURLConnection openConnection(String url,String urlEnc,String method,boolean followRedirects,int connOrReadTimeout,int currentRedirectCount,Map<String,List<String>> requestHeaders,Map<String,String> postParams) throws IOException{
		if(currentRedirectCount < 0) throw new IllegalArgumentException("current redirect count can not set to below zero.");
		if(currentRedirectCount > REDIRECT_MAX_COUNT) throw new IOException("too many redirect times.");
		url = HttpManager.encodeURL(url, urlEnc);
		boolean isSSL = url.toLowerCase().startsWith(HTTPS_PREFIX);
		URL myUrl = new URL(url);
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
			if(method.equalsIgnoreCase("POST") && postParams != null){
				Iterator<String> keys = postParams.keySet().iterator();
				StringBuffer paramsBuff = new StringBuffer();
				while(keys.hasNext()){
					String key = keys.next();
					String value = postParams.get(key);
					paramsBuff.append(key.concat("=").concat(value).concat("&"));
				}
				String paramsStr = paramsBuff.toString();
				if(!paramsStr.equals("")) paramsStr = paramsStr.substring(0, paramsStr.length()-1);
				output = httpConn.getOutputStream();
				BufferedOutputStream buffOutput = new BufferedOutputStream(output);
				buffOutput.write(HttpManager.encodeParams(paramsStr, urlEnc).getBytes());
				buffOutput.flush();
				output.close();
			}
			if(!followRedirects) return httpConn;
			//implements 'followRedirects' by myself,because the method of setFollowRedirects and setInstanceFollowRedirects have existed some problems.
			int rspCode = httpConn.getResponseCode();
			if(rspCode != HttpURLConnection.HTTP_MOVED_PERM && rspCode != HttpURLConnection.HTTP_MOVED_TEMP && rspCode != HttpURLConnection.HTTP_SEE_OTHER) return httpConn;
			String location = httpConn.getHeaderField(HEADER_RESPONSE_LOCATION);
			if(location == null) throw new IOException("Redirects failed.Could not find the location header.");
			if(location.toLowerCase().indexOf(myUrl.getProtocol() + "://") < 0) location = myUrl.getProtocol() + "://" + myUrl.getHost() + location;
			httpConn.disconnect();
			return openConnection(location,urlEnc,"GET",followRedirects,connOrReadTimeout,++currentRedirectCount,requestHeaders,null);
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