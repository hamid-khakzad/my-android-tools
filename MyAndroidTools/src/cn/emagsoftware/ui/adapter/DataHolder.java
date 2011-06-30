package cn.emagsoftware.ui.adapter;

import android.view.View;

public abstract class DataHolder<T> {
	
	protected T mData = null;
	
	public DataHolder(T data){
		mData = data;
	}
	
	public abstract View onCreateView(int position,T data);
	
	public abstract void onUpdateView(int position,View view,T data);
	
	public T getData(){
		return mData;
	}
	
}
