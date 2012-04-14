package cn.emagsoftware.ui.adapterview;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.WrapperListAdapter;
import cn.emagsoftware.util.LogManager;

public abstract class AsyncDataExecutor {
	
	private static PushThread PUSH_THREAD = new PushThread();
	static{
		ThreadPoolManager.executeThread(PUSH_THREAD);
	}
	
	private int mMaxThreadCount = 5;
	private int mMaxWaitCount = 20;
	
	private AdapterView<?> mAdapterView = null;
	
	private LinkedList<Integer> mPushedPositions = new LinkedList<Integer>();
	private LinkedList<DataHolder> mPushedHolders = new LinkedList<DataHolder>();
	private int mCurExecuteIndex = 0;
	private byte[] mLockExecute = new byte[0];
	private Set<Thread> mCurExecuteThreads = new HashSet<Thread>();
	private Handler mHandler = new Handler(Looper.getMainLooper());
	
	public AsyncDataExecutor(int maxThreadCount,int maxWaitCount){
		if(maxThreadCount <= 0) throw new IllegalArgumentException("maxThreadCount should be great than zero.");
		if(maxWaitCount <= 0) throw new IllegalArgumentException("maxWaitCount should be great than zero.");
		this.mMaxThreadCount = maxThreadCount;
		this.mMaxWaitCount = maxWaitCount;
	}
	
	public void bindViewForRefresh(AdapterView<?> adapterView){
		this.mAdapterView = adapterView;
	}
	
	public void pushAsync(int position,DataHolder dataHolder){
		if(!PUSH_THREAD.execPushingAsync(this,position,dataHolder)) push(position,dataHolder);    //异步执行不满足条件时，将在当前线程执行，这种情况只会在一开始调用时偶发
	}
	
	private void push(int position,DataHolder dataHolder){
		Thread executeThread = null;
		synchronized(mLockExecute){
			Iterator<Integer> positionIterator = mPushedPositions.iterator();
			Iterator<DataHolder> dataHolderIterator = mPushedHolders.iterator();
			int index = -1;
			boolean isRemoved = false;
			while(positionIterator.hasNext()){
				int curPosition = positionIterator.next();
				if(++index < mCurExecuteIndex){
					DataHolder curDataHolder = dataHolderIterator.next();
					if(position == curPosition && dataHolder.equals(curDataHolder)) return;
				}else{
					if(position == curPosition){
						positionIterator.remove();
						dataHolderIterator.remove();
						isRemoved = true;
						break;
					}
				}
			}
			mPushedPositions.add(mCurExecuteIndex, position);
			mPushedHolders.add(mCurExecuteIndex, dataHolder);
			if(!isRemoved){
				if(mPushedPositions.size() - mCurExecuteIndex > mMaxWaitCount){
					mPushedPositions.removeLast();
					mPushedHolders.removeLast();
				}
			}
			if(mCurExecuteThreads.size() < mMaxThreadCount){
				executeThread = createExecuteThread(position,dataHolder);
				mCurExecuteThreads.add(executeThread);
				mCurExecuteIndex++;
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
					//界面重绘可能会重复使用到异步数据，但DataHolder在执行过程中却不允许重复执行，所以执行时要遍历检查所有的异步项，并且先前已执行完的要升级为强引用
					//当前线程会在DataHolder全部执行完且移出队列后统一将异步数据置为软引用
					for(int i = 0;i < curHolder.getAsyncDataCount();i++){
						Object curAsyncData = curHolder.getAsyncData(i);
						if(curAsyncData == null){
							try{
								final Object asyncData = onExecute(curPosition,curHolder,i);
								if(asyncData == null) throw new NullPointerException("the method 'onExecute' returns null");
								curHolder.setAsyncData(i, asyncData);
								//更新界面
								final int curPositionCopy = curPosition;
								final DataHolder curHolderPoint = curHolder;
								final int iCopy = i;
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										//这里采取最小范围的更新策略，通过notifyDataSetChanged更新会影响效率
										AdapterView<?> adapterViewPoint = mAdapterView;
										if(adapterViewPoint == null) return;
										Adapter adapter = adapterViewPoint.getAdapter();
										if(adapter == null) return;
										if(adapter instanceof WrapperListAdapter) adapter = ((WrapperListAdapter)adapter).getWrappedAdapter();
										if(!(adapter instanceof GenericAdapter)) return;
										GenericAdapter genericAdapter = (GenericAdapter)adapter;    //需动态获取Adapter，以保证数据和UI的一致性
										if(curPositionCopy >= genericAdapter.getCount()) return;    //界面发生了改变
										if(!curHolderPoint.equals(genericAdapter.queryDataHolder(curPositionCopy))) return;    //界面发生了改变
										int count = adapterViewPoint.getChildCount();    //不包含header和footer的个数
										if(count <= 0) return;
										int headerCount = 0;
										if(adapterViewPoint instanceof ListView) headerCount = ((ListView)adapterViewPoint).getHeaderViewsCount();
										int first = adapterViewPoint.getFirstVisiblePosition() - headerCount;
										int last = adapterViewPoint.getLastVisiblePosition() - headerCount;
										int end = count - 1 + first;
				                        if (first > end) return;
				                        if (last > end) last = end;
										if(curPositionCopy >= first && curPositionCopy <= last){
											int convertPosition = curPositionCopy;
											if(genericAdapter.isConvertView()) convertPosition = curPositionCopy - first;
											//getChildAt不包含header和footer的索引
											curHolderPoint.onAsyncDataExecuted(adapterViewPoint.getContext(), curPositionCopy, adapterViewPoint.getChildAt(convertPosition), asyncData, iCopy);
										}
									}
								});
							}catch(Exception e){
								LogManager.logE(AsyncDataExecutor.class, "execute async data failed(position:"+curPosition+",index:"+i+")", e);
							}
						}else{
							curHolder.setAsyncData(i, curAsyncData);
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
					executor.push(position,dataHolder);
				}
			});
			return true;
		}
	}
	
}
