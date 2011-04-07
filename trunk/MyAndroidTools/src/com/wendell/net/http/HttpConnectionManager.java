package com.wendell.net.http;

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
 * @version 1.0
 */
public final class HttpConnectionManager {
	
	public static final String HEADER_REQUEST_ACCEPT_LANGUAGE = "Accept-Language";
	public static final String HEADER_REQUEST_CONNECTION = "Connection";
	public static final String HEADER_REQUEST_CACHE_CONTROL = "Cache-Control";
	public static final String HEADER_REQUEST_ACCEPT_CHARSET = "Accept-Charset";
	public static final String HEADER_REQUEST_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_REQUEST_USER_AGENT = "User-Agent";
	public static final String HEADER_REQUEST_COOKIE = "Cookie";
	public static final String HEADER_RESPONSE_LOCATION = "Location";
	public static final String HEADER_RESPONSE_SET_COOKIE = "Set-Cookie";
	
	private HttpConnectionManager(){}
	
	/**
	 * 返回HttpURLConnection实例
	 * @param url 请求的url
	 * @param isSSL 是否是加密的https请求
	 * @param followRedirects 是否自动重定向
	 * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
	 * @param requestHeaders 请求头
	 * @return HttpURLConnection实例
	 * @throws IOException
	 */
	private static HttpURLConnection openConnection(String url,boolean isSSL,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders) throws IOException{
		HttpURLConnection httpConn = null;
		try{
			if(isSSL) {
				SSLContext sslCont = SSLContext.getInstance("TLS"); 
				sslCont.init(null, new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sslCont.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier(new URL(url).getHost()));
				httpConn = (HttpsURLConnection)new URL(url).openConnection();
			} else {
				httpConn = (HttpURLConnection)new URL(url).openConnection();
			}
			httpConn.setInstanceFollowRedirects(followRedirects);
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
			return httpConn;
		}catch(IOException e){
			if(httpConn != null) httpConn.disconnect();
			throw e;
		}catch(Exception e){
			if(httpConn != null) httpConn.disconnect();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 进行http get请求
	 * @param url 请求的url
	 * @param isSSL 是否是加密的https请求
	 * @param followRedirects 是否自动重定向
	 * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
	 * @param requestHeaders 请求头
	 * @return HttpResponseResult实例
	 * @throws IOException
	 */
	public static HttpResponseResult doGet(String url,boolean isSSL,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders) throws IOException{
		HttpURLConnection httpConn = null;
		InputStream input = null;
		try{
			httpConn = openConnection(url, isSSL, followRedirects, connOrReadTimeout, requestHeaders);
			httpConn.setRequestMethod("GET");
			HttpResponseResult result = new HttpResponseResult();
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
	 * 进行http post请求
	 * @param url 请求的url
	 * @param isSSL 是否是加密的https请求
	 * @param followRedirects 是否自动重定向
	 * @param connOrReadTimeout 连接和读取的超时时间，以毫秒为单位，设为0表示永不超时
	 * @param requestHeaders 请求头
	 * @param params 请求时带入的参数
	 * @param paramsCharset 带入参数的字符集
	 * @return HttpResponseResult实例
	 * @throws IOException
	 */
	public static HttpResponseResult doPost(String url,boolean isSSL,boolean followRedirects,int connOrReadTimeout,Map<String,List<String>> requestHeaders,Map<String,String> params,String paramsCharset) throws IOException{
		HttpURLConnection httpConn = null;
		OutputStream output = null;
		InputStream input = null;
		try{
			httpConn = openConnection(url, isSSL, followRedirects, connOrReadTimeout, requestHeaders);
			httpConn.setRequestMethod("POST");
			if(params != null){
				Iterator<String> keys = params.keySet().iterator();
				StringBuffer paramsBuff = new StringBuffer();
				while(keys.hasNext()){
					String key = keys.next();
					String value = params.get(key);
					paramsBuff.append(key.concat("=").concat(value).concat("&"));
				}
				String paramsStr = paramsBuff.toString();
				if(!paramsStr.equals("")) paramsStr = paramsStr.substring(0, paramsStr.length()-1);
				output = httpConn.getOutputStream();
				BufferedOutputStream buffOutput = new BufferedOutputStream(output);
				buffOutput.write(paramsStr.getBytes(paramsCharset));
				buffOutput.flush();
			}
			HttpResponseResult result = new HttpResponseResult();
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
				try{
					if(output != null) output.close();
				}finally{
					if(httpConn != null) httpConn.disconnect();
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
