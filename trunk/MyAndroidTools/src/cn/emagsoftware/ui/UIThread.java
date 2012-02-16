package cn.emagsoftware.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * <p>����Ĺ���ʵ��������android.os.AsyncTask�࣬��AsyncTask���ڲ��������̳߳�ʵ�֣��߳���Դ���ᱻ�����ͷš������Կ����ͷŵ�ʵ�֣��ṩ���û�һ���ڶ�ѡ��
 * <p>����֧���ڷ�UI-Thread�д���������
 * @author Wendell
 * @version 2.5
 */
public class UIThread extends Thread {
	
	protected Context context = null;
	protected boolean isCancelled = false;
	protected Handler handler = new Handler(Looper.getMainLooper());
	
	public UIThread(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		if(isCancelled) return;
		handler.post(new Runnable() {    //��start������onBeginUI���������ڵ�ǰ�߳�������ʼ�����ͣ����Ա�֤UI�̵߳��ν�һ�£�������ֲ�ͬ�������
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(isCancelled) return;
				onBeginUI(context);
				execSuperStart();    //������onBeginUIִ����ɺ��������߳�
			}
		});
	}
	
	private void execSuperStart(){
		super.start();
	}
	
	public final void run(){
		super.run();
		try{
			if(isCancelled) return;
			final Object result = onRunNoUI(context);
			handler.post(new Runnable(){
				@Override
				public void run() {
					if(isCancelled) return;
					onSuccessUI(context,result);
				}
			});
		}catch(final Exception e){
			handler.post(new Runnable(){
				@Override
				public void run() {
					if(isCancelled) return;
					onExceptionUI(context,e);
				}
			});
		}
	}
	
	public void cancel(){
		isCancelled = true;
	}
	
	public void postProgress(final Object progress){
		handler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				onProgressUI(context,progress);
			}
		});
	}
	
	protected void onBeginUI(Context context){}
	
	protected Object onRunNoUI(Context context) throws Exception{return null;}
	
	protected void onProgressUI(Context context,Object progress){}
	
	protected void onSuccessUI(Context context,Object result){}
	
	protected void onExceptionUI(Context context,Exception e){}
	
}
