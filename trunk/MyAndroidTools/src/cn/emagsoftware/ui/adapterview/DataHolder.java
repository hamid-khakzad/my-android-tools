package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	protected boolean[] mIsAsyncDataCompleted = null;
	
	/**
	 * <p>���캯��
	 * @param data ��Ҫ�õ�������
	 * @param asyncDataCount ��Ҫ��ִ�е��첽���ݵĸ���
	 */
	public DataHolder(Object data,int asyncDataCount){
		mData = data;
		mIsAsyncDataCompleted = new boolean[asyncDataCount];
		for(int i = 0;i < mIsAsyncDataCompleted.length;i++){
			mIsAsyncDataCompleted[i] = false;
		}
	}
	
	/**
	 * <p>ʹ�õ�ǰdata����Viewʱ����
	 * <p>��������첽���ݵļ��أ��ڴ���Viewʱ��ͨ��isAsyncDataCompleted(int index)�������ж������첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View��
	 * <p>����ͨ��ViewHolder����View�Ľṹ��Ϣ���Ӷ���߸���ʱ��Ч��
	 * @param context
	 * @param position
	 * @param data
	 * @return
	 */
	public abstract View onCreateView(Context context,int position,Object data);
	
	/**
	 * <p>ʹ�õ�ǰdata����Viewʱ���������ڽ�Լ��Դ�Ŀ��ǣ�ViewĬ�ϻᱻ���ã���ʱֻ��Ҫ����View����
	 * <p>��������첽���ݵļ��أ��ڸ���Viewʱ��ͨ��isAsyncDataCompleted(int index)�������ж������첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View��
	 * <p>��ͨ��GenericAdapter��setConvertView����������View�����ã����Ա��ָ÷�����ʵ��Ϊ��
	 * <p>����View����ͨ��ViewHolder�����Ч��
	 * @param context
	 * @param position
	 * @param view
	 * @param data
	 */
	public abstract void onUpdateView(Context context,int position,View view,Object data);
	
	/**
	 * <p>��ȡ���캯���д�������ݶ���
	 * @return
	 */
	public Object getData(){
		return mData;
	}
	
	/**
	 * <p>��ȡָ�����첽�����Ƿ�������
	 * @param index �첽�������ڵ�λ��
	 * @return
	 */
	public boolean isAsyncDataCompleted(int index){
		return mIsAsyncDataCompleted[index];
	}
	
	/**
	 * <p>����ָ�����첽���ݵļ���������
	 * @param index �첽�������ڵ�λ��
	 * @param isCompleted �Ƿ��Ѽ������
	 */
	public void setAsyncDataCompleted(int index,boolean isCompleted){
		mIsAsyncDataCompleted[index] = isCompleted;
	}
	
}
