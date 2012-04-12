package cn.emagsoftware.ui.adapterview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.emagsoftware.util.LogManager;

import android.os.Handler;
import android.os.Looper;
import android.widget.AdapterView;
import android.widget.ListView;

public abstract class AsyncDataExecutor {
	
	private static PushThread PUSH_THREAD = new PushThread();
	static{
		ThreadPoolManager.executeThread(PUSH_THREAD);
	}
	
	private int mMaxThreadCount = 5;
	private int mMaxPushedCount = 20;
	
	private AdapterView<?> mAdapterView = null;
	private GenericAdapter mGenericAdapter = null;
	
	private List<Integer> mPushedPositions = new LinkedList<Integer>();
	private List<DataHolder> mPushedHolders = new LinkedList<DataHolder>();
	private int mCurExecuteIndex = 0;
	private byte[] mLockExecute = new byte[0];
	private Set<Thread> mCurExecuteThreads = new HashSet<Thread>();
	private Handler mHandler = new Handler(Looper.getMainLooper());
	
	public AsyncDataExecutor(int maxThreadCount,int maxPushedCount){
		if(maxThreadCount <= 0) throw new IllegalArgumentException("maxThreadCount should be great than zero.");
		if(maxPushedCount <= 0) throw new IllegalArgumentException("maxPushedCount should be great than zero.");
		this.mMaxThreadCount = maxThreadCount;
		this.mMaxPushedCount = maxPushedCount;
	}
	
	public void bindForRefresh(AdapterView<?> adapterView,GenericAdapter genericAdapter){
		if(adapterView == null || genericAdapter == null) throw new NullPointerException();
		this.mAdapterView = adapterView;
		this.mGenericAdapter = genericAdapter;
	}
	
	public void pushAsync(int position,DataHolder dataHolder){
		if(!PUSH_THREAD.execPushingAsync(this, position, dataHolder)) push(position,dataHolder);    //异步执行不满足条件时，将在当前线程执行，这种情况只会在一开始调用时偶发
	}
	
	private void push(int position,DataHolder dataHolder){
		int asyncDataCount = dataHolder.getAsyncDataCount();
		List<Integer> asyncDataIndexes = new ArrayList<Integer>(asyncDataCount);
		for(int i = 0;i < asyncDataCount;i++){
			if(dataHolder.getAsyncData(i) == null) asyncDataIndexes.add(i);
		}
		
		
		
		
		
		Thread executeThread = null;
		synchronized(mLockExecute){
			int index = mPushedHolders.indexOf(dataHolder);
			if(index == -1){
				mPushedPositions.add(mCurExecuteIndex, position);
				mPushedHolders.add(mCurExecuteIndex, dataHolder);
				int size = mPushedPositions.size();
				if(size > mMaxPushedCount){
					mPushedPositions.remove(size - 1);
					mPushedHolders.remove(size - 1);
				}
				if(asyncDataIndexes.size() == 0) mCurExecuteIndex++;
				else if(mCurExecuteThreads.size() < mMaxThreadCount){
					executeThread = createExecuteThread(position,dataHolder);
					mCurExecuteThreads.add(executeThread);
					mCurExecuteIndex++;
				}
			}else if(index >= mCurExecuteIndex){    //还未执行到
				mPushedPositions.remove(index);
				mPushedPositions.add(mCurExecuteIndex, position);
				if(index != mCurExecuteIndex){
					mPushedHolders.remove(index);
					mPushedHolders.add(mCurExecuteIndex, dataHolder);
				}
				if(asyncDataIndexes.size() == 0) mCurExecuteIndex++;
				else if(mCurExecuteThreads.size() < mMaxThreadCount){
					executeThread = createExecuteThread(position,dataHolder);
					mCurExecuteThreads.add(executeThread);
					mCurExecuteIndex++;
				}
			}
		}
		if(executeThread != null) ThreadPoolManager.executeThread(executeThread);
	}
	
	private Thread createExecuteThread(final int firstPosition,final DataHolder firstDataHolder){
		return new Thread(){
			private int curPosition;
			private DataHolder curHolder;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while(true){
					if(curHolder == null){
						curPosition = firstPosition;
						curHolder = firstDataHolder;
					}else{
						synchronized(mLockExecute){
							mPushedPositions.remove((Integer)curPosition);
							mPushedHolders.remove(curHolder);
							if(mCurExecuteIndex > 0) mCurExecuteIndex--;
							for(int i = 0;i < curHolder.getAsyncDataCount();i++){
								curHolder.changeAsyncDataToSoftReference(i);
							}
							if(mCurExecuteIndex >= mPushedPositions.size()){
								mCurExecuteThreads.remove(this);
								return;
							}else{
								curPosition = mPushedPositions.get(mCurExecuteIndex);
								curHolder = mPushedHolders.get(mCurExecuteIndex);
								mCurExecuteIndex++;
							}
						}
					}
					for(int i = 0;i < curHolder.getAsyncDataCount();i++){
						if(curHolder.getAsyncData(i) == null){
							try{
								Object asyncData = onExecute(curPosition,curHolder,i);
								curHolder.setAsyncData(i, asyncData);
								//更新界面
								if(mAdapterView != null && mGenericAdapter != null){
									//判断完再执行UI操作可提高UI操作的效率
									if(mGenericAdapter.isConvertView()){
										mHandler.post(new Runnable() {
											@Override
											public void run() {
												// TODO Auto-generated method stub
												//这里采取最小范围的更新策略，通过notifyDataSetChanged更新会影响效率
												int count = mAdapterView.getChildCount();    //不包含header和footer的个数
												if(count <= 0) return;
												int headerCount = 0;
												if(mAdapterView instanceof ListView) headerCount = ((ListView)mAdapterView).getHeaderViewsCount();
												int first = mAdapterView.getFirstVisiblePosition() - headerCount;
												int last = mAdapterView.getLastVisiblePosition() - headerCount;
												int end = count - 1 + first;
												if(first > end) return;
												if(last > end) last = end;
												
												
												
												
												int position = curPositions.next();
												if(position >= first && position <= last){
													DataHolder dholder = curResolvedHolders.get(position);
													int convertPosition = position - first;
													//getChildAt不包含header和footer的索引
													dholder.onUpdateView(mAdapterView.getContext(), position, mAdapterView.getChildAt(convertPosition), dholder.getData(), true);
												}
											}
										});
									}else{
										mHandler.post(new Runnable() {
											@Override
											public void run() {
												// TODO Auto-generated method stub
												//这里采取最小范围的更新策略，通过notifyDataSetChanged更新会影响效率
												int count = mAdapterView.getChildCount();    //不包含header和footer的个数
												if(count <= 0) return;
												int first = mAdapterView.getFirstVisiblePosition() - mHeaderCount;
												int last = mAdapterView.getLastVisiblePosition() - mHeaderCount;
												int end = count - 1;
												if(first > end) return;
												if(last > end) last = end;
												
												int position = curPositions.next();
												if(position >= first && position <= last){
													DataHolder dholder = curResolvedHolders.get(position);
													int convertPosition = position;
													//getChildAt不包含header和footer的索引
													dholder.onUpdateView(mAdapterView.getContext(), position, mAdapterView.getChildAt(convertPosition), dholder.getData(), true);
												}
											}
										});
									}
								}
							}catch(Exception e){
								LogManager.logE(AsyncDataExecutor.class, "execute async data failed(position:"+curPosition+",index:"+i+")", e);
							}
						}
					}
				}
			}
		};
	}
	
	/**
	 * <p>加载异步数据的回调方法，注意，该方法可能会在多线程的环境中执行，所以要保证该方法是线程安全的
	 * <p>可抛出任何异常，抛出异常时，外部会认为当前的异步数据执行失败
	 * @param position 所在AdapterView中的位置
	 * @param dataHolder 用于AdapterView的DataHolder对象
	 * @param asyncDataIndex 需要加载的DataHolder中异步数据的索引
	 * @return 执行后得到的结果
	 * @throws Exception
	 */
	public abstract Object onExecute(int position,DataHolder dataHolder,int asyncDataIndex) throws Exception;
	
	private static class PushThread extends Thread{
		private Handler handler = null;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			Looper.prepare();
			handler = new Handler();
			Looper.loop();
		}
		public boolean execPushingAsync(final AsyncDataExecutor executor,final int position,final DataHolder dataHolder){
			if(handler == null) return false;
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					executor.push(position, dataHolder);
				}
			});
			return true;
		}
	}
	
}
