package cn.emagsoftware.ui.adapter;

import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	
	public DataHolder(Object data){
		mData = data;
	}
	
	/**
	 * <p>使用当前data创建View时触发
	 * <p>可以通过ViewHolder来绑定View的结构信息，从而提高更新时的效率
	 * @param position
	 * @param data
	 * @return
	 */
	public abstract View onCreateView(int position,Object data);
	
	/**
	 * <p>使用当前data更新View时触发，出于节约资源的考虑，View会被复用，此时只需要更新View即可
	 * <p>更新View可以利用ViewHolder来提高效率
	 * @param position
	 * @param view
	 * @param data
	 */
	public abstract void onUpdateView(int position,View view,Object data);
	
	/**
	 * <p>指示异步加载的数据是否已经完成，这可以从当前data的数据状态来判断
	 * <p>如果当前data不需要通过异步来加载额外数据，可直接返回true
	 * @return
	 */
	public abstract boolean isAsyncCompleted();
	
	public Object getData(){
		return mData;
	}
	
}
