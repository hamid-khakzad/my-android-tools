package cn.emagsoftware.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * <p>该类的功能实现类似于android.os.AsyncTask类，但AsyncTask类内部采用了线程池实现，线程资源不会被立即释放。该类以快速释放的实现，提供给用户一个第二选择
 * <p>该类支持在非UI-Thread中创建并启动
 * @author Wendell
 * @version 1.3
 */
public class UIThread extends Thread {
	
	protected Context context = null;
	protected Handler handler = new Handler(Looper.getMainLooper());
	protected Callback callback = new Callback();
	
	public UIThread(Context context,Callback callback){
		if(context == null) throw new NullPointerException();
		this.context = context;
		if(callback != null) this.callback = callback;
	}
	
	public final void run(){
		super.run();
		try{
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					callback.onBeginUI(context);
				}
			});
			final Object result = callback.onRunNoUI(context);
			handler.post(new Runnable(){
				@Override
				public void run() {
					callback.onSuccessUI(context,result);
				}
			});
		}catch(final Exception e){
			handler.post(new Runnable(){
				@Override
				public void run() {
					callback.onExceptionUI(context,e);
				}
			});
		}finally{
			handler.post(new Runnable(){
				@Override
				public void run() {
					callback.onFinallyUI(context);
				}
			});
		}
	}
	
	public static class Callback{
		
		public void onBeginUI(Context context){}
		
		public Object onRunNoUI(Context context) throws Exception{return null;}
		
		public void onSuccessUI(Context context,Object result){}
		
		public void onExceptionUI(Context context,Exception e){}
		
		public void onFinallyUI(Context context){}
		
	}
	
}
