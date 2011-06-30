package cn.emagsoftware.ui.adapter;

import android.view.View;

public abstract class ViewHolder {
	
	protected Object data = null;
	
	public ViewHolder(Object data){
		this.data = data;
	}
	
	public abstract View onCreateView(int position,Object data);
	
	public abstract void onUpdateView(int position,View view,Object data);
	
	
	
	
	
	public void updateData(Object data){
		this.data = data;
	}
	
	public Object getData(){
		return data;
	}
	
}
