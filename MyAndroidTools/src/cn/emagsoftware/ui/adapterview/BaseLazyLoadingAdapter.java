package cn.emagsoftware.ui.adapterview;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseLazyLoadingAdapter extends BaseLoadingAdapter {
	
	/**��ǰ���ص������*/
	protected int mStart = 0;
	/**ÿ�μ��صĳ���*/
	protected int mLimit = 10;
	
	public BaseLazyLoadingAdapter(final Context context,int limit){
		super(context);
		if(limit <= 0) throw new IllegalArgumentException("limit should be great than zero.");
		mLimit = limit;
		super.mTask = new AsyncTask<Object, Integer, Object>(){
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
					return onLoad(mStart,mLimit);
				}catch(Exception e){
					Log.e("BaseLazyLoadingAdapter", "Execute lazy loading failed.", e);
					return e;
				}
			}
			@Override
			protected void onPostExecute(Object result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
				if(result == null){
					mIsLoading = false;
					onAfterLoad(context,null);
				}else if(result instanceof List<?>){
					List<DataHolder> resultList = (List<DataHolder>)result;
					addDataHolders(resultList);
					mStart = mStart + resultList.size();
					mIsLoading = false;
					onAfterLoad(context,null);
				}else if(result instanceof Exception){
					mIsLoading = false;
					onAfterLoad(context,(Exception)result);
				}
			}
		};
	}
	
	/**
	 * <p>�����˸����ͬ�������������õ�ǰ���һЩ����
	 */
	@Override
	public void clearDataHolders() {
		// TODO Auto-generated method stub
		super.clearDataHolders();
		mStart = 0;
	}
	
	/**
	 * <p>���ڵ�ǰ����ԣ���ʹ��onLoad(int start,int limit)�滻�˵�ǰ���������ã��ʽ����ʵ���Է�ֹ���౻ǿ��Ҫ��ʵ��
	 */
	@Override
	public List<DataHolder> onLoad() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * <p>���صľ���ʵ�֣�ͨ������Ĳ�������ʵ�ֶַε������ء��÷����ɷ�UI�̻߳ص������Կ���ִ�к�ʱ����
	 * @param start ���μ��صĿ�ʼ���
	 * @param limit ���μ��صĳ���
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(int start,int limit) throws Exception;
	
}
