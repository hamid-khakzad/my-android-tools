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
	 * <p>ʹ�õ�ǰdata����Viewʱ���������ڽ�Լ��Դ�Ŀ��ǣ�ViewĬ�ϻᱻ���ã���ʱֻ��Ҫ����View����
	 * <p>����Viewʱ��ͨ��isAsyncDataCompleted()�������ж��첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View��
	 * <p>��������View�����ã�����ǰ������Ȼ�п��ܱ��ص���������£�
	 *    <br>1.���첽���ݼ������ʱ�����жϵ�ǰdata�Ƿ��ڱ���AdapterView�Ŀɼ�λ�ã������ڿɼ�λ�ã���ص���ǰ����������View
	 *    <br>2.����������첽���ݵ�����ˢ�¿ɼ�����(setRefreshVisibleArea)�����ص���ǰ������ˢ��View
	 * <p>����View����ͨ��ViewHolder�����Ч��
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
