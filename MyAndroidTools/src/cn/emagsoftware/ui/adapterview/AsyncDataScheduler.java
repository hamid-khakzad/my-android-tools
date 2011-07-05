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
import android.widget.ListView;
import android.widget.WrapperListAdapter;

public class AsyncDataScheduler {
	
	/**异步数据调度器的休眠时间，以毫秒为单位*/
	public static final int SCHEDULER_DORMANCY_TIME = 2000;
	
	protected AdapterView<?> mAdapterView = null;
	protected int mHeaderCount = 0;
	protected GenericAdapter mGenericAdapter = null;
	protected int mThreadCount = 0;
	protected AsyncDataExecutor mExecutor = null;
	
	protected int mExtractedIndex = 0;
	protected List<Integer> mExtractedPositions = null;
	protected List<DataHolder> mExtractedHolders = null;
	
	/**是否正在停止*/
	protected boolean mIsStopping = false;
	/**是否已经停止*/
	protected boolean mIsStopped = true;
	/**当前的执行线程*/
	protected List<Thread> mCurrExecutiveThreads = Collections.synchronizedList(new ArrayList<Thread>());
	
	/**用于同步代码块而创建的锁对象*/
	protected byte[] mLock1 = new byte[0];
	/**用于同步代码块而创建的锁对象*/
	protected byte[] mLock2 = new byte[0];
	
	public AsyncDataScheduler(AdapterView<?> adapterView,int threadCount,AsyncDataExecutor executor){
		if(adapterView == null || executor == null) throw new NullPointerException();
		if(threadCount <= 0) throw new IllegalArgumentException("maxThreadCount should be great than zero.");
		Adapter adapter = adapterView.getAdapter();
		if(adapter == null) throw new RuntimeException("Adapter is null,call setAdapter function for AdapterView first.");
		if(adapter instanceof WrapperListAdapter) adapter = ((WrapperListAdapter)adapter).getWrappedAdapter();
		if(!(adapter instanceof GenericAdapter)) throw new RuntimeException("To use AsyncDataScheduler,the type of adapter for AdapterView should only be cn.emagsoftware.ui.adapterview.GenericAdapter.");
		mAdapterView = adapterView;
		if(adapterView instanceof ListView) mHeaderCount = ((ListView)adapterView).getHeaderViewsCount();
		mGenericAdapter = (GenericAdapter)adapter;
		mThreadCount = threadCount;
		mExecutor = executor;
	}
	
	public void start(){
		synchronized(mLock1){
			if(mIsStopped){
				mIsStopping = false;
				mIsStopped = false;
			}else{
				mIsStopping = false;
				return;
			}
		}
		new Thread(){
			public void run() {
				while(true){
					synchronized(mLock1){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					try{
						sleep(SCHEDULER_DORMANCY_TIME);
					}catch(InterruptedException e){
						throw new RuntimeException(e);
					}
					synchronized(mLock1){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					//获取当前时间点需要处理的快照
					final boolean[] isOK = {false};
					final List<Integer> positions = new LinkedList<Integer>();
					final List<DataHolder> holders = new LinkedList<DataHolder>();
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							int count = mGenericAdapter.getCount();
							if(count <= 0) {
								isOK[0] = true;
								return;
							}
							int first = mAdapterView.getFirstVisiblePosition() - mHeaderCount;
							int last = mAdapterView.getLastVisiblePosition() - mHeaderCount;
							if(first < 0) first = 0;
							else if(first >= count) first = count - 1;
							if(last < 0) last = 0;
							else if(last >= count) last = count - 1;
							for(int i = first;i < last + 1;i++){
								positions.add(i);
							}
							holders.addAll(mGenericAdapter.queryDataHolders(first, last + 1));
							isOK[0] = true;
						}
					});
					while(!isOK[0]){
						try{
							sleep(200);
						}catch(InterruptedException e){
							throw new RuntimeException(e);
						}
					}
					synchronized(mLock1){
						if(mIsStopping){
							mIsStopped = true;
							return;
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
					synchronized(mLock2){
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
					int size = mExtractedPositions.size();
					if(size == 0 || mExtractedIndex == size) continue;    //如果当前提取队列没有增加新的项，将不会启动加载线程，以节约资源
					synchronized(mLock1){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					//启动异步数据加载线程
					int remainCount = mThreadCount - mCurrExecutiveThreads.size();
					for(int i = 0;i < remainCount;i++){
						Thread thread = new Thread(){
							public void run() {
								while(true){
									if(mIsStopping){
										mCurrExecutiveThreads.remove(this);
										return;
									}
									List<Integer> positions = null;
									List<DataHolder> holders = null;
									synchronized(mLock2){
										int currIndex = mExtractedIndex;
										int endIndex;
										int eachCount = mExecutor.getEachCount();
										if(eachCount == -1){
											endIndex = mExtractedPositions.size();
										}else{
											endIndex = currIndex + eachCount;
											int size = mExtractedPositions.size();
											if(endIndex > size) endIndex = size;
										}
										if(currIndex < endIndex){
											positions = mExtractedPositions.subList(currIndex, endIndex);
											holders = mExtractedHolders.subList(currIndex, endIndex);
											mExtractedIndex = endIndex;
										}else{
											mCurrExecutiveThreads.remove(this);
											return;
										}
									}
									if(mIsStopping){
										mCurrExecutiveThreads.remove(this);
										return;
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
												int count = mGenericAdapter.getCount();
												if(count <= 0) return;
												int first = mAdapterView.getFirstVisiblePosition() - mHeaderCount;
												int last = mAdapterView.getLastVisiblePosition() - mHeaderCount;
												if(first < 0) first = 0;
												else if(first >= count) first = count - 1;
												if(last < 0) last = 0;
												else if(last >= count) last = count - 1;
												for(int i = 0;i < positionsCopy.size();i++){
													int position = positionsCopy.get(i);
													if(position >= first && position <= last){
														DataHolder dholder = holdersCopy.get(i);
														int convertPosition = position - first;
														dholder.onUpdateView(mAdapterView.getContext(), position, mAdapterView.getChildAt(convertPosition), dholder.getData());
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
						mCurrExecutiveThreads.add(thread);
						thread.start();
					}
				}
			};
		}.start();
	}
	
	public void stop(){
		mIsStopping = true;
	}
	
}
