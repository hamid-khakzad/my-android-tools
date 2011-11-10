package cn.emagsoftware.ui.adapterview;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.view.View;

public abstract class DataHolder {
	
	protected Object mData = null;
	protected Object[] mAsyncData = null;
	
	/**
	 * <p>���캯��
	 * @param data ��Ҫ�õ�������
	 * @param asyncDataCount ��Ҫ��ִ�е��첽���ݵĸ���
	 */
	public DataHolder(Object data,int asyncDataCount){
		mData = data;
		mAsyncData = new Object[asyncDataCount];
	}
	
	/**
	 * <p>ʹ�õ�ǰdata����Viewʱ����
	 * <p>��������첽���ݵļ��أ��ڴ���Viewʱ��ͨ��getAsyncData(int index)�Ƿ�Ϊnull���ж������첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View��
	 * <p>����ͨ��ViewHolder����View�Ľṹ��Ϣ���Ӷ���߸���ʱ��Ч��
	 * @param context
	 * @param position
	 * @param data
	 * @return
	 */
	public abstract View onCreateView(Context context,int position,Object data);
	
	/**
	 * <p>ʹ�õ�ǰdata����Viewʱ���������ڽ�Լ��Դ�Ŀ��ǣ�ViewĬ�ϻᱻ���ã���ʱֻ��Ҫ����View����
	 * <p>��������첽���ݵļ��أ��ڸ���Viewʱ��ͨ��getAsyncData(int index)�Ƿ�Ϊnull���ж������첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View��
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
	 * <p>��ȡָ��λ�õ��첽���ݣ�δ���ػ��ѱ�����ʱ����null
	 * @param index �첽���ݵ�λ��
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object getAsyncData(int index){
		SoftReference<Object> asyncDataRef = (SoftReference<Object>)mAsyncData[index];
		if(asyncDataRef == null) return null;
		return asyncDataRef.get();
	}
	
	/**
	 * <p>����ָ��λ�õ��첽����
	 * @param index �첽���ݵ�λ��
	 * @param asyncData
	 */
	public void setAsyncData(int index,Object asyncData){
		mAsyncData[index] = new SoftReference<Object>(asyncData);
	}
	
	/**
	 * <p>��ȡ�첽���ݵĸ���
	 * @return
	 */
	public int getAsyncDataCount(){
		return mAsyncData.length;
	}
	
}
