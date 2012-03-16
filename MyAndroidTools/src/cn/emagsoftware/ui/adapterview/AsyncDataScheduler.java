package cn.emagsoftware.ui.adapterview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.WrapperListAdapter;
import cn.emagsoftware.util.LogManager;

public class AsyncDataScheduler {
	
	/**�첽���ݵ�����������ʱ�䣬�Ժ���Ϊ��λ*/
	public static final int SCHEDULER_DORMANCY_TIME = 2000;
	
	protected AdapterView<?> mAdapterView = null;
	protected int mHeaderCount = 0;
	protected GenericAdapter mGenericAdapter = null;
	protected int mMaxThreadCount = 0;
	protected AsyncDataExecutor mExecutor = null;
	/**UI�̵߳�Handler*/
	protected Handler mHandler = new Handler(Looper.getMainLooper());
	
	/**�����(�ɼ���Χ֮���)������첽���ݵĸ���*/
	protected int mExtraCountForExecutingData = 0;
	/**�����(�ɼ���Χ֮���)�����첽���ݲ������յĸ���*/
	protected int mExtraCountForKeepingData = 5;
	
	protected Map<Integer,DataHolder> mResolvedHolders = Collections.synchronizedMap(new HashMap<Integer,DataHolder>());
	
	protected int mExtractedAsyncDataIndex = 0;
	protected int mExtractedIndex = 0;
	protected List<Integer> mExtractedPositions = null;
	protected List<DataHolder> mExtractedHolders = null;
	
	/**�Ƿ�����ֹͣ*/
	protected boolean mIsStopping = false;
	/**�Ƿ��Ѿ�ֹͣ*/
	protected boolean mIsStopped = true;
	/**��ǰ��ִ���߳��б�*/
	protected List<Thread> mCurrExecutiveThreads = Collections.synchronizedList(new ArrayList<Thread>());
	
	/**����ͬ��ֹͣ�����̴߳�����������*/
	protected byte[] mLockStop = new byte[0];
	/**����ͬ����ȡ���д�����������*/
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
	 * <p>��ȡ��ǰAdapterView��Adapter
	 * @return
	 */
	public GenericAdapter getViewAdapter(){
		return mGenericAdapter;
	}
	
	/**
	 * <p>��ȡ���õ�����̸߳���
	 * @return
	 */
	public int getMaxThreadCount(){
		return mMaxThreadCount;
	}
	
	/**
	 * <p>��ȡ���õ��첽����ִ���߶���
	 * @return
	 */
	public AsyncDataExecutor getAsyncDataExecutor(){
		return mExecutor;
	}
	
	/**
	 * <p>���ö����(�ɼ���Χ֮���)������첽���ݵĸ���
	 * @param extraCount
	 */
	public void setExtraCountForExecutingData(int extraCount){
		if(extraCount < 0) throw new IllegalArgumentException("extraCount should be great than zero or equals zero");
		mExtraCountForExecutingData = extraCount;
	}
	
	/**
	 * <p>���ö����(�ɼ���Χ֮���)�����첽���ݲ������յĸ���
	 * @param extraCount
	 */
	public void setExtraCountForKeepingData(int extraCount){
		if(extraCount < 0) throw new IllegalArgumentException("extraCount should be great than zero or equals zero");
		mExtraCountForKeepingData = extraCount;
	}
	
	/**
	 * <p>��ʼ�����¿�ʼ��ǰ������
	 * <p>���Ѿ���ʼ������£��ظ����õ�ǰ�����������κ�����
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
		ThreadPoolManager.executeThread(new Thread(){
			public void run() {
				while(true){
					synchronized(mLockStop){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					//��ȡ��ǰʱ�����Ҫ����Ķ���
					final boolean[] isOK = {false};
					final int[] allFirstAndLast = {0,-1,-1};
					final List<Integer> positions = new LinkedList<Integer>();
					final List<DataHolder> holders = new LinkedList<DataHolder>();
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							int adapterCount = mGenericAdapter.getCount();
							if(adapterCount <= 0){
								isOK[0] = true;
								return;
							}
							allFirstAndLast[0] = adapterCount;
							int count = mAdapterView.getChildCount();    //������header��footer�ĸ���
							if(count <= 0) {
								isOK[0] = true;
								return;
							}
							int first = mAdapterView.getFirstVisiblePosition() - mHeaderCount;
							int last = mAdapterView.getLastVisiblePosition() - mHeaderCount;
							if(first >= adapterCount){
								isOK[0] = true;
								return;
							}
							if(last >= adapterCount) last = adapterCount - 1;
							allFirstAndLast[1] = first;
							allFirstAndLast[2] = last;
							first = first - mExtraCountForExecutingData;
							if(first < 0) first = 0;
							last = last + mExtraCountForExecutingData;
							if(last < 0 || last >= adapterCount) last = adapterCount - 1;
							for(int i = first;i <= last;i++){
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
					//����Χ��DataHolder���첽�����޸�Ϊ�����ã�����GC����
					Set<Integer> resolvedPositionSet = mResolvedHolders.keySet();
					Integer[] resolvedPositions = resolvedPositionSet.toArray(new Integer[resolvedPositionSet.size()]);
					if(allFirstAndLast[1] == -1 || allFirstAndLast[2] == -1){
						for(int i = 0;i < resolvedPositions.length;i++){
							int position = resolvedPositions[i];
							DataHolder holder = mResolvedHolders.get(position);
							for(int j = 0;j < holder.getAsyncDataCount();j++){
								holder.changeAsyncDataToSoftReference(j);
							}
							mResolvedHolders.remove(position);
						}
					}else{
						int first = allFirstAndLast[1]-mExtraCountForKeepingData;
						int last = allFirstAndLast[2]+mExtraCountForKeepingData;
						if(last < 0 || last >= allFirstAndLast[0]) last = allFirstAndLast[0] - 1;
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
					}
					synchronized(mLockStop){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					//�����»�ȡ���Ķ���
					for(int i = 0;i < positions.size();i++){
						int position = positions.get(i);
						DataHolder holder = holders.get(i);
						boolean isAllAsyncDataCompleted = true;
						for(int j = 0;j < holder.getAsyncDataCount();j++){
							Object asyncData = holder.getAsyncData(j);
							if(asyncData == null) {
								isAllAsyncDataCompleted = false;
							}else {
								holder.setAsyncData(j, asyncData);    //�����������ã���������Ϊǿ����
								mResolvedHolders.put(position, holder);
							}
						}
						if(isAllAsyncDataCompleted){
							positions.remove(i);
							holders.remove(i);
							i--;
						}
					}
					//���¶����滻��ȡ����
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
					if(size == 0 || mExtractedIndex == size) continue;    //�����ǰ��ȡ����û�������µ�����������������̣߳��Խ�Լ��Դ
					synchronized(mLockStop){
						if(mIsStopping){
							mIsStopped = true;
							return;
						}
					}
					//��������̵߳ĸ��������������߳�
					int needThreadCount;
					int dataHolderCount = mExecutor.getDataHolderCount();
					int asyncDataCount = mExecutor.getAsyncDataCount();
					if(dataHolderCount == -1){
						needThreadCount = 1;
					}else if(dataHolderCount == 1 && asyncDataCount != -1){    //ÿ��ִֻ��һ��DataHolderʱ����������̸߳�����Ҫ����ÿ��DataHolder���첽���ݸ���
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
							if(curThreadCount == 0) curThreadCount = 1;    //ÿ��DataHolder������һ���߳���ִ���첽���ݼ���
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
									List<Integer> positions = null;    //��һ������
									List<DataHolder> dataHolders = null;    //�ڶ�������
									List<Integer> asyncDataIndexes = null;    //����������
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
											if(dataHolderCount == 1){    //ÿ��ִֻ��һ��DataHolderʱ��Ҫ��ʼ������������
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
									//ִ�м����߼�
									try{
										mExecutor.onExecute(positions, dataHolders, asyncDataIndexes);
									}catch(Exception e){
										int from = positions.get(0);
										int to = positions.get(positions.size() - 1);
										LogManager.logE(AsyncDataScheduler.class, "execute async data failed from position "+from+" to "+to+".", e);
									}
									//ɸѡ�����ع���DataHolder
									final Map<Integer, DataHolder> curResolvedHolders = new HashMap<Integer, DataHolder>();
									if(asyncDataIndexes == null){
										for(int i = 0;i < positions.size();i++){
											int position = positions.get(i);
											DataHolder holder = dataHolders.get(i);
											for(int j = 0;j < holder.getAsyncDataCount();j++){
												if(holder.getAsyncData(j) != null){
													curResolvedHolders.put(position, holder);
													break;
												}
											}
										}
									}else{
										int position = positions.get(0);
										DataHolder holder = dataHolders.get(0);
										for(int i = 0;i < asyncDataIndexes.size();i++){
											if(holder.getAsyncData(asyncDataIndexes.get(i)) != null){
												curResolvedHolders.put(position, holder);
												break;
											}
										}
									}
									//��Ӽ��ع���DataHolder��mResolvedHolders�����½���
									if(curResolvedHolders.size() > 0){
										mResolvedHolders.putAll(curResolvedHolders);
										mHandler.post(new Runnable() {
											@Override
											public void run() {
												// TODO Auto-generated method stub
												//�����ȡ��С��Χ�ĸ��²��ԣ�ͨ��notifyDataSetChanged���»�Ӱ��Ч��
												int count = mAdapterView.getChildCount();    //������header��footer�ĸ���
												if(count <= 0) return;
												int first = mAdapterView.getFirstVisiblePosition() - mHeaderCount;
												int last = mAdapterView.getLastVisiblePosition() - mHeaderCount;
												int end;
												if(mGenericAdapter.isConvertView()) end = count - 1 + first;
												else end = count - 1;
												if(first > end) return;
												if(last > end) last = end;
												Iterator<Integer> curPositions = curResolvedHolders.keySet().iterator();
												while(curPositions.hasNext()){
													int position = curPositions.next();
													if(position >= first && position <= last){
														DataHolder dholder = curResolvedHolders.get(position);
														int convertPosition;
														if(mGenericAdapter.isConvertView()) convertPosition = position - first;
														else convertPosition = position;
														//getChildAt������header��footer������
														dholder.onUpdateView(mAdapterView.getContext(), position, mAdapterView.getChildAt(convertPosition), dholder.getData());
													}
												}
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
						throw new RuntimeException(e);
					}
				}
			};
		});
	}
	
	/**
	 * <p>ֹͣ��ǰ������
	 */
	public void stop(){
		mIsStopping = true;
	}
	
}
