package cn.emagsoftware.ui.adapterview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class GenericAdapter extends BaseAdapter implements Cloneable {
	
	protected Context mContext = null;
	protected List<DataHolder> mHolders = new ArrayList<DataHolder>();
	/**是否转换View以提高性能*/
	protected boolean mIsConvertView = true;
	
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
		mHolders.add(location, holder);
		notifyDataSetChanged();
	}
	
	public void addDataHolders(List<DataHolder> holders){
		mHolders.addAll(holders);
		notifyDataSetChanged();
	}
	
	public void addDataHolders(int location,List<DataHolder> holders){
		mHolders.addAll(location, holders);
		notifyDataSetChanged();
	}
	
	public void removeDataHolder(int location){
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
		mHolders.remove(location);
		mHolders.add(location, holder);
		notifyDataSetChanged();
	}
	
	public void updateDataHolders(int location,List<DataHolder> holders){
		int oldSize = mHolders.size();
		int tempSize = location + holders.size();
		if(tempSize > oldSize) tempSize = oldSize;
		List<DataHolder> removeList = mHolders.subList(location, tempSize);
		mHolders.removeAll(removeList);
		mHolders.addAll(location, holders);
		notifyDataSetChanged();
	}
	
	public DataHolder queryDataHolder(int location){
		return mHolders.get(location);
	}
	
	public int queryDataHolder(DataHolder holder){
		return mHolders.indexOf(holder);
	}
	
	public List<DataHolder> queryDataHolders(int location,int end){
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
	}
	
	/**
	 * <p>深度克隆当前对象，以供需要相同数据的其他AdapterView使用
	 * @param cloneMgr
	 * @return
	 */
	public GenericAdapter cloneDeeply(DataHolderCloneMgr cloneMgr){
		GenericAdapter cloneOne = null;
		try{
			cloneOne = (GenericAdapter)super.clone();
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
		//以下开始深度克隆
		List<DataHolder> clone = new ArrayList<DataHolder>();
		for(int i = 0;i < mHolders.size();i++){
			DataHolder originalHolder = mHolders.get(i);
			clone.add(cloneMgr.clone(i, originalHolder));
		}
		cloneOne.mHolders = clone;
		return cloneOne;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mHolders.size();
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
		DataHolder holder = mHolders.get(position);
		if(convertView == null || !mIsConvertView){
			return holder.onCreateView(mContext, position, holder.getData());
		}else{
			holder.onUpdateView(mContext, position, convertView, holder.getData());
			return convertView;
		}
	}
	
	public abstract static interface DataHolderCloneMgr{
		public abstract DataHolder clone(int position,DataHolder holder);
	}
	
}
