package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.ui.UIThread;

import android.content.Context;
import android.util.Log;

public abstract class BaseLoadingAdapter extends GenericAdapter{
	
	/**是否正在加载*/
	protected boolean mIsLoading = false;
	/**是否已经加载过*/
	protected boolean mIsLoaded = false;
	/**当前的加载条件*/
	protected Object mCurCondition = null;
	
	public BaseLoadingAdapter(Context context){
		super(context);
	}
	
	/**
	 * <p>加载的执行方法
	 * @param condition 加载时需要的条件，没有时可传null
	 * @return true表示开始加载；false表示已经在加载，本次的调用无效
	 */
	public boolean load(final Object condition){
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
				return onLoad(context,condition);
			}
			@Override
			public void onSuccessUI(Context context,Object result) {
				// TODO Auto-generated method stub
				super.onSuccessUI(context,result);
				if(result == null){
					mIsLoading = false;
					mIsLoaded = true;
					onAfterLoad(context,condition,null);
				}else{
					addDataHolders((List<DataHolder>)result);    //该方法需在UI线程中执行且是非线程安全的
					mIsLoading = false;
					mIsLoaded = true;
					onAfterLoad(context,condition,null);
				}
			}
			@Override
			public void onExceptionUI(Context context,Exception e) {
				// TODO Auto-generated method stub
				super.onExceptionUI(context,e);
				Log.e("BaseLoadingAdapter","Execute loading failed.",e);
				mIsLoading = false;
				onAfterLoad(context,condition,e);
			}
		}));
		return true;
	}
	
	/**
	 * <p>获取当前的加载条件
	 * @return
	 */
	public Object getCurCondition(){
		return mCurCondition;
	}
	
	/**
	 * <p>是否正在加载
	 * @return
	 */
	public boolean isLoading(){
		return mIsLoading;
	}
	
	/**
	 * <p>是否已经加载过
	 * @return
	 */
	public boolean isLoaded(){
		return mIsLoaded;
	}
	
	/**
	 * <p>在加载之前的回调方法，可以显示一些loading之类的字样。如对于ListView，可以通过addFooterView方法添加一个正在加载的提示
	 * @param context
	 * @param condition
	 */
	public abstract void onBeginLoad(Context context,Object condition);
	
	/**
	 * <p>加载的具体实现，该方法将在非UI线程中执行，要注意不能执行UI的操作
	 * @param context
	 * @param condition
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(Context context,Object condition) throws Exception;
	
	/**
	 * <p>加载完成后的回调方法，可以通过判断exception是否为null来获悉加载成功与否，从而给用户一些提示
	 * @param context
	 * @param condition
	 * @param exception
	 */
	public abstract void onAfterLoad(Context context,Object condition,Exception exception);
	
}
