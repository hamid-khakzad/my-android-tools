package cn.emagsoftware.ui.adapter;

import java.util.List;

public abstract class AsyncDataExecutor {
	
	/**每次取得的要处理的DataHolder的个数*/
	protected int mEachCount;
	
	public AsyncDataExecutor(int eachCount){
		if(eachCount <= 0) throw new IllegalArgumentException("eachCount should be great than zero.");
		mEachCount = eachCount;
	}
	
	public int getEachCount(){
		return mEachCount;
	}
	
	/**
	 * <p>加载异步数据的回调方法，注意，该方法可能会在多线程的环境中执行，所以要保证方法内部是线程安全的
	 * <p>可抛出任何异常，抛出异常时，外部会认为当前异步数据的加载以失败告终
	 * @param firstPosition
	 * @param lastPosition
	 * @param holders
	 * @throws Exception
	 */
	public abstract void onExecute(int firstPosition,int lastPosition,List<DataHolder> holders) throws Exception;
	
}
