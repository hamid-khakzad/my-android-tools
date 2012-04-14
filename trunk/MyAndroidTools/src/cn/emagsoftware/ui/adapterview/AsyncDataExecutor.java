package cn.emagsoftware.ui.adapterview;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
	
	private List<Integer> mPushedPositions = new LinkedList<Integer>();
	private List<DataHolder> mPushedHolders = new LinkedList<DataHolder>();
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
			int index = mPushedHolders.indexOf(dataHolder);
			if(index == -1){
				mPushedPositions.add(mCurExecuteIndex, position);
				mPushedHolders.add(mCurExecuteIndex, dataHolder);
				int size = mPushedPositions.size();
				if(size - mCurExecuteIndex > mMaxWaitCount){
					mPushedPositions.remove(size - 1);
					mPushedHolders.remove(size - 1);
				}
				if(mCurExecuteThreads.size() < mMaxThreadCount){
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
				if(mCurExecuteThreads.size() < mMaxThreadCount){
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
								final int iCopy = i;
								final DataHolder curHolderPoint = curHolder;
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
										int count = adapterViewPoint.getChildCount();    //不包含header和footer的个数
										if(count <= 0) return;
										int headerCount = 0;
										if(adapterViewPoint instanceof ListView) headerCount = ((ListView)adapterViewPoint).getHeaderViewsCount();
										int first = adapterViewPoint.getFirstVisiblePosition() - headerCount;
										int last = adapterViewPoint.getLastVisiblePosition() - headerCount;
										int nowPosition = -1;
										int size = genericAdapter.getCount();
										for(int i = first;i <= last;i++){    //只循环可见范围以防止过长占用UI线程
											if(i >= size) break;
											if(curHolderPoint.equals(genericAdapter.queryDataHolder(i))){
												nowPosition = i;
												break;
											}
										}
										if(nowPosition != -1){    //当前DataHolder的最新位置仍在可见范围内
											int convertPosition = nowPosition;
											if(genericAdapter.isConvertView()) convertPosition = nowPosition - first;
											//getChildAt不包含header和footer的索引
											curHolderPoint.onAsyncDataExecuted(adapterViewPoint.getContext(), nowPosition, adapterViewPoint.getChildAt(convertPosition), asyncData, iCopy);
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
