package cn.emagsoftware.ui.adapterview;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseLazyLoadingAdapter extends BaseLoadingAdapter {
	
	/**当前加载到的序号*/
	protected int mStart = 0;
	/**每次加载的长度*/
	protected int mLimit = 10;
	
	public BaseLazyLoadingAdapter(final Context context,int limit){
		super(context);
		if(limit <= 0) throw new IllegalArgumentException("limit should be great than zero.");
		mLimit = limit;
		super.mTask = new AsyncTask<Object, Integer, Object>(){
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
					return onLoad(mStart,mLimit);
				}catch(Exception e){
					Log.e("BaseLazyLoadingAdapter", "Execute lazy loading failed.", e);
					return e;
				}
			}
			@Override
			protected void onPostExecute(Object result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				if(result == null){
					mIsLoading = false;
					onAfterLoad(context,null);
				}else if(result instanceof List<?>){
					List<DataHolder> resultList = (List<DataHolder>)result;
					addDataHolders(resultList);
					mStart = mStart + resultList.size();
					mIsLoading = false;
					onAfterLoad(context,null);
				}else if(result instanceof Exception){
					mIsLoading = false;
					onAfterLoad(context,(Exception)result);
				}
			}
		};
	}
	
	/**
	 * <p>覆盖了父类的同名方法，以重置当前类的一些属性
	 */
	@Override
	public void clearDataHolders() {
		// TODO Auto-generated method stub
		super.clearDataHolders();
		mStart = 0;
	}
	
	/**
	 * <p>对于当前类而言，已使用onLoad(int start,int limit)替换了当前方法的作用，故将其简单实现以防止子类被强制要求实现
	 */
	@Override
	public List<DataHolder> onLoad() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * <p>加载的具体实现，通过传入的参数可以实现分段的懒加载。该方法由非UI线程回调，所以可以执行耗时操作
	 * @param start 本次加载的开始序号
	 * @param limit 本次加载的长度
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(int start,int limit) throws Exception;
	
}
