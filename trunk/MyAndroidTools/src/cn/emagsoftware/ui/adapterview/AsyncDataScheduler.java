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
	protected int mMaxThreadCount = 0;
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
	
	/**是否需要刷新当前AdapterView的可见区域*/
	protected boolean mIsRefreshVisibleArea = false;
	
	/**用于同步停止调度线程代码块的锁对象*/
	protected byte[] mLockStop = new byte[0];
	/**用于同步提取队列代码块的锁对象*/
	protected byte[] mLockExtract = new byte[0];
	
	public AsyncDataScheduler(AdapterView<?> adapterView,int maxThreadCount,AsyncDataExecutor executor){
		if(adapterView == null || executor == null) throw new NullPointerException();
		if(maxThreadCount <= 0) throw new IllegalArgumentException("maxThreadCount should be great than zero.");
		Adapter adapter = adapterView.getAdapter();
		if(adapter == null) throw new RuntimeException("Adapter is null,call setAdapter function for AdapterView first.");
		if(adapter instanceof WrapperListAdapter) adapter = ((WrapperListAdapter)adapter).getWrappedAdapter();
		if(!(adapter instanceof GenericAdapter)) throw new RuntimeException("To use AsyncDataScheduler,the type of adapter for AdapterView should only be cn.emagsoftware.ui.adapterview.GenericAdapter.");
		mAdapterView = adapterView;
		if(adapterView instanceof ListView) mHeaderCount = ((ListView)adapterView).getHeaderViewsCount();
		mGenericAdapter = (GenericAdapter)adapter;
		mMaxThreadCount = maxThreadCount;
		mExecutor = executor;
	}
	
	public void start(){
		synchronized(mLockStop){
			if(mIsStopped){
				mIsStopping = false;
				mIsStopped = false;
			}else{
				mIsStopping = false;
				return;
			}
		}
		new Thread("AsyncDataScheduler Thread"){
			public void run() {
				while(true){
					synchronized(mLockStop){
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
					synchronized(mLockStop){
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
							if(mIsRefreshVisibleArea){    //需要刷新AdapterView的可见区域
								for(int i = first;i < last + 1;i++){
									DataHolder holder = mGenericAdapter.queryDataHolder(i);
									if(holder.isAsyncDataCompleted()){
										int convertPosition = i - first;
										holder.onUpdateView(mAdapterView.getContext(), i, mAdapterView.getChildAt(convertPosition), holder.getData());
									}else{
										positions.add(i);
										holders.add(holder);
									}
								}
								mIsRefreshVisibleArea = false;
							}else{
								for(int i = first;i < last + 1;i++){
									DataHolder holder = mGenericAdapter.queryDataHolder(i);
									if(!holder.isAsyncDataCompleted()){
										positions.add(i);
										holders.add(holder);
									}
								}
							}
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
					synchronized(mLockStop){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					//用新队列替换提取队列
					synchronized(mLockExtract){
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
					synchronized(mLockStop){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					//计算需要启动的加载线程的个数
					int needThreadCount;
					int eachCount = mExecutor.getEachCount();
					if(eachCount == -1){
						needThreadCount = 1;
					}else{
						int execSize = size - mExtractedIndex;
						int remainder = execSize%eachCount;
						if(remainder == 0) needThreadCount = execSize/eachCount;
						else needThreadCount = execSize/eachCount + 1;
					}
					int remainCount = mMaxThreadCount - mCurrExecutiveThreads.size();
					if(needThreadCount > remainCount) needThreadCount = remainCount;
					//启动异步数据加载线程
					for(int i = 0;i < needThreadCount;i++){
						Thread thread = new Thread(){
							public void run() {
								while(true){
									if(mIsStopping){
										mCurrExecutiveThreads.remove(this);
										return;
									}
									List<Integer> positions = null;
									List<DataHolder> holders = null;
									synchronized(mLockExtract){
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
	
	/**
	 * <p>设置是否需要刷新当前AdapterView的可见区域
	 * <p>一般情况下无需调用该方法，但如果当前AdapterView的Adapter也用于其他的AdapterView，则Adapter的数据有可能会被外部修改，
	 *    所以需要调用该方法来刷新当前AdapterView的可见区域，非可见区域在变得可见时会自动刷新
	 * @param isRefreshVisibleArea
	 */
	public void setRefreshVisibleArea(boolean isRefreshVisibleArea){
		mIsRefreshVisibleArea = isRefreshVisibleArea;
	}
	
}
