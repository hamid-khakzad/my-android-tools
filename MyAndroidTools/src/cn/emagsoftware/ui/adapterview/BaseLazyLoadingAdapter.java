package cn.emagsoftware.ui.adapterview;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AdapterView;

public abstract class BaseLazyLoadingAdapter extends BaseLoadingAdapter {
	
	/**当前加载到的序号*/
	protected int mStart = 0;
	/**每次加载的长度*/
	protected int mLimit = 10;
	/**是否已经加载了全部数据*/
	protected boolean mIsLoadedAll = false;
	
	public BaseLazyLoadingAdapter(Context context,int limit){
		super(context);
		if(limit <= 0) throw new IllegalArgumentException("limit should be great than zero.");
		mLimit = limit;
	}
	
	/**
	 * <p>绑定AdapterView，使其自动懒加载。如滑动ListView到最下面时才开始新的加载
	 * @param adapterView
	 */
	public void bindLazyLoading(AdapterView<?> adapterView){
		if(adapterView instanceof AbsListView){
			AbsListView absList = (AbsListView)adapterView;
			absList.setOnScrollListener(new AbsListView.OnScrollListener(){
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub
					if(firstVisibleItem + visibleItemCount == totalItemCount && !isLoadedAll()){
						load();
					}
				}
				@Override
				public void onScrollStateChanged(AbsListView view,int scrollState) {
					// TODO Auto-generated method stub
				}
			});
		}else{
			throw new UnsupportedOperationException("Only supports lazy loading for the AdapterView which is AbsListView.");
		}
	}
	
	/**
	 * <p>覆盖了父类的同名方法，用来执行懒加载
	 */
	@Override
	public boolean load() {
		// TODO Auto-generated method stub
		if(mIsLoading) return false;
		mIsLoading = true;
		mTask = new AsyncTask<Object, Integer, Object>(){
			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				onBeginLoad(mContext);
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
					mIsLoadedAll = true;
					mIsLoading = false;
					onAfterLoad(mContext,null);
				}else if(result instanceof List<?>){
					List<DataHolder> resultList = (List<DataHolder>)result;
					addDataHolders(resultList);    //该方法需在UI线程中执行且是非线程安全的
					int size = resultList.size();
					mStart = mStart + size;
					if(size == 0) mIsLoadedAll = true;
					else mIsLoadedAll = false;
					mIsLoading = false;
					onAfterLoad(mContext,null);
				}else if(result instanceof Exception){
					mIsLoading = false;
					onAfterLoad(mContext,(Exception)result);
				}
			}
		};
		mTask.execute("");
		return true;
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
	 * <p>是否已经加载了全部数据
	 * @return
	 */
	public boolean isLoadedAll(){
		return mIsLoadedAll;
	}
	
	/**
	 * <p>对于当前类而言，已使用onLoad(int start,int limit)替换了onLoad()的作用，故将其简单实现以防止子类被强制要求实现
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
