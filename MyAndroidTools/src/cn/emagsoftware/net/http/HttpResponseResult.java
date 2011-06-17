package cn.emagsoftware.net.http;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpResponseResult {
	
	protected URL responseURL = null;
	protected int responseCode = -1;
	protected Map<String,List<String>> responseHeaders = null;
	protected byte[] data = null;
	
	public URL getResponseURL(){
		return responseURL;
	}
	
	public void setResponseURL(URL responseURL){
		this.responseURL = responseURL;
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public Map<String, List<String>> getResponseHeaders() {
		return responseHeaders;
	}
	public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String getDataString(String charset){
		if(data == null) return null;
		try{
			return new String(data,charset);
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
	}
	
}
