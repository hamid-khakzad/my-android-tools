package cn.emagsoftware.ui.adapterview;

import java.lang.reflect.Field;
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
		onBeginLoad(mContext,condition);    //在load中调用而不是在UIThread中，可使UI线程衔接一致，避免带来不同步的情况
		ThreadPoolManager.executeThread(new UIThread(mContext){
			@Override
			protected Object onRunNoUI(Context context) throws Exception {
				// TODO Auto-generated method stub
				super.onRunNoUI(context);
				return onLoad(context,condition,mStart,mLimit);
			}
			@SuppressWarnings("unchecked")
			@Override
			protected void onSuccessUI(Context context,Object result) {
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
			protected void onExceptionUI(Context context,Exception e) {
				// TODO Auto-generated method stub
				super.onExceptionUI(context,e);
				Log.e("BaseLazyLoadingAdapter","Execute lazy loading failed.",e);
				mIsLoading = false;
				mIsException = true;
				onAfterLoad(context,condition,e);
			}
		});
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
