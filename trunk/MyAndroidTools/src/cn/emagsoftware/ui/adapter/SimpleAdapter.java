package cn.emagsoftware.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SimpleAdapter extends BaseAdapter {
	
	protected Context context = null;
	protected List<ViewHolder> views = new ArrayList<ViewHolder>();
	
	public SimpleAdapter(Context context){
		this.context = context;
	}
	
	public SimpleAdapter(Context context,List<ViewHolder> views){
		this(context);
		addViews(views);
	}
	
	public void addView(ViewHolder holder){
		views.add(holder);
		notifyDataSetChanged();
	}
	
	public void addView(int location,ViewHolder holder){
		views.add(location, holder);
		notifyDataSetChanged();
	}
	
	public void addViews(List<ViewHolder> views){
		this.views.addAll(views);
		notifyDataSetChanged();
	}
	
	public void addViews(int location,List<ViewHolder> views){
		this.views.addAll(location, views);
		notifyDataSetChanged();
	}
	
	public void removeView(int location){
		views.remove(location);
		notifyDataSetChanged();
	}
	
	public void removeView(ViewHolder holder){
		views.remove(holder);
		notifyDataSetChanged();
	}
	
	public void removeViews(List<ViewHolder> views){
		this.views.removeAll(views);
		notifyDataSetChanged();
	}
	
	public void updateView(int location,ViewHolder holder){
		views.remove(location);
		views.add(location, holder);
		notifyDataSetChanged();
	}
	
	public void updateViews(int location,List<ViewHolder> views){
		int oldSize = this.views.size();
		int tempSize = location + views.size();
		if(tempSize > oldSize) tempSize = oldSize;
		List<ViewHolder> removeList = this.views.subList(location, tempSize);
		this.views.removeAll(removeList);
		this.views.addAll(location, views);
		notifyDataSetChanged();
	}
	
	public void updateViewData(int location,Object data){
		views.get(location).updateData(data);
	}
	
	public void updateViewsData(int location,List<Object> data){
		int index = 0;
		for(int i = location;i < views.size();i++){
			if(index >= data.size()) break;
			views.get(i).updateData(data.get(index));
			index = index + 1;
		}
	}
	
	public ViewHolder queryView(int location){
		return views.get(location);
	}
	
	public int queryView(ViewHolder holder){
		return views.indexOf(holder);
	}
	
	public List<ViewHolder> queryViews(int location,int end){
		return views.subList(location, end);
	}
	
	public boolean queryViews(List<ViewHolder> views){
		return this.views.containsAll(views);
	}
	
	public void clearViews(){
		views.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return views.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		
		
		
		
		
		return null;
	}
	
}
