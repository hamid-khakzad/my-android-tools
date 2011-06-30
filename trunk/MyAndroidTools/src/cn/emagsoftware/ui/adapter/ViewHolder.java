package cn.emagsoftware.ui.adapter;

import android.view.View;

public class ViewHolder {
	
	protected View[] mParams = null;
	
	public ViewHolder(){
	}
	
	public ViewHolder(View... params){
		mParams = params;
	}
	
	public void setParams(View... params){
		mParams = params;
	}
	
	public View[] getParams(){
		return mParams;
	}
	
}
