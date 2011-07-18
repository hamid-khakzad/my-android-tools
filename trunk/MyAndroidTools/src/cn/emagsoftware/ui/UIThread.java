package cn.emagsoftware.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * <p>����Ĺ���ʵ��������android.os.AsyncTask�࣬��AsyncTask���ڲ��������̳߳�ʵ�֣��߳���Դ���ᱻ�����ͷš������Կ����ͷŵ�ʵ�֣��ṩ���û�һ���ڶ�ѡ��
 * <p>����֧���ڷ�UI-Thread�д���������
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
