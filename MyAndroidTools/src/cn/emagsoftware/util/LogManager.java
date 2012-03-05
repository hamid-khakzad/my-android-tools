package cn.emagsoftware.util;

import android.util.Log;

/**
 * <p>日志管理类，主要对日志做了以下几点功能补充：<br>
 *    1.强制执行TAG参数使用类名的规范<br>
 *    2.避免了直接输出变量可能导致的空指针异常<br>
 *    3.增加了显示开关，可根据等级控制日志显示与否
 * @author Wendell
 *
 */
public final class LogManager {
	
	public static int LOGGING_LEVEL = Log.VERBOSE;
	
	private LogManager(){}
	
	public static void logV(Class<? extends Object> tag,String msg){
		if(Log.VERBOSE >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			Log.v(tag.getSimpleName(), msg);
		}
	}
	
	public static void logV(Class<? extends Object> tag,String msg,Throwable tr){
		if(Log.VERBOSE >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			if(tr == null) Log.v(tag.getSimpleName(), msg);
			else Log.v(tag.getSimpleName(), msg, tr);
		}
	}
	
	public static void logD(Class<? extends Object> tag,String msg){
		if(Log.DEBUG >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			Log.d(tag.getSimpleName(), msg);
		}
	}
	
	public static void logD(Class<? extends Object> tag,String msg,Throwable tr){
		if(Log.DEBUG >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			if(tr == null) Log.d(tag.getSimpleName(), msg);
			else Log.d(tag.getSimpleName(), msg, tr);
		}
	}
	
	public static void logI(Class<? extends Object> tag,String msg){
		if(Log.INFO >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			Log.i(tag.getSimpleName(), msg);
		}
	}
	
	public static void logI(Class<? extends Object> tag,String msg,Throwable tr){
		if(Log.INFO >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			if(tr == null) Log.i(tag.getSimpleName(), msg);
			else Log.i(tag.getSimpleName(), msg, tr);
		}
	}
	
	public static void logW(Class<? extends Object> tag,String msg){
		if(Log.WARN >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			Log.w(tag.getSimpleName(), msg);
		}
	}
	
	public static void logW(Class<? extends Object> tag,Throwable tr){
		if(Log.WARN >= LOGGING_LEVEL){
			if(tr == null) Log.w(tag.getSimpleName(), "");
			else Log.w(tag.getSimpleName(), tr);
		}
	}
	
	public static void logW(Class<? extends Object> tag,String msg,Throwable tr){
		if(Log.WARN >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			if(tr == null) Log.w(tag.getSimpleName(), msg);
			else Log.w(tag.getSimpleName(), msg, tr);
		}
	}
	
	public static void logE(Class<? extends Object> tag,String msg){
		if(Log.ERROR >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			Log.e(tag.getSimpleName(), msg);
		}
	}
	
	public static void logE(Class<? extends Object> tag,String msg,Throwable tr){
		if(Log.ERROR >= LOGGING_LEVEL){
			if(msg == null) msg = "";
			if(tr == null) Log.e(tag.getSimpleName(), msg);
			else Log.e(tag.getSimpleName(), msg, tr);
		}
	}
	
}
