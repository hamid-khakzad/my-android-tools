package cn.emagsoftware.ui.adapter;

import java.util.List;

public abstract class AsyncDataExecutor {
	
	/**ÿ��ȡ�õ�Ҫ�����DataHolder�ĸ���*/
	protected int mEachCount;
	
	public AsyncDataExecutor(int eachCount){
		if(eachCount <= 0) throw new IllegalArgumentException("eachCount should be great than zero.");
		mEachCount = eachCount;
	}
	
	public int getEachCount(){
		return mEachCount;
	}
	
	/**
	 * <p>�����첽���ݵĻص�������ע�⣬�÷������ܻ��ڶ��̵߳Ļ�����ִ�У�����Ҫ��֤�����ڲ����̰߳�ȫ��
	 * <p>���׳��κ��쳣���׳��쳣ʱ���ⲿ����Ϊ��ǰ�첽���ݵļ�����ʧ�ܸ���
	 * @param firstPosition
	 * @param lastPosition
	 * @param holders
	 * @throws Exception
	 */
	public abstract void onExecute(int firstPosition,int lastPosition,List<DataHolder> holders) throws Exception;
	
}
