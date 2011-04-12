package com.wendell.ui;

import android.content.Context;
import android.os.Handler;

public class UIThread extends Thread {
	
	protected Context context = null;
	protected Handler handler = null;
	protected Callback callback = new Callback();
	
	public UIThread(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
		this.handler = new Handler();
	}
	
	public void setCallback(Callback callback){
		if(callback == null) throw new NullPointerException();
		this.callback = callback;
	}
	
	public final void start(){
		callback.onBeginUI(context);
		super.start();
	}
	
	public final void run(){
		super.run();
		final Callback currentCallback = callback;
		try{
			final Object result = currentCallback.onRunNoUI(context);
			handler.post(new Runnable(){
				@Override
				public void run() {
					currentCallback.onSuccessUI(context,result);
				}
			});
		}catch(final Exception e){
			handler.post(new Runnable(){
				@Override
				public void run() {
					currentCallback.onExceptionUI(context,e);
				}
			});
		}finally{
			handler.post(new Runnable(){
				@Override
				public void run() {
					currentCallback.onFinallyUI(context);
				}
			});
		}
	}
	
	public static class Callback{
		
		public void onBeginUI(Context context){
			
		}
		
		public Object onRunNoUI(Context context) throws Exception{
			return null;
		}
		
		public void onSuccessUI(Context context,Object result){
			
		}
		
		public void onExceptionUI(Context context,Exception e){
			
		}
		
		public void onFinallyUI(Context context){
			
		}
		
	}
	
}
