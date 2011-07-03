package cn.emagsoftware.ui.adapterview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Adapter;
import android.widget.AdapterView;

public class AsyncDataScheduler extends Thread {
	
	/**异步数据调度器的休眠时间，以毫秒为单位*/
	public static final int SCHEDULER_DORMANCY_TIME = 2000;
	
	protected AdapterView<?> mAdapterView = null;
	protected SimpleAdapter mSimpleAdapter = null;
	protected int mMaxThreadCount = 0;
	protected AsyncDataExecutor mExecutor = null;
	
	protected boolean mIsCancelMe = false;
	protected boolean mIsCancelThreads = false;
	
	protected int mExtractedIndex = 0;
	protected List<Integer> mExtractedPositions = null;
	protected List<DataHolder> mExtractedHolders = null;
	protected List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>());
	
	public AsyncDataScheduler(AdapterView<?> adapterView,int maxThreadCount,AsyncDataExecutor executor){
		if(adapterView == null || executor == null) throw new NullPointerException();
		if(maxThreadCount <= 0) throw new IllegalArgumentException("maxThreadCount should be great than zero.");
		Adapter adapter = adapterView.getAdapter();
		if(adapter == null) throw new RuntimeException("Adapter is null,call setAdapter function for AdapterView first.");
		if(!(adapter instanceof SimpleAdapter)) throw new RuntimeException("To use AsyncDataScheduler,the type of adapter for AdapterView should only be cn.emagsoftware.ui.adapterview.SimpleAdapter.");
		mAdapterView = adapterView;
		mSimpleAdapter = (SimpleAdapter)adapter;
		mMaxThreadCount = maxThreadCount;
		mExecutor = executor;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		while(true){
			if(mIsCancelMe) break;
			try{
				sleep(SCHEDULER_DORMANCY_TIME);
			}catch(InterruptedException e){
				break;
			}
			if(mIsCancelMe) break;
			//获取当前时间点需要处理的快照
			final boolean[] isOK = {false};
			final List<Integer> positions = new LinkedList<Integer>();
			final List<DataHolder> holders = new LinkedList<DataHolder>();
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					int first = mAdapterView.getFirstVisiblePosition();
					int last = mAdapterView.getLastVisiblePosition();
					for(int i = first;i < last + 1;i++){
						positions.add(i);
					}
					holders.addAll(mSimpleAdapter.queryDataHolders(first, last + 1));
					isOK[0] = true;
				}
			});
			while(!isOK[0]){
				try{
					sleep(500);
				}catch(InterruptedException e){
					break;
				}
			}
			//删除已经加载过的项
			for(int i = 0;i < positions.size();i++){
				DataHolder holder = holders.get(i);
				if(holder.isAsyncDataCompleted()){
					positions.remove(i);
					holders.remove(i);
					i--;
				}
			}
			//用新队列替换提取队列
			synchronized(AsyncDataScheduler.this){
				if(mExtractedPositions == null || positions.size() == 0){
					mExtractedIndex = 0;
					mExtractedPositions = positions;
					mExtractedHolders = holders;
				}else{
					int tempIndex = 0;
					for(int i = 0;i < mExtractedIndex;i++){
						DataHolder extractedHolder = mExtractedHolders.get(i);
						int index = holders.indexOf(extractedHolder);
						if(index != -1){
							positions.add(0, positions.remove(index));
							holders.add(0,holders.remove(index));
							tempIndex++;
						}
					}
					mExtractedIndex = tempIndex;
					mExtractedPositions = positions;
					mExtractedHolders = holders;
				}
			}
			if(mExtractedPositions.size() == 0) continue;
			//启动异步数据加载线程
			int remainCount = mMaxThreadCount - threads.size();
			for(int i = 0;i < remainCount;i++){
				Thread thread = new Thread(){
					public void run() {
						while(true){
							if(mIsCancelThreads){
								threads.remove(this);
								break;
							}
							List<Integer> positions = null;
							List<DataHolder> holders = null;
							synchronized(AsyncDataScheduler.this){
								int currIndex = mExtractedIndex;
								int endIndex = currIndex + mExecutor.getEachCount();
								int size = mExtractedPositions.size();
								if(endIndex > size) endIndex = size;
								if(currIndex < endIndex){
									positions = mExtractedPositions.subList(currIndex, endIndex);
									holders = mExtractedHolders.subList(currIndex, endIndex);
									mExtractedIndex = endIndex;
								}else{
									threads.remove(this);
									break;
								}
							}
							if(mIsCancelThreads){
								threads.remove(this);
								break;
							}
							//执行加载逻辑
							try{
								mExecutor.onExecute(positions, holders);
								//置执行成功标志
								for(int i = 0;i < positions.size();i++){
									DataHolder dholder = holders.get(i);
									dholder.setAsyncDataCompleted(true);
								}
								//如果当前项正好此时显示在界面上，将回调onUpdateView方法
								final List<Integer> positionsCopy = positions;
								final List<DataHolder> holdersCopy = holders;
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										int firstVisible = mAdapterView.getFirstVisiblePosition();
										int lastVisible = mAdapterView.getLastVisiblePosition();
										for(int i = 0;i < positionsCopy.size();i++){
											int position = positionsCopy.get(i);
											if(position >= firstVisible && position <= lastVisible){
												DataHolder dholder = holdersCopy.get(i);
												dholder.onUpdateView(position, mAdapterView.getChildAt(position), dholder.getData());
											}
										}
									}
								});
							}catch(Exception e){
								int from = positions.get(0);
								int to = positions.get(positions.size() - 1);
								Log.e("AsyncDataScheduler", "execute async data failed from position "+from+" to "+to+".", e);
							}
						}
					}
				};
				threads.add(thread);
				thread.start();
			}
		}
	}
	
	public void cancelMe(){
		mIsCancelMe = true;
		interrupt();
	}
	
	public void cancelThreads(){
		mIsCancelThreads = true;
	}
	
}
