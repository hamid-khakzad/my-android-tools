package cn.emagsoftware.ui.adapterview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class GenericAdapter extends BaseAdapter {
	
	protected Context mContext = null;
	protected List<DataHolder> mHolders = new ArrayList<DataHolder>();
	/**是否转换View以提高性能*/
	protected boolean mIsConvertView = true;
	/**是否循环显示View*/
	protected boolean mIsLoopView = false;
	
	public GenericAdapter(Context context){
		if(context == null) throw new NullPointerException();
		mContext = context;
	}
	
	public GenericAdapter(Context context,List<DataHolder> holders){
		this(context);
		addDataHolders(holders);
	}
	
	public void addDataHolder(DataHolder holder){
		mHolders.add(holder);
		notifyDataSetChanged();
	}
	
	public void addDataHolder(int location,DataHolder holder){
		if(mIsLoopView) location = getRealPosition(location);
		mHolders.add(location, holder);
		notifyDataSetChanged();
	}
	
	public void addDataHolders(List<DataHolder> holders){
		mHolders.addAll(holders);
		notifyDataSetChanged();
	}
	
	public void addDataHolders(int location,List<DataHolder> holders){
		if(mIsLoopView) location = getRealPosition(location);
		mHolders.addAll(location, holders);
		notifyDataSetChanged();
	}
	
	public void removeDataHolder(int location){
		if(mIsLoopView) location = getRealPosition(location);
		mHolders.remove(location);
		notifyDataSetChanged();
	}
	
	public void removeDataHolder(DataHolder holder){
		mHolders.remove(holder);
		notifyDataSetChanged();
	}
	
	public void removeDataHolders(List<DataHolder> holders){
		mHolders.removeAll(holders);
		notifyDataSetChanged();
	}
	
	public void updateDataHolder(int location,DataHolder holder){
		if(mIsLoopView) location = getRealPosition(location);
		mHolders.remove(location);
		mHolders.add(location, holder);
		notifyDataSetChanged();
	}
	
	public void updateDataHolders(int location,List<DataHolder> holders){
		if(mIsLoopView) location = getRealPosition(location);
		int oldSize = mHolders.size();
		int tempSize = location + holders.size();
		if(tempSize > oldSize) tempSize = oldSize;
		List<DataHolder> removeList = mHolders.subList(location, tempSize);
		mHolders.removeAll(removeList);
		mHolders.addAll(location, holders);
		notifyDataSetChanged();
	}
	
	public DataHolder queryDataHolder(int location){
		if(mIsLoopView) location = getRealPosition(location);
		return mHolders.get(location);
	}
	
	public int queryDataHolder(DataHolder holder){
		return mHolders.indexOf(holder);
	}
	
	public List<DataHolder> queryDataHolders(int location,int end){
		if(mIsLoopView) {
			location = getRealPosition(location);
			end = (end-1)%getRealCount() + 1;
		}
		return mHolders.subList(location, end);
	}
	
	public boolean queryDataHolders(List<DataHolder> holders){
		return mHolders.containsAll(holders);
	}
	
	public void clearDataHolders(){
		mHolders.clear();
		notifyDataSetChanged();
	}
	
	public void setConvertView(boolean isConvertView){
		mIsConvertView = isConvertView;
		notifyDataSetChanged();
	}
	
	public void setLoopView(boolean isLoopView){
		mIsLoopView = isLoopView;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int size = mHolders.size();
		if(size == 0) return size;
		if(mIsLoopView) return Integer.MAX_VALUE;
		else return size;
	}
	
	public int getRealCount(){
		return mHolders.size();
	}
	
	public int getRealPosition(int position){
		return position%getRealCount();
	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return queryDataHolder(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		DataHolder holder = queryDataHolder(position);
		if(convertView == null || !mIsConvertView){
			return holder.onCreateView(mContext, position, holder.getData());
		}else{
			holder.onUpdateView(mContext, position, convertView, holder.getData());
			return convertView;
		}
	}
	
}
