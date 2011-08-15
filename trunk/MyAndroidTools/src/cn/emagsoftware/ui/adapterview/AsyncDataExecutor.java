package cn.emagsoftware.ui.adapterview;

import java.util.List;

public abstract class AsyncDataExecutor {
	
	/**ÿ��ȡ�õ�Ҫ�����DataHolder�ĸ�����-1Ϊȡ�����еĴ��������*/
	protected int mDataHolderCount;
	/**ÿ��ȡ�õ�Ҫ�����DataHolder���첽���ݵĸ�����-1Ϊȡ�õ�ǰDataHolder���д�������첽���ݸ���*/
	protected int mAsyncDataCount;
	
	/**
	 * <p>���캯��
	 * @param dataHolderCount ÿ��ȡ�õ�Ҫ�����DataHolder�ĸ������ɴ���-1��ȡ�����еĴ��������
	 * @param asyncDataCount ÿ��ȡ�õ�Ҫ�����DataHolder���첽���ݵĸ������ɴ���-1��ȡ�õ�ǰDataHolder���д�������첽���ݸ�����
	 *                       <b>ֻ�е�dataHolderCount����Ϊ1ʱ���ò��������òŻ���Ч</b>
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
	 * <p>�����첽���ݵĻص�������ע�⣬�÷������ܻ��ڶ��̵߳Ļ�����ִ�У�����Ҫ��֤�÷������̰߳�ȫ��
	 * <p>���׳��κ��쳣���׳��쳣ʱ���ⲿ����Ϊ��ǰ�첽���ݵļ�����ʧ�ܸ���
	 * @param positions ����AdapterView�е�λ��
	 * @param dataHolders ����AdapterView��DataHolder����
	 * @param asyncDataIndexes ��Ҫ���ص�DataHolder���첽���ݵ�������<b>ֻ�е����캯����dataHolderCount����Ϊ1ʱ����Ч������ò�������null</b>
	 * @throws Exception
	 */
	public abstract void onExecute(List<Integer> positions,List<DataHolder> dataHolders,List<Integer> asyncDataIndexes) throws Exception;
	
}
