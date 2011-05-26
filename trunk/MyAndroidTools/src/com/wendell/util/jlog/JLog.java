package com.wendell.util.jlog;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * <p>java自带日志的操作类
 * @author Wendell
 * @version 1.3
 */
public final class JLog {
	
	static{
		try{
			loadNativeConfig();
		}catch(Exception e){
			System.err.println("Loading native configuration failed");
			e.printStackTrace(System.err);
		}
	}
	
	private JLog(){};
	
	/**
	 * <p>该方法在原则上应该最多只被调用一次
	 * @param configInput
	 * @throws IOException
	 */
	public static void loadConfig(InputStream configInput) throws IOException {
		LogManager manager = LogManager.getLogManager();
		manager.readConfiguration(configInput);
	}
	
	/**
	 * <p>该方法在原则上应该最多只被调用一次
	 * @throws IOException
	 */
	private static void loadNativeConfig() throws IOException {
		InputStream input = null;
		try{
			input = JLog.class.getResourceAsStream("/com/wendell/util/jlog/templat/jlog.properties");
			loadConfig(input);
		}finally{
			if(input != null) input.close();
		}
	}
	
	private static Logger getLogger(String name){
		return Logger.getLogger(name);
	}
	
	public static void severe(String name,String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.SEVERE, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void severe(String name,String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.SEVERE, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void severe(String name,String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.SEVERE, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void warning(String name,String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.WARNING, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void warning(String name,String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.WARNING, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void warning(String name,String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.WARNING, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void info(String name,String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.INFO, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void info(String name,String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.INFO, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void info(String name,String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.INFO, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void config(String name,String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.CONFIG, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void config(String name,String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.CONFIG, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void config(String name,String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.CONFIG, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void fine(String name,String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.FINE, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void fine(String name,String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.FINE, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void fine(String name,String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.FINE, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finer(String name,String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.FINER, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finer(String name,String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.FINER, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finer(String name,String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.FINER, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finest(String name,String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.FINEST, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finest(String name,String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.FINEST, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finest(String name,String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger(name).logp(Level.FINEST, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
}
