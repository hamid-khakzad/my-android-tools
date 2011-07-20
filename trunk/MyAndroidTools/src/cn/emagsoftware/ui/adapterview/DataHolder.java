package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	protected boolean mIsAsyncDataCompleted = false;
	
	public DataHolder(Object data){
		mData = data;
	}
	
	/**
	 * <p>使用当前data创建View时触发
	 * <p>如果存在异步数据的加载，在创建View时需通过isAsyncDataCompleted()方法来判断异步数据是否加载完成，若加载完成，也要更新到View上
	 * <p>可以通过ViewHolder来绑定View的结构信息，从而提高更新时的效率
	 * @param context
	 * @param position
	 * @param data
	 * @return
	 */
	public abstract View onCreateView(Context context,int position,Object data);
	
	/**
	 * <p>使用当前data更新View时触发，出于节约资源的考虑，View默认会被复用，此时只需要更新View即可
	 * <p>如果存在异步数据的加载，在更新View时需通过isAsyncDataCompleted()方法来判断异步数据是否加载完成，若加载完成，也要更新到View上
	 * <p>若通过GenericAdapter的setConvertView方法设置了View不复用，可以保持该方法的实现为空
	 * <p>更新View可以通过ViewHolder来提高效率
	 * @param context
	 * @param position
	 * @param view
	 * @param data
	 */
	public abstract void onUpdateView(Context context,int position,View view,Object data);
	
	public Object getData(){
		return mData;
	}
	
	public boolean isAsyncDataCompleted(){
		return mIsAsyncDataCompleted;
	}
	
	public void setAsyncDataCompleted(boolean isAsyncDataCompleted){
		mIsAsyncDataCompleted = isAsyncDataCompleted;
	}
	
}
