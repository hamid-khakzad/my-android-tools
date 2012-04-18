package cn.emagsoftware.ui.adapterview;

import java.lang.reflect.Field;
import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.AdapterView;

/**
 * @deprecated 该类已舍弃，请使用BaseStepLoadingAdapter代替
 * @author Wendell
 */
public abstract class BaseLazyLoadingAdapter extends BaseLoadingAdapter {
	
	/**每次加载的长度*/
	private int mLimit = 10;
	/**是否已经加载了全部数据*/
	private boolean mIsLoadedAll = false;
	
	public BaseLazyLoadingAdapter(Context context,int limit){
		super(context);
		if(limit <= 0) throw new IllegalArgumentException("limit should be great than zero.");
		mLimit = limit;
	}
	
	/**
	 * <p>绑定AdapterView，使其自动懒加载
	 * <p>目前只支持AbsListView，当AbsListView滑动到最后面时将自动开始新的加载
	 * <p>AbsListView的bindLazyLoading实现实际上执行了OnScrollListener事件；
	 *    用户若包含自己的OnScrollListener逻辑，请在bindLazyLoading之前调用setOnScrollListener，bindLazyLoading方法会将用户的逻辑包含进来；
	 *    若在bindLazyLoading之后调用setOnScrollListener，将取消bindLazyLoading的作用
	 * @param adapterView
	 * @param remainingCount 当剩余多少个时开始继续加载，最小值为0，表示直到最后才开始继续加载
	 */
	public void bindLazyLoading(AdapterView<?> adapterView,int remainingCount){
		if(adapterView instanceof AbsListView){
			try{
				AbsListView absList = (AbsListView)adapterView;
				Field field = AbsListView.class.getDeclaredField("mOnScrollListener");
				field.setAccessible(true);
				AbsListView.OnScrollListener onScrollListener = (AbsListView.OnScrollListener)field.get(absList);
				if(onScrollListener != null && onScrollListener instanceof LazyLoadingListener){
					absList.setOnScrollListener(new LazyLoadingListener(((LazyLoadingListener)onScrollListener).getOriginalListener(), remainingCount));
				}else{
					absList.setOnScrollListener(new LazyLoadingListener(onScrollListener, remainingCount));
				}
			}catch(NoSuchFieldException e){
				throw new RuntimeException(e);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}
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
		onBeginLoad(mContext,condition);
		final int start = getRealCount();
		new AsyncWeakTask<Object, Integer, Object>(mContext) {
			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				try{
					return onLoad(condition,start,mLimit);
				}catch(Exception e){
					return e;
				}
			}
			@SuppressWarnings("unchecked")
			@Override
			protected void onPostExecute(Object[] objs, Object result) {
				// TODO Auto-generated method stub
				if(result instanceof Exception){
					Exception e = (Exception)result;
					LogManager.logE(BaseLazyLoadingAdapter.class, "Execute lazy loading failed.", e);
					mIsLoading = false;
					mIsException = true;
					onAfterLoad((Context)objs[0],condition,e);
				}else{
					if(result == null){
						mIsLoading = false;
						mIsLoaded = true;
						mIsLoadedAll = true;
						mIsException = false;
						onAfterLoad((Context)objs[0],condition,null);
					}else{
						List<DataHolder> resultList = (List<DataHolder>)result;
						addDataHolders(resultList);    //该方法需在UI线程中执行且是非线程安全的
						mIsLoading = false;
						mIsLoaded = true;
						if(resultList.size() == 0) mIsLoadedAll = true;
						else mIsLoadedAll = false;
						mIsException = false;
						onAfterLoad((Context)objs[0],condition,null);
					}
				}
			}
		}.execute("");
		return true;
	}
	
	/**
	 * <p>是否已全部加载
	 * @return
	 */
	public boolean isLoadedAll(){
		return mIsLoadedAll;
	}
	
	/**
	 * <p>对于当前类而言，已使用onLoad(Object condition,int start,int limit)替换了当前方法的作用，故将其简单实现以防止子类被强制要求实现
	 */
	@Override
	public List<DataHolder> onLoad(Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * <p>加载的具体实现，通过传入的参数可以实现分段的懒加载。该方法由非UI线程回调，所以可以执行耗时操作
	 * @param condition
	 * @param start 本次加载的开始序号
	 * @param limit 本次加载的长度
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(Object condition,int start,int limit) throws Exception;
	
	private class LazyLoadingListener implements AbsListView.OnScrollListener{
		private AbsListView.OnScrollListener mOriginalListener = null;
		private int mRemainingCount = 0;
		public LazyLoadingListener(AbsListView.OnScrollListener originalListener,int remainingCount){
			if(originalListener != null && originalListener instanceof LazyLoadingListener) throw new IllegalArgumentException("the OnScrollListener could not be LazyLoadingListener");
			this.mOriginalListener = originalListener;
			this.mRemainingCount = remainingCount;
		}
		@Override
		public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
			// TODO Auto-generated method stub
			//执行原始监听器的逻辑
			if(mOriginalListener != null) mOriginalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			//执行setOnScrollListener时就会触发onScroll，此时要排除AbsListView不可见或可见Item个数为0的情况
			//修改AbsListView的Item个数时会触发onScroll，此时要排除AbsListView不可见的情况
			if(visibleItemCount == 0) return;
			if(firstVisibleItem + visibleItemCount + mRemainingCount >= totalItemCount && !isLoadedAll() && !isException()){
				load(mCurCondition);
			}
		}
		@Override
		public void onScrollStateChanged(AbsListView view,int scrollState) {
			// TODO Auto-generated method stub
			//执行原始监听器的逻辑
			if(mOriginalListener != null) mOriginalListener.onScrollStateChanged(view, scrollState);
		}
		public AbsListView.OnScrollListener getOriginalListener(){
			return mOriginalListener;
		}
	}
	
}
