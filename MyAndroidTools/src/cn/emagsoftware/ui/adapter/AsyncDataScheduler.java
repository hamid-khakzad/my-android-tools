package cn.emagsoftware.ui.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class AsyncDataScheduler extends Thread {
	
	/**异步数据调度器的休眠时间，以毫秒为单位*/
	public static final int SCHEDULER_DORMANCY_TIME = 2000;
	
	protected int mMaxThreadCount;
	protected AsyncDataExecutor mExecutor;
	
	protected boolean mIsStarted = false;
	protected boolean mIsCancelMe = false;
	protected boolean mIsCancelThreads = false;
	
	protected Queue mOriginalQueue = null;
	protected Queue mExtractedQueue = null;
	protected List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>());
	
	public AsyncDataScheduler(int maxThreadCount,AsyncDataExecutor executor){
		if(maxThreadCount <= 0) throw new IllegalArgumentException("maxThreadCount should be great than zero.");
		if(executor == null) throw new NullPointerException();
		mMaxThreadCount = maxThreadCount;
		mExecutor = executor;
	}
	
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
		mIsStarted = true;
	}
	
	public boolean isStarted(){
		return mIsStarted;
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
			if(mOriginalQueue != null){
				Queue tempQueue = mOriginalQueue;    //提取当前原始队列的引用，防止其被改变
				if(tempQueue == mExtractedQueue) continue;    //当前的提取队列已经是最新队列时将直接返回
				tempQueue.mDataPositions = new LinkedList<Integer>();
				for(int i = tempQueue.mFirstPosition;i <= tempQueue.mLastPosition;i++){
					tempQueue.mDataPositions.add(i);
				}
				//删除已经加载过的项
				for(int i = 0;i < tempQueue.mDataPositions.size();i++){
					DataHolder holder = tempQueue.mDataHolders.get(i);
					if(holder.isAsyncDataCompleted()){
						tempQueue.mDataPositions.remove(i);
						tempQueue.mDataHolders.remove(i);
						i--;
					}
				}
				//用新队列替换提取队列
				if(mExtractedQueue == null || tempQueue.mDataPositions.size() == 0){
					mExtractedQueue = tempQueue;
				}else{
					synchronized(AsyncDataScheduler.this){
						for(int i = 0;i < mExtractedQueue.mCurrentIndex;i++){
							DataHolder extractedHolder = mExtractedQueue.mDataHolders.get(i);
							int index = tempQueue.mDataHolders.indexOf(extractedHolder);
							if(index != -1){
								tempQueue.mDataPositions.remove(index);
								tempQueue.mDataHolders.remove(index);
							}
						}
						mExtractedQueue = tempQueue;
					}
				}
				if(mExtractedQueue.mDataPositions.size() == 0) continue;
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
									int currIndex = mExtractedQueue.mCurrentIndex;
									int endIndex = currIndex + mExecutor.getEachCount();
									int size = mExtractedQueue.mDataPositions.size();
									if(endIndex > size) endIndex = size;
									if(currIndex < endIndex){
										positions = mExtractedQueue.mDataPositions.subList(currIndex, endIndex);
										holders = mExtractedQueue.mDataHolders.subList(currIndex, endIndex);
										mExtractedQueue.mCurrentIndex = endIndex;
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
									//置执行成功标志，如果在界面上显示的项，将回调onUpdateView方法
									for(int i = 0;i < positions.size();i++){
										DataHolder dholder = holders.get(i);
										dholder.setAsyncDataCompleted(true);
										//int pos = positions.get(i);
										
									}
								}catch(Exception e){
									int from = positions.get(0);
									int to = positions.get(positions.size() - 1);
									Log.e("AsyncDataScheduler", "execute loading async data failed from position "+from+" to "+to+".", e);
								}
							}
						}
					};
					threads.add(thread);
					thread.start();
				}
			}
		}
	}
	
	public void updateQueue(int firstPosition,int lastPosition,List<DataHolder> holders){
		Queue tempQueue = new Queue();
		tempQueue.mFirstPosition = firstPosition;
		tempQueue.mLastPosition = lastPosition;
		tempQueue.mDataHolders = holders;
		mOriginalQueue = tempQueue;
	}
	
	public void cancelMe(){
		mIsCancelMe = true;
		interrupt();
	}
	
	public void cancelThreads(){
		mIsCancelThreads = true;
	}
	
	private class Queue{
		int mCurrentIndex = 0;
		int mFirstPosition;
		int mLastPosition;
		List<Integer> mDataPositions = null;
		List<DataHolder> mDataHolders = null;
	}
	
}
