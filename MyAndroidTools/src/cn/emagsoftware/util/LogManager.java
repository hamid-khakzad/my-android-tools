package cn.emagsoftware.util;

import android.util.Log;

/**
 * <p>日志管理类，主要对日志做了以下几点功能补充：<br>
 *    1.强制执行TAG参数使用类名的规范<br>
 *    2.避免了直接输出变量可能导致的空指针异常<br>
 *    3.增加了是否显示日志的开关，日志是否显示可统一控制
 * @author Wendell
 *
 */
public final class LogManager {
	
	public static boolean ENABLE_LOGGING = true;
	
	private LogManager(){}
	
	public static void logD(Class<? extends Object> tag,String msg){
		if(ENABLE_LOGGING){
			if(msg == null) msg = "";
			Log.d(tag.getSimpleName(), msg);
		}
	}
	
	public static void logD(Class<? extends Object> tag,String msg,Throwable tr){
		if(ENABLE_LOGGING){
			if(msg == null) msg = "";
			if(tr == null) Log.d(tag.getSimpleName(), msg);
			else Log.d(tag.getSimpleName(), msg, tr);
		}
	}
	
	public static void logI(Class<? extends Object> tag,String msg){
		if(ENABLE_LOGGING){
			if(msg == null) msg = "";
			Log.i(tag.getSimpleName(), msg);
		}
	}
	
	public static void logI(Class<? extends Object> tag,String msg,Throwable tr){
		if(ENABLE_LOGGING){
			if(msg == null) msg = "";
			if(tr == null) Log.i(tag.getSimpleName(), msg);
			else Log.i(tag.getSimpleName(), msg, tr);
		}
	}
	
	public static void logW(Class<? extends Object> tag,String msg){
		if(ENABLE_LOGGING){
			if(msg == null) msg = "";
			Log.w(tag.getSimpleName(), msg);
		}
	}
	
	public static void logW(Class<? extends Object> tag,String msg,Throwable tr){
		if(ENABLE_LOGGING){
			if(msg == null) msg = "";
			if(tr == null) Log.w(tag.getSimpleName(), msg);
			else Log.w(tag.getSimpleName(), msg, tr);
		}
	}
	
	public static void logE(Class<? extends Object> tag,String msg){
		if(ENABLE_LOGGING){
			if(msg == null) msg = "";
			Log.e(tag.getSimpleName(), msg);
		}
	}
	
	public static void logE(Class<? extends Object> tag,String msg,Throwable tr){
		if(ENABLE_LOGGING){
			if(msg == null) msg = "";
			if(tr == null) Log.e(tag.getSimpleName(), msg);
			else Log.e(tag.getSimpleName(), msg, tr);
		}
	}
	
}
