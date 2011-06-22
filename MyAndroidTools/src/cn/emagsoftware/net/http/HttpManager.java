package cn.emagsoftware.net.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

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
	
	public static String concatParams(Map<String,String> params){
		Iterator<String> keys = params.keySet().iterator();
		StringBuffer paramsBuff = new StringBuffer();
		while(keys.hasNext()){
			String key = keys.next();
			String value = params.get(key);
			paramsBuff.append(key.concat("=").concat(value).concat("&"));
		}
		String paramsStr = paramsBuff.toString();
		if(!paramsStr.equals("")) paramsStr = paramsStr.substring(0, paramsStr.length()-1);
		return paramsStr;
	}
	
	public static String encodeParams(String concatedParams,String enc){
		try{
			concatedParams = URLEncoder.encode(concatedParams,enc);
			concatedParams = StringUtilities.replaceWordsAll(concatedParams, URLEncoder.encode("="), "=");
			concatedParams = StringUtilities.replaceWordsAll(concatedParams, URLEncoder.encode("&"), "&");
			return concatedParams;
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
	}
	
}
