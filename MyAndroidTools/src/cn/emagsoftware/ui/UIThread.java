package cn.emagsoftware.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * <p>该类的功能实现类似于AsyncTask类
 * <p>该类支持在非UI-Thread中创建并启动
 * @author Wendell
 * @version 2.0
 * @deprecated 该类已舍弃，可使用AsyncTask代替，涉及到更新UI的，可使用AsyncUITask代替
 */
public class UIThread extends Thread {
	
	protected Context context = null;
	protected boolean isCancelled = false;
	protected Handler handler = new Handler(Looper.getMainLooper());
	
	public UIThread(Context context){
		if(context == null) throw new NullPointerException();
		this.context = context;
	}
	
	public final void run(){
		super.run();
		try{
			if(isCancelled) return;
			final boolean[] isOK = new boolean[1];
			isOK[0] = false;
			handler.post(new Runnable() {    //onBeginUI只能在run中回调，start在线程池中不被调用，所以不能通过重写start来回调
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(isCancelled){
						isOK[0] = true;
						return;
					}
					onBeginUI(context);
					isOK[0] = true;
				}
			});
			while(!isOK[0]){
				sleep(100);
			}
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
	
	/**
	 * <p>onBeginUI是在当前线程真正运行之后才会调用，会使UI线程产生空隙
	 *    若要使UI线程衔接一致，避免出现外部的不同步，可在启动当前线程之前执行相关UI操作
	 * @param context
	 */
	protected void onBeginUI(Context context){}
	
	protected Object onRunNoUI(Context context) throws Exception{return null;}
	
	protected void onProgressUI(Context context,Object progress){}
	
	protected void onSuccessUI(Context context,Object result){}
	
	protected void onExceptionUI(Context context,Exception e){}
	
}
