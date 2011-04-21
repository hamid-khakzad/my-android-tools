package com.wendell.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class UIThread extends Thread {
	
	protected Context context = null;
	protected Handler handler = new Handler(Looper.getMainLooper());
	protected Callback callback = new Callback();
	
	public UIThread(Context context,Callback callback){
		if(context == null) throw new NullPointerException();
		this.context = context;
		if(callback != null) this.callback = callback;
	}
	
	public final void start(){
		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				callback.onBeginUI(context);
				execSuperStart();
			}
		});
	}
	
	protected void execSuperStart(){
		super.start();
	}
	
	public final void run(){
		super.run();
		try{
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
