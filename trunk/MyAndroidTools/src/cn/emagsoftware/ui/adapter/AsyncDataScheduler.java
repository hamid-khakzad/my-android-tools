package cn.emagsoftware.ui.adapter;

import java.util.List;

public class AsyncDataScheduler extends Thread {
	
	protected int mMaxThreadCount;
	protected AsyncDataExecutor mExecutor;
	protected boolean mIsStarted = false;
	
	protected int mFirstPosition;
	protected int mLastPosition;
	protected List<DataHolder> mQueue = null;
	
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
		
		
		
		
		
		
		
	}
	
	public void updateQueue(int firstPosition,int lastPosition,List<DataHolder> queue){
		mFirstPosition = firstPosition;
		mLastPosition = lastPosition;
		mQueue = queue;
	}
	
}
