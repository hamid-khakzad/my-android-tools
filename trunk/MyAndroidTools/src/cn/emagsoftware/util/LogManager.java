package cn.emagsoftware.util;

import android.util.Log;

/**
 * <p>��־�����࣬��Ҫ����־�������¼��㹦�ܲ��䣺<br>
 *    1.ǿ��ִ��TAG����ʹ�������Ĺ淶<br>
 *    2.������ֱ������������ܵ��µĿ�ָ���쳣<br>
 *    3.�������Ƿ���ʾ��־�Ŀ��أ���־�Ƿ���ʾ��ͳһ����
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
