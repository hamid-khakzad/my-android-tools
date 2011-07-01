package cn.emagsoftware.ui.adapter;

import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	protected boolean mIsAsyncDataCompleted = false;
	
	public DataHolder(Object data){
		mData = data;
	}
	
	/**
	 * <p>ʹ�õ�ǰdata����Viewʱ����
	 * <p>����ͨ��ViewHolder����View�Ľṹ��Ϣ���Ӷ���߸���ʱ��Ч��
	 * @param position
	 * @param data
	 * @return
	 */
	public abstract View onCreateView(int position,Object data);
	
	/**
	 * <p>ʹ�õ�ǰdata����Viewʱ���������ڽ�Լ��Դ�Ŀ��ǣ�View�ᱻ���ã���ʱֻ��Ҫ����View����
	 * <p>����Viewʱ��ͨ��isAsyncDataCompleted()�������ж��첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View��
	 * <p>���첽���ݼ������ʱ�����жϵ�ǰdata�Ƿ�����ʾλ�ã���������ʾλ�ã�Ҳ�ᵥ���ص���ǰ����������View
	 * <p>����View��������ViewHolder�����Ч��
	 * @param position
	 * @param view
	 * @param data
	 */
	public abstract void onUpdateView(int position,View view,Object data);
	
	public Object getData(){
		return mData;
	}
	
	public boolean isAsyncDataCompleted(){
		return mIsAsyncDataCompleted;
	}
	
	public void setAsyncDataCompleted(boolean isAsyncDataCompleted){
		mIsAsyncDataCompleted = isAsyncDataCompleted;
	}
	
}
