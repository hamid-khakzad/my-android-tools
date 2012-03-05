package cn.emagsoftware.util;

import android.util.Log;

/**
 * <p>��־�����࣬��Ҫ����־�������¼��㹦�ܲ��䣺<br>
 *    1.ǿ��ִ��TAG����ʹ�������Ĺ淶<br>
 *    2.������ֱ������������ܵ��µĿ�ָ���쳣<br>
 *    3.������������أ��ɸ��ݵȼ�������־������
 * @author Wendell
 *
 */
public final class LogManager {
	
	/**
	 * <p>ͨ�����ô˱�����������־�������ֻ�еȼ����ڵ������õĵȼ�ʱ��־�Żᱻ��������ô˱���ΪLog.ASSERT�ɽ�ֹ���еȼ�����־���
	 */
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
