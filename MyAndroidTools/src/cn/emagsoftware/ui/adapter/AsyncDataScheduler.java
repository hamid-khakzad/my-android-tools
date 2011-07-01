package cn.emagsoftware.ui.adapter;

import java.util.List;

public class AsyncDataScheduler extends Thread {
	
	/**异步数据调度器的休眠时间，以毫秒为单位*/
	public static final int SCHEDULER_DORMANCY_TIME = 2000;
	
	protected int mMaxThreadCount;
	protected AsyncDataExecutor mExecutor;
	protected boolean mIsStarted = false;
	
	protected boolean mIsCancelMe = false;
	protected boolean mIsCancelThreads = false;
	
	protected int mFirstPosition;
	protected int mLastPosition;
	protected List<DataHolder> mQueue = null;
	protected List<DataHolder> mFilteredQueue = null;
	
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
			if(mQueue != null){
				List<DataHolder> tempQueue = mQueue;
				
				
				
				
			}
		}
	}
	
	public void updateQueue(int firstPosition,int lastPosition,List<DataHolder> queue){
		mFirstPosition = firstPosition;
		mLastPosition = lastPosition;
		mQueue = queue;
	}
	
	public void cancelMe(){
		mIsCancelMe = true;
		interrupt();
	}
	
	public void cancelThreads(){
		mIsCancelThreads = true;
	}
	
}
