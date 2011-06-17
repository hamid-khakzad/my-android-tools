package cn.emagsoftware.net.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import cn.emagsoftware.util.StringUtilities;

public final class HttpManager {
	
	private HttpManager(){}
	
	public static String encodeURL(String url,String enc){
		try{
			url = URLEncoder.encode(url,enc);
			url = StringUtilities.replaceWordsAll(url, URLEncoder.encode(":"), ":");
			url = StringUtilities.replaceWordsAll(url, URLEncoder.encode("/"), "/");
			url = StringUtilities.replaceWordsAll(url, URLEncoder.encode("?"), "?");
			url = StringUtilities.replaceWordsAll(url, URLEncoder.encode("="), "=");
			url = StringUtilities.replaceWordsAll(url, URLEncoder.encode("&"), "&");
			return url;
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
	}
	
	public static String encodeParams(String params,String enc){
		try{
			params = URLEncoder.encode(params,enc);
			params = StringUtilities.replaceWordsAll(params, URLEncoder.encode("="), "=");
			params = StringUtilities.replaceWordsAll(params, URLEncoder.encode("&"), "&");
			return params;
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
	}
	
}
