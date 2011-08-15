package cn.emagsoftware.ui.adapterview;

import java.util.List;

public abstract class AsyncDataExecutor {
	
	/**每次取得的要处理的DataHolder的个数，-1为取得所有的待处理个数*/
	protected int mDataHolderCount;
	/**每次取得的要处理的DataHolder中异步数据的个数，-1为取得当前DataHolder所有待处理的异步数据个数*/
	protected int mAsyncDataCount;
	
	/**
	 * <p>构造函数
	 * @param dataHolderCount 每次取得的要处理的DataHolder的个数，可传入-1来取得所有的待处理个数
	 * @param asyncDataCount 每次取得的要处理的DataHolder中异步数据的个数，可传入-1来取得当前DataHolder所有待处理的异步数据个数。
	 *                       <b>只有当dataHolderCount参数为1时，该参数的设置才会有效</b>
	 */
	public AsyncDataExecutor(int dataHolderCount,int asyncDataCount){
		if(dataHolderCount <= 0 && dataHolderCount != -1) throw new IllegalArgumentException("dataHolderCount should be great than zero or equal -1.");
		if(asyncDataCount <= 0 && asyncDataCount != -1) throw new IllegalArgumentException("asyncDataCount should be great than zero or equal -1.");
		mDataHolderCount = dataHolderCount;
		mAsyncDataCount = asyncDataCount;
	}
	
	public int getDataHolderCount(){
		return mDataHolderCount;
	}
	
	public int getAsyncDataCount(){
		return mAsyncDataCount;
	}
	
	/**
	 * <p>加载异步数据的回调方法，注意，该方法可能会在多线程的环境中执行，所以要保证该方法是线程安全的
	 * <p>可抛出任何异常，抛出异常时，外部会认为当前异步数据的加载以失败告终
	 * @param positions 所在AdapterView中的位置
	 * @param dataHolders 用于AdapterView的DataHolder对象
	 * @param asyncDataIndexes 需要加载的DataHolder中异步数据的索引，<b>只有当构造函数中dataHolderCount设置为1时才有效，否则该参数传入null</b>
	 * @throws Exception
	 */
	public abstract void onExecute(List<Integer> positions,List<DataHolder> dataHolders,List<Integer> asyncDataIndexes) throws Exception;
	
}
