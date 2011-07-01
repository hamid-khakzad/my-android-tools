package cn.emagsoftware.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

public class SimpleAdapter extends BaseAdapter {
	
	protected Context mContext = null;
	protected List<DataHolder> mHolders = new ArrayList<DataHolder>();
	/**使用此Adapter的容器*/
	protected ViewGroup mOwner = null;
	/**异步数据加载的调度器*/
	protected AsyncDataScheduler mScheduler = null;
	
	public SimpleAdapter(Context context){
		mContext = context;
	}
	
	public SimpleAdapter(Context context,List<DataHolder> holders){
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
	
	/**
	 * <p>绑定异步数据加载的调度器
	 * @param scheduler
	 */
	public void bindAsyncDataScheduler(AsyncDataScheduler scheduler){
		if(mScheduler != null) throw new RuntimeException("bindAsyncDataScheduler method can only be called once.");
		mScheduler = scheduler;
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
		mOwner = parent;
		if(mScheduler != null && mOwner instanceof AdapterView<?>){
			AdapterView<?> adapterOwner = (AdapterView<?>)mOwner;
			int first = adapterOwner.getFirstVisiblePosition();
			int last = adapterOwner.getLastVisiblePosition();
			mScheduler.updateQueue(first, last, queryDataHolders(first,last + 1));    //更新异步数据调度器的调度队列
			if(!mScheduler.isStarted()) mScheduler.start();    //启动异步数据调度器
		}
		DataHolder holder = mHolders.get(position);
		if(convertView == null){
			return holder.onCreateView(position, holder.getData());
		}else{
			holder.onUpdateView(position, convertView, holder.getData());
			return convertView;
		}
	}
	
}
