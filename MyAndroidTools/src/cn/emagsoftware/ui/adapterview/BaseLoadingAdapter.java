package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.ui.UIThread;
import cn.emagsoftware.util.LogManager;

import android.content.Context;

public abstract class BaseLoadingAdapter extends GenericAdapter{
	
	/**�Ƿ����ڼ���*/
	protected boolean mIsLoading = false;
	/**�Ƿ��Ѿ����ع�*/
	protected boolean mIsLoaded = false;
	/**��ǰ�ļ����Ƿ������쳣*/
	protected boolean mIsException = false;
	/**��ǰ�ļ�������*/
	protected Object mCurCondition = null;
	
	public BaseLoadingAdapter(Context context){
		super(context);
	}
	
	/**
	 * <p>���ص�ִ�з���
	 * @param condition ����ʱ��Ҫ��������û��ʱ�ɴ�null
	 * @return true��ʾ��ʼ���أ�false��ʾ�Ѿ��ڼ��أ����εĵ�����Ч
	 */
	public boolean load(final Object condition){
		if(mIsLoading) return false;
		mIsLoading = true;
		mCurCondition = condition;
		onBeginLoad(mContext,condition);    //��load�е��ö�������UIThread��onBeginUI�У���ʹUI�߳��ν�һ�£���������ⲿ�Ĳ�ͬ�����
		ThreadPoolManager.executeThread(new UIThread(mContext){
			@Override
			protected Object onRunNoUI(Context context) throws Exception {
				// TODO Auto-generated method stub
				super.onRunNoUI(context);
				return onLoad(context,condition);
			}
			@SuppressWarnings("unchecked")
			@Override
			protected void onSuccessUI(Context context,Object result) {
				// TODO Auto-generated method stub
				super.onSuccessUI(context,result);
				List<DataHolder> resultList = (List<DataHolder>)result;
				if(resultList != null && resultList.size() > 0) addDataHolders(resultList);    //�÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
				mIsLoading = false;
				mIsLoaded = true;
				mIsException = false;
				onAfterLoad(context,condition,null);
			}
			@Override
			protected void onExceptionUI(Context context,Exception e) {
				// TODO Auto-generated method stub
				super.onExceptionUI(context,e);
				LogManager.logE(BaseLoadingAdapter.class, "Execute loading failed.", e);
				mIsLoading = false;
				mIsException = true;
				onAfterLoad(context,condition,e);
			}
		});
		return true;
	}
	
	/**
	 * <p>��ȡ��ǰ�ļ�������
	 * @return
	 */
	public Object getCurCondition(){
		return mCurCondition;
	}
	
	/**
	 * <p>�Ƿ����ڼ���
	 * @return
	 */
	public boolean isLoading(){
		return mIsLoading;
	}
	
	/**
	 * <p>�Ƿ��Ѿ����ع�
	 * @return
	 */
	public boolean isLoaded(){
		return mIsLoaded;
	}
	
	/**
	 * <p>��ǰ�ļ����Ƿ������쳣
	 * @return
	 */
	public boolean isException(){
		return mIsException;
	}
	
	/**
	 * <p>�ڼ���֮ǰ�Ļص�������������ʾһЩloading֮��������������ListView������ͨ��addFooterView�������һ�����ڼ��ص���ʾ
	 * @param context
	 * @param condition
	 */
	public abstract void onBeginLoad(Context context,Object condition);
	
	/**
	 * <p>���صľ���ʵ�֣��÷������ڷ�UI�߳���ִ�У�Ҫע�ⲻ��ִ��UI�Ĳ���
	 * @param context
	 * @param condition
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(Context context,Object condition) throws Exception;
	
	/**
	 * <p>������ɺ�Ļص�����������ͨ���ж�exception�Ƿ�Ϊnull����Ϥ���سɹ���񣬴Ӷ����û�һЩ��ʾ
	 * @param context
	 * @param condition
	 * @param exception
	 */
	public abstract void onAfterLoad(Context context,Object condition,Exception exception);
	
}
