package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.ui.UIThread;

import android.content.Context;
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
	/**当前的加载是否发生了异常*/
	protected boolean mIsException = false;
	/**当前的加载条件*/
	protected Object mCurCondition = null;
	
	public BaseLazyLoadingAdapter(Context context,int limit){
		super(context);
		if(limit <= 0) throw new IllegalArgumentException("limit should be great than zero.");
		mLimit = limit;
	}
	
	/**
	 * <p>绑定AdapterView，使其自动懒加载
	 * <p>目前只支持AbsListView，当AbsListView滑动到最后面时将自动开始新的加载
	 *    当调用AbsListView的setAdapter、addFooterView等方法时也会自动触发加载，若要避免此类情形的自动加载，可将这些方法的调用放在当前方法之前
	 * @param adapterView
	 */
	public void bindLazyLoading(AdapterView<?> adapterView){
		if(adapterView instanceof AbsListView){
			AbsListView absList = (AbsListView)adapterView;
			absList.setOnScrollListener(new AbsListView.OnScrollListener(){
				@Override
				public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
					// TODO Auto-generated method stub
					if(firstVisibleItem + visibleItemCount == totalItemCount && !isLoadedAll() && !mIsException){
						load(mCurCondition);
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
	public boolean load(final Object condition) {
		// TODO Auto-generated method stub
		if(mIsLoading) return false;
		mIsLoading = true;
		mCurCondition = condition;
		ThreadPoolManager.executeThread(new UIThread(mContext,new UIThread.Callback(){
			@Override
			public void onBeginUI(Context context) {
				// TODO Auto-generated method stub
				super.onBeginUI(context);
				onBeginLoad(context,condition);
			}
			@Override
			public Object onRunNoUI(Context context) throws Exception {
				// TODO Auto-generated method stub
				super.onRunNoUI(context);
				return onLoad(context,condition,mStart,mLimit);
			}
			@Override
			public void onSuccessUI(Context context,Object result) {
				// TODO Auto-generated method stub
				super.onSuccessUI(context,result);
				if(result == null){
					mIsLoading = false;
					mIsLoaded = true;
					mIsLoadedAll = true;
					mIsException = false;
					onAfterLoad(context,condition,null);
				}else{
					List<DataHolder> resultList = (List<DataHolder>)result;
					addDataHolders(resultList);    //该方法需在UI线程中执行且是非线程安全的
					mIsLoading = false;
					mIsLoaded = true;
					int size = resultList.size();
					mStart = mStart + size;
					if(size == 0) mIsLoadedAll = true;
					else mIsLoadedAll = false;
					mIsException = false;
					onAfterLoad(context,condition,null);
				}
			}
			@Override
			public void onExceptionUI(Context context,Exception e) {
				// TODO Auto-generated method stub
				super.onExceptionUI(context,e);
				Log.e("BaseLazyLoadingAdapter","Execute lazy loading failed.",e);
				mIsLoading = false;
				mIsException = true;
				onAfterLoad(context,condition,e);
			}
		}));
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
	 * <p>对于当前类而言，已使用onLoad(Context context,Object condition,int start,int limit)替换了当前方法的作用，故将其简单实现以防止子类被强制要求实现
	 */
	@Override
	public List<DataHolder> onLoad(Context context,Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * <p>加载的具体实现，通过传入的参数可以实现分段的懒加载。该方法由非UI线程回调，所以可以执行耗时操作
	 * @param context
	 * @param condition
	 * @param start 本次加载的开始序号
	 * @param limit 本次加载的长度
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(Context context,Object condition,int start,int limit) throws Exception;
	
}
