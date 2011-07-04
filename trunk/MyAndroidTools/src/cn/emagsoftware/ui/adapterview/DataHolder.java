package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	protected boolean mIsAsyncDataCompleted = false;
	
	public DataHolder(Object data){
		mData = data;
	}
	
	/**
	 * <p>ʹ�õ�ǰdata����Viewʱ����
	 * <p>����Viewʱ��ͨ��isAsyncDataCompleted()�������ж��첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View��
	 * <p>����ͨ��ViewHolder����View�Ľṹ��Ϣ���Ӷ���߸���ʱ��Ч��
	 * @param context
	 * @param position
	 * @param data
	 * @return
	 */
	public abstract View onCreateView(Context context,int position,Object data);
	
	/**
	 * <p>ʹ�õ�ǰdata����Viewʱ���������ڽ�Լ��Դ�Ŀ��ǣ�View�ᱻ���ã���ʱֻ��Ҫ����View����
	 * <p>����Viewʱ��ͨ��isAsyncDataCompleted()�������ж��첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View��
	 * <p>���첽���ݼ������ʱ�����жϵ�ǰdata�Ƿ�����ʾλ�ã���������ʾλ�ã�Ҳ�ᵥ���ص���ǰ����������View
	 * <p>����View��������ViewHolder�����Ч��
	 * @param context
	 * @param position
	 * @param view
	 * @param data
	 */
	public abstract void onUpdateView(Context context,int position,View view,Object data);
	
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