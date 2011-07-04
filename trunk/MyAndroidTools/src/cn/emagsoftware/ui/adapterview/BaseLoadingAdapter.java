package cn.emagsoftware.ui.adapterview;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseLoadingAdapter extends GenericAdapter{
	
	/**ִ���첽�������*/
	protected AsyncTask<Object, Integer, Object> mTask = null;
	/**�Ƿ����ڼ���*/
	protected boolean mIsLoading = false;
	
	public BaseLoadingAdapter(final Context context){
		super(context);
		mTask = new AsyncTask<Object, Integer, Object>(){
			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				onBeginLoad(context);
			}
			@Override
			protected Object doInBackground(Object... params) {
				// TODO Auto-generated method stub
				try{
					return onLoad(context);
				}catch(Exception e){
					Log.e("BaseLoadingAdapter", "Execute loading failed.", e);
					return e;
				}
			}
			@Override
			protected void onPostExecute(Object result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				mIsLoading = false;
				if(result == null){
					onAfterLoad(context,null);
				}else if(result instanceof List<?>){
					onAfterLoad(context,null);
				}else if(result instanceof Exception){
					onAfterLoad(context,(Exception)result);
				}
			}
		};
	}
	
	/**
	 * <p>���ص�ִ�з���
	 * @return true��ʾ��ʼ���أ�false��ʾ�Ѿ��ڼ��أ����εĵ�����Ч
	 */
	public boolean load(){
		if(mIsLoading) return false;
		mIsLoading = true;
		mTask.execute("");
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
