package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	protected boolean[] mIsAsyncDataCompleted = null;
	protected Object mTag = null;
	
	/**
	 * <p>构造函数
	 * @param data 需要用到的数据
	 * @param asyncDataCount 需要被执行的异步数据的个数
	 */
	public DataHolder(Object data,int asyncDataCount){
		mData = data;
		mIsAsyncDataCompleted = new boolean[asyncDataCount];
		for(int i = 0;i < mIsAsyncDataCompleted.length;i++){
			mIsAsyncDataCompleted[i] = false;
		}
	}
	
	/**
	 * <p>使用当前data创建View时触发
	 * <p>如果存在异步数据的加载，在创建View时需通过isAsyncDataCompleted(int index)方法来判断所有异步数据是否加载完成，若加载完成，也要更新到View上
	 * <p>可以通过ViewHolder来绑定View的结构信息，从而提高更新时的效率
	 * @param context
	 * @param position
	 * @param data
	 * @return
	 */
	public abstract View onCreateView(Context context,int position,Object data);
	
	/**
	 * <p>使用当前data更新View时触发，出于节约资源的考虑，View默认会被复用，此时只需要更新View即可
	 * <p>如果存在异步数据的加载，在更新View时需通过isAsyncDataCompleted(int index)方法来判断所有异步数据是否加载完成，若加载完成，也要更新到View上
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
	 * <p>获取异步数据的个数
	 * @return
	 */
	public int getAsyncDataCount(){
		return mIsAsyncDataCompleted.length;
	}
	
	/**
	 * <p>获取指定的异步数据是否加载完成
	 * @param index 异步数据所在的位置
	 * @return
	 */
	public boolean isAsyncDataCompleted(int index){
		return mIsAsyncDataCompleted[index];
	}
	
	/**
	 * <p>设置指定的异步数据的加载完成情况
	 * @param index 异步数据所在的位置
	 * @param isCompleted 是否已加载完成
	 */
	public void setAsyncDataCompleted(int index,boolean isCompleted){
		mIsAsyncDataCompleted[index] = isCompleted;
	}
	
	/**
	 * <p>设置标签，通过该操作可以存储一些有用的数据，如异步加载的数据
	 * <p>需要顺便指出的是，除非异步加载的数据较小且DataHolder实例较少，否则<b>强烈建议</b>将异步加载的数据存入本地SQLite数据库，在需要展示时再实时查询
	 * @param tag
	 */
	public void setTag(Object tag){
		mTag = tag;
	}
	
	/**
	 * <p>获取标签
	 * @return
	 */
	public Object getTag(){
		return mTag;
	}
	
}
