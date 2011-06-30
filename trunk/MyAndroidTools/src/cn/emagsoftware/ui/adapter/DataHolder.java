package cn.emagsoftware.ui.adapter;

import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	
	public DataHolder(Object data){
		mData = data;
	}
	
	public abstract View onCreateView(int position,Object data);
	
	public abstract void onUpdateView(int position,View view,Object data);
	
	public Object getData(){
		return mData;
	}
	
}
