package cn.emagsoftware.ui.adapterview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	/**额外的(可见范围之外的)需加载异步数据的个数*/
	protected int mExtraCountForExecutingData = 0;
	/**额外的(可见范围之外的)保持异步数据不被回收的个数*/
	protected int mExtraCountForKeepingData = 5;
	
	protected Map<Integer,DataHolder> mResolvedHolders = Collections.synchronizedMap(new HashMap<Integer,DataHolder>());
	
	protected int mExtractedAsyncDataIndex = 0;
	protected int mExtractedIndex = 0;
	protected List<Integer> mExtractedPositions = null;
	protected List<DataHolder> mExtractedHolders = null;
	
	/**是否正在停止*/
	protected boolean mIsStopping = false;
	/**是否已经停止*/
	protected boolean mIsStopped = true;
	/**当前的调度线程*/
	protected Thread mCurrThread = null;
	/**当前的执行线程列表*/
	protected List<Thread> mCurrExecutiveThreads = Collections.synchronizedList(new ArrayList<Thread>());
	
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
	
	/**
	 * <p>获取当前AdapterView的Adapter
	 * @return
	 */
	public GenericAdapter getViewAdapter(){
		return mGenericAdapter;
	}
	
	/**
	 * <p>获取设置的最大线程个数
	 * @return
	 */
	public int getMaxThreadCount(){
		return mMaxThreadCount;
	}
	
	/**
	 * <p>获取设置的异步数据执行者对象
	 * @return
	 */
	public AsyncDataExecutor getAsyncDataExecutor(){
		return mExecutor;
	}
	
	/**
	 * <p>设置额外的(可见范围之外的)需加载异步数据的个数
	 * @param extraCount
	 */
	public void setExtraCountForExecutingData(int extraCount){
		if(extraCount < 0) throw new IllegalArgumentException("extraCount should be great than zero or equals zero");
		mExtraCountForExecutingData = extraCount;
	}
	
	/**
	 * <p>设置额外的(可见范围之外的)保持异步数据不被回收的个数
	 * @param extraCount
	 */
	public void setExtraCountForKeepingData(int extraCount){
		if(extraCount < 0) throw new IllegalArgumentException("extraCount should be great than zero or equals zero");
		mExtraCountForKeepingData = extraCount;
	}
	
	/**
	 * <p>开始或重新开始当前调度器
	 * <p>在已经开始的情况下，重复调用当前方法将不起任何作用
	 */
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
		ThreadPoolManager.executeThread(mCurrThread = new Thread(){
			public void run() {
				while(true){
					synchronized(mLockStop){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					//获取当前时间点需要处理的队列
					final boolean[] isOK = {false};
					final int[] firstAndLastIndex = {0,0};
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
							firstAndLastIndex[0] = mAdapterView.getFirstVisiblePosition() - mHeaderCount;
							firstAndLastIndex[1] = mAdapterView.getLastVisiblePosition() - mHeaderCount;
							if(firstAndLastIndex[0] < 0) firstAndLastIndex[0] = 0;
							else if(firstAndLastIndex[0] >= count) firstAndLastIndex[0] = count - 1;
							if(firstAndLastIndex[1] < 0) firstAndLastIndex[1] = 0;
							else if(firstAndLastIndex[1] >= count) firstAndLastIndex[1] = count - 1;
							int first = firstAndLastIndex[0] - mExtraCountForExecutingData;
							if(first < 0) first = 0;
							int last = firstAndLastIndex[1] + mExtraCountForExecutingData;
							if(last < 0 || last >= count) last = count - 1;
							for(int i = first;i < last + 1;i++){
								positions.add(i);
								holders.add(mGenericAdapter.queryDataHolder(i));
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
					//将范围外DataHolder的异步数据修改为弱引用，方便GC回收
					Set<Integer> resolvedPositionSet = mResolvedHolders.keySet();
					Integer[] resolvedPositions = resolvedPositionSet.toArray(new Integer[resolvedPositionSet.size()]);
					int first = firstAndLastIndex[0]-mExtraCountForKeepingData;
					int last = firstAndLastIndex[1]+mExtraCountForKeepingData;
					if(last < 0) last = Integer.MAX_VALUE;
					for(int i = 0;i < resolvedPositions.length;i++){
						int position = resolvedPositions[i];
						if(position < first || position > last){
							DataHolder holder = mResolvedHolders.get(position);
							for(int j = 0;j < holder.getAsyncDataCount();j++){
								holder.changeAsyncDataToSoftReference(j);
							}
							mResolvedHolders.remove(position);
						}
					}
					synchronized(mLockStop){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					//过滤新获取到的队列
					for(int i = 0;i < positions.size();i++){
						int position = positions.get(i);
						DataHolder holder = holders.get(i);
						boolean isAllAsyncDataCompleted = true;
						for(int j = 0;j < holder.getAsyncDataCount();j++){
							Object asyncData = holder.getAsyncData(j);
							if(asyncData == null) {
								isAllAsyncDataCompleted = false;
							}else {
								holder.setAsyncData(j, asyncData);    //可能是弱引用，将其升级为强引用
								mResolvedHolders.put(position, holder);
							}
						}
						if(isAllAsyncDataCompleted){
							positions.remove(i);
							holders.remove(i);
							i--;
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
					//计算加载线程的个数并启动加载线程
					int needThreadCount;
					int dataHolderCount = mExecutor.getDataHolderCount();
					int asyncDataCount = mExecutor.getAsyncDataCount();
					if(dataHolderCount == -1){
						needThreadCount = 1;
					}else if(dataHolderCount == 1 && asyncDataCount != -1){    //每次只执行一个DataHolder时，计算加载线程个数需要考虑每个DataHolder的异步数据个数
						int tempThreadCount = 0;
						for(int i = mExtractedIndex;i < size;i++){
							DataHolder holder = mExtractedHolders.get(i);
							int curAsyncDataCount = 0;
							for(int j = 0;j < holder.getAsyncDataCount();j++){
								if(holder.getAsyncData(j) == null) curAsyncDataCount = curAsyncDataCount + 1;
							}
							int remainder = curAsyncDataCount%asyncDataCount;
							int curThreadCount;
							if(remainder == 0) curThreadCount = curAsyncDataCount/asyncDataCount;
							else curThreadCount = curAsyncDataCount/asyncDataCount + 1;
							if(curThreadCount == 0) curThreadCount = 1;    //每个DataHolder至少有一个线程来执行异步数据加载
							tempThreadCount = tempThreadCount + curThreadCount;
						}
						needThreadCount = tempThreadCount;
					}else{
						int execSize = size - mExtractedIndex;
						int remainder = execSize%dataHolderCount;
						if(remainder == 0) needThreadCount = execSize/dataHolderCount;
						else needThreadCount = execSize/dataHolderCount + 1;
					}
					int remainCount = mMaxThreadCount - mCurrExecutiveThreads.size();
					if(needThreadCount > remainCount) needThreadCount = remainCount;
					for(int i = 0;i < needThreadCount;i++){
						Thread thread = new Thread(){
							public void run() {
								while(true){
									if(mIsStopping){
										mCurrExecutiveThreads.remove(this);
										return;
									}
									List<Integer> positions = null;    //第一个参数
									List<DataHolder> dataHolders = null;    //第二个参数
									List<Integer> asyncDataIndexes = null;    //第三个参数
									synchronized(mLockExtract){
										int curIndex = mExtractedIndex;
										int endIndex;
										int dataHolderCount = mExecutor.getDataHolderCount();
										if(dataHolderCount == -1){
											endIndex = mExtractedPositions.size();
										}else{
											endIndex = curIndex + dataHolderCount;
											int size = mExtractedPositions.size();
											if(endIndex > size) endIndex = size;
										}
										if(curIndex < endIndex){
											positions = mExtractedPositions.subList(curIndex, endIndex);
											dataHolders = mExtractedHolders.subList(curIndex, endIndex);
											if(dataHolderCount == 1){    //每次只执行一个DataHolder时需要初始化第三个参数
												DataHolder curHolder = dataHolders.get(0);
												int asyncDataCount = mExecutor.getAsyncDataCount();
												asyncDataIndexes = new ArrayList<Integer>();
												for(int i = mExtractedAsyncDataIndex;i < curHolder.getAsyncDataCount();i++){
													if(asyncDataCount != -1){
														if(asyncDataIndexes.size() >= asyncDataCount) break;
													}
													if(curHolder.getAsyncData(i) == null) asyncDataIndexes.add(i);
													mExtractedAsyncDataIndex = i;
												}
												if(asyncDataIndexes.size() == 0){
													mExtractedAsyncDataIndex = 0;
													mExtractedIndex = endIndex;
													continue;
												}else{
													mExtractedAsyncDataIndex = mExtractedAsyncDataIndex + 1;
													if(mExtractedAsyncDataIndex >= curHolder.getAsyncDataCount()){
														mExtractedAsyncDataIndex = 0;
														mExtractedIndex = endIndex;
													}
												}
											}else{
												mExtractedIndex = endIndex;
											}
										}else{
											mCurrExecutiveThreads.remove(this);
											return;
										}
									}
									//执行加载逻辑
									try{
										mExecutor.onExecute(positions, dataHolders, asyncDataIndexes);
									}catch(Exception e){
										int from = positions.get(0);
										int to = positions.get(positions.size() - 1);
										Log.e("AsyncDataScheduler", "execute async data failed from position "+from+" to "+to+".", e);
									}
									//添加加载过的DataHolder到mResolvedHolders
									boolean hasExecuteOneAtLeast = false;
									if(asyncDataIndexes == null){
										for(int i = 0;i < positions.size();i++){
											int position = positions.get(i);
											DataHolder holder = dataHolders.get(i);
											for(int j = 0;j < holder.getAsyncDataCount();j++){
												if(holder.getAsyncData(j) != null){
													hasExecuteOneAtLeast = true;
													mResolvedHolders.put(position, holder);
													break;
												}
											}
										}
									}else{
										int position = positions.get(0);
										DataHolder holder = dataHolders.get(0);
										for(int i = 0;i < asyncDataIndexes.size();i++){
											if(holder.getAsyncData(asyncDataIndexes.get(i)) != null){
												hasExecuteOneAtLeast = true;
												mResolvedHolders.put(position, holder);
												break;
											}
										}
									}
									//更新界面
									if(hasExecuteOneAtLeast){
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												// TODO Auto-generated method stub
												//通过Adapter的notifyDataSetChanged方法更新显示，可以使绑定到该Adapter的所有AdapterView都能得到更新
												mGenericAdapter.notifyDataSetChanged();
											}
										});
									}
								}
							}
						};
						mCurrExecutiveThreads.add(thread);
						ThreadPoolManager.executeThread(thread);
					}
					synchronized(mLockStop){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					try{
						sleep(SCHEDULER_DORMANCY_TIME);
					}catch(InterruptedException e){
						Log.i("AsyncDataScheduler", "speed up scheduling...");
					}
				}
			};
		});
	}
	
	/**
	 * <p>加快一次调度器的调度速度，调度器在两次调度之间会有一定的休眠
	 * <p>一般情况下无需调用此方法，除非对调度器调度的及时性要求较高
	 */
	public void speedup(){
		if(mCurrThread != null) mCurrThread.interrupt();
	}
	
	/**
	 * <p>停止当前调度器
	 */
	public void stop(){
		mIsStopping = true;
	}
	
}
