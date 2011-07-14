package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.ui.UIThread;

import android.content.Context;
import android.util.Log;

public abstract class BaseLoadingAdapter extends GenericAdapter{
	
	/**�Ƿ����ڼ���*/
	protected boolean mIsLoading = false;
	/**�Ƿ��Ѿ��������*/
	protected boolean mIsLoaded = false;
	
	public BaseLoadingAdapter(Context context){
		super(context);
	}
	
	/**
	 * <p>���ص�ִ�з���
	 * @return true��ʾ��ʼ���أ�false��ʾ�Ѿ��ڼ��أ����εĵ�����Ч
	 */
	public boolean load(){
		if(mIsLoading) return false;
		mIsLoading = true;
		new UIThread(mContext,new UIThread.Callback(){
			@Override
			public void onBeginUI(Context context) {
				// TODO Auto-generated method stub
				super.onBeginUI(context);
				onBeginLoad(context);
			}
			@Override
			public Object onRunNoUI(Context context) throws Exception {
				// TODO Auto-generated method stub
				super.onRunNoUI(context);
				return onLoad(context);
			}
			@Override
			public void onSuccessUI(Context context, Object result) {
				// TODO Auto-generated method stub
				super.onSuccessUI(context, result);
				if(result == null){
					mIsLoading = false;
					mIsLoaded = true;
					onAfterLoad(context,null);
				}else{
					addDataHolders((List<DataHolder>)result);    //�÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
					mIsLoading = false;
					mIsLoaded = true;
					onAfterLoad(context,null);
				}
			}
			@Override
			public void onExceptionUI(Context context, Exception e) {
				// TODO Auto-generated method stub
				super.onExceptionUI(context, e);
				Log.e("BaseLoadingAdapter", "Execute loading failed.", e);
				mIsLoading = false;
				onAfterLoad(context,e);
			}
		}).start();
		return true;
	}
	
	/**
	 * <p>�Ƿ����ڼ���
	 * @return
	 */
	public boolean isLoading(){
		return mIsLoading;
	}
	
	/**
	 * <p>�Ƿ��Ѿ��������
	 * @return
	 */
	public boolean isLoaded(){
		return mIsLoaded;
	}
	
	/**
	 * <p>�ڼ���֮ǰ�Ļص�������������ʾһЩloading֮��������������ListView������ͨ��addFooterView�������һ�����ڼ��ص���ʾ
	 * @param context
	 */
	public abstract void onBeginLoad(Context context);
	
	/**
	 * <p>���صľ���ʵ�֣��÷������ڷ�UI�߳���ִ�У�Ҫע�ⲻ��ִ��UI�Ĳ���
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(Context context) throws Exception;
	
	/**
	 * <p>������ɺ�Ļص�����������ͨ���ж�exception�Ƿ�Ϊnull����Ϥ���سɹ���񣬴Ӷ����û�һЩ��ʾ
	 * @param context
	 * @param exception
	 */
	public abstract void onAfterLoad(Context context,Exception exception);
	
}
