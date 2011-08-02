package cn.emagsoftware.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * <p>该类的功能实现类似于android.os.AsyncTask类，但AsyncTask类内部采用了线程池实现，线程资源不会被立即释放。该类以快速释放的实现，提供给用户一个第二选择
 * <p>该类支持在非UI-Thread中创建并启动
 * @author Wendell
 * @version 1.6
 */
public class UIThread extends Thread {
	
	protected Context context = null;
	protected Handler handler = new Handler(Looper.getMainLooper());
	
	public UIThread(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
	}
	
	public final void run(){
		super.run();
		try{
			final boolean[] isOK = new boolean[1];
			isOK[0] = false;
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					onBeginUI(context);
					isOK[0] = true;
				}
			});
			while(!isOK[0]){
				sleep(100);
			}
			final Object result = onRunNoUI(context);
			handler.post(new Runnable(){
				@Override
				public void run() {
					onSuccessUI(context,result);
				}
			});
		}catch(final Exception e){
			handler.post(new Runnable(){
				@Override
				public void run() {
					onExceptionUI(context,e);
				}
			});
		}finally{
			handler.post(new Runnable(){
				@Override
				public void run() {
					onFinallyUI(context);
				}
			});
		}
	}
	
	public void postProgress(final Object progress){
		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				onProgressUI(progress);
			}
		});
	}
	
	protected void onBeginUI(Context context){}
	
	protected Object onRunNoUI(Context context) throws Exception{return null;}
	
	protected void onProgressUI(Object progress){}
	
	protected void onSuccessUI(Context context,Object result){}
	
	protected void onExceptionUI(Context context,Exception e){}
	
	protected void onFinallyUI(Context context){}
	
}
