package cn.emagsoftware.ui.adapter;

import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	
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
	 * <p>����View��������ViewHolder�����Ч��
	 * @param position
	 * @param view
	 * @param data
	 */
	public abstract void onUpdateView(int position,View view,Object data);
	
	/**
	 * <p>ָʾ�첽���ص������Ƿ��Ѿ���ɣ�����Դӵ�ǰdata������״̬���ж�
	 * <p>�����ǰdata����Ҫͨ���첽�����ض������ݣ���ֱ�ӷ���true
	 * @return
	 */
	public abstract boolean isAsyncCompleted();
	
	public Object getData(){
		return mData;
	}
	
}
