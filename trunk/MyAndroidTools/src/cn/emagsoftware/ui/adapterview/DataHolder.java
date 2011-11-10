package cn.emagsoftware.ui.adapterview;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	protected Object[] mAsyncData = null;
	
	/**
	 * <p>构造函数
	 * @param data 需要用到的数据
	 * @param asyncDataCount 需要被执行的异步数据的个数
	 */
	public DataHolder(Object data,int asyncDataCount){
		mData = data;
		mAsyncData = new Object[asyncDataCount];
	}
	
	/**
	 * <p>使用当前data创建View时触发
	 * <p>如果存在异步数据的加载，在创建View时需通过getAsyncData(int index)是否为null来判断所有异步数据是否加载完成，若加载完成，也要更新到View上
	 * <p>可以通过ViewHolder来绑定View的结构信息，从而提高更新时的效率
	 * @param context
	 * @param position
	 * @param data
	 * @return
	 */
	public abstract View onCreateView(Context context,int position,Object data);
	
	/**
	 * <p>使用当前data更新View时触发，出于节约资源的考虑，View默认会被复用，此时只需要更新View即可
	 * <p>如果存在异步数据的加载，在更新View时需通过getAsyncData(int index)是否为null来判断所有异步数据是否加载完成，若加载完成，也要更新到View上
	 * <p>若通过GenericAdapter的setConvertView方法设置了View不复用，可以保持该方法的实现为空
	 * <p>更新View可以通过ViewHolder来提高效率
	 * @param context
	 * @param position
	 * @param view
	 * @param data
	 */
	public abstract void onUpdateView(Context context,int position,View view,Object data);
	
	/**
	 * <p>获取构造函数中传入的数据对象
	 * @return
	 */
	public Object getData(){
		return mData;
	}
	
	/**
	 * <p>获取指定位置的异步数据，未加载或已被回收时返回null
	 * @param index 异步数据的位置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object getAsyncData(int index){
		SoftReference<Object> asyncDataRef = (SoftReference<Object>)mAsyncData[index];
		if(asyncDataRef == null) return null;
		return asyncDataRef.get();
	}
	
	/**
	 * <p>设置指定位置的异步数据
	 * @param index 异步数据的位置
	 * @param asyncData
	 */
	public void setAsyncData(int index,Object asyncData){
		mAsyncData[index] = new SoftReference<Object>(asyncData);
	}
	
	/**
	 * <p>获取异步数据的个数
	 * @return
	 */
	public int getAsyncDataCount(){
		return mAsyncData.length;
	}
	
}
