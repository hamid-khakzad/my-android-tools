package cn.emagsoftware.util.jlog;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * <p>java自带日志的操作类
 * @author Wendell
 * @version 1.4
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
			input = JLog.class.getResourceAsStream("/cn/emagsoftware/util/jlog/templat/jlog.properties");
			loadConfig(input);
		}finally{
			if(input != null) input.close();
		}
	}
	
	private static Logger getLogger(){
		return Logger.getLogger("cn.emagsoftware.util.jlog.JLog");
	}
	
	public static void severe(String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.SEVERE, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void severe(String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.SEVERE, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void severe(String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.SEVERE, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void warning(String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.WARNING, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void warning(String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.WARNING, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void warning(String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.WARNING, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void info(String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.INFO, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void info(String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.INFO, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void info(String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.INFO, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void config(String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.CONFIG, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void config(String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.CONFIG, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void config(String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.CONFIG, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void fine(String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.FINE, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void fine(String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.FINE, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void fine(String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.FINE, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finer(String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.FINER, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finer(String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.FINER, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finer(String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.FINER, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finest(String msg){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.FINEST, stack.getClassName(), stack.getMethodName(), msg);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finest(String msg,Object[] params){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.FINEST, stack.getClassName(), stack.getMethodName(), msg, params);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
	public static void finest(String msg,Throwable thrown){
		try{
			Thread current = Thread.currentThread();
			StackTraceElement stack = current.getStackTrace()[3];
			current.setContextClassLoader(JLog.class.getClassLoader());
			getLogger().logp(Level.FINEST, stack.getClassName(), stack.getMethodName(), msg, thrown);
		}catch(RuntimeException e){
			System.err.println("Write log failed");
			e.printStackTrace(System.err);
		}
	}
	
}
