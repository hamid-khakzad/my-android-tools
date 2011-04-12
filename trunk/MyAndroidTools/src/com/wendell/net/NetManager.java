package com.wendell.net;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.wendell.net.http.HttpConnectionManager;
import com.wendell.net.http.HttpResponseResult;

public final class NetManager {
	
	private static final String USEFUL_TEST_URL = "http://www.baidu.com";
	private static final String USEFUL_TEST_HOST = "www.baidu.com";
	
	private NetManager(){}
	
	public boolean isNetUseful(){
		try{
			HttpResponseResult result = HttpConnectionManager.doGet(USEFUL_TEST_URL, false, true, 15000, null);
			if(result.getResponseCode() != HttpURLConnection.HTTP_OK) return false;
			String host = result.getResponseURL().getHost();
			String content = result.getDataString("gb2312");
			if(USEFUL_TEST_HOST.equalsIgnoreCase(host) && content.indexOf(USEFUL_TEST_HOST) >= 0) {    //若能访问到原始站点，证明网络有效
				return true;
			}
			return false;
		}catch(IOException e){
			return false;
		}
	}
	
}
