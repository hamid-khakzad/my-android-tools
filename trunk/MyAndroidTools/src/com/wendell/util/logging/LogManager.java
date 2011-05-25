package com.wendell.util.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * <p>��־��������
 * @author Wendell
 * @version 1.0
 */
public final class LogManager {
	
	static{
		try{
			loadNativeConfig();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	private LogManager(){};
	
	/**
	 * <p>�÷�����ԭ����Ӧ�����ֻ������һ��
	 * @param configInput
	 * @throws IOException
	 */
	public static void loadConfig(InputStream configInput) throws IOException {
		java.util.logging.LogManager logManager = java.util.logging.LogManager.getLogManager();
		logManager.readConfiguration(configInput);
	}
	
	/**
	 * <p>�÷�����ԭ����Ӧ�����ֻ������һ��
	 * @throws IOException
	 */
	private static void loadNativeConfig() throws IOException {
		InputStream input = null;
		try{
			input = LogManager.class.getResourceAsStream("/com/wendell/util/logging/templat/logging.properties");
			loadConfig(input);
		}finally{
			if(input != null) input.close();
		}
	}
	
	public static Logger getLogger(String name){
		return Logger.getLogger(name);
	}
	
	public static Logger getLogger(String name,String resourceBundleName){
		return Logger.getLogger(name,resourceBundleName);
	}
	
	public static Logger getLogger(Class<Object> clazz){
		return getLogger(clazz.getName());
	}
	
}
