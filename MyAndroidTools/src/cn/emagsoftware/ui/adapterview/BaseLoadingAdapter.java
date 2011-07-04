package cn.emagsoftware.ui.adapterview;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseLoadingAdapter extends GenericAdapter{
	
	/**执行异步任务的类*/
	protected AsyncTask<Object, Integer, Object> mTask = null;
	/**是否正在加载*/
	protected boolean mIsLoading = false;
	
	public BaseLoadingAdapter(final Context context){
		super(context);
		mTask = new AsyncTask<Object, Integer, Object>(){
			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				onBeginLoad(context);
			}
			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				try{
					return onLoad(context);
				}catch(Exception e){
					Log.e("BaseLoadingAdapter", "Execute loading failed.", e);
					return e;
				}
			}
			@Override
			protected void onPostExecute(Object result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				mIsLoading = false;
				if(result == null){
					onAfterLoad(context,null);
				}else if(result instanceof List<?>){
					onAfterLoad(context,null);
				}else if(result instanceof Exception){
					onAfterLoad(context,(Exception)result);
				}
			}
		};
	}
	
	/**
	 * <p>加载的执行方法
	 * @return true表示开始加载；false表示已经在加载，本次的调用无效
	 */
	public boolean load(){
		if(mIsLoading) return false;
		mIsLoading = true;
		mTask.execute("");
		return true;
	}
	
	/**
	 * <p>是否正在加载
	 * @return
	 */
	public boolean isLoading(){
		return mIsLoading;
	}
	
	/**
	 * <p>在加载之前的回调方法，可以显示一些loading之类的字样。如对于ListView，可以通过addFooterView方法添加一个正在加载的提示
	 * @param context
	 */
	public abstract void onBeginLoad(Context context);
	
	/**
	 * <p>加载的具体实现，该方法将在非UI线程中执行，要注意不能执行UI的操作
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(Context context) throws Exception;
	
	/**
	 * <p>加载完成后的回调方法，可以通过判断exception是否为null来获悉加载成功与否，从而给用户一些提示
	 * @param context
	 * @param exception
	 */
	public abstract void onAfterLoad(Context context,Exception exception);
	
}
