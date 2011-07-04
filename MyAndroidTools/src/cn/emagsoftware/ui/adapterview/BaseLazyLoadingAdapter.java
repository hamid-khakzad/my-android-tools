package cn.emagsoftware.ui.adapterview;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AdapterView;

public abstract class BaseLazyLoadingAdapter extends BaseLoadingAdapter {
	
	/**��ǰ���ص������*/
	protected int mStart = 0;
	/**ÿ�μ��صĳ���*/
	protected int mLimit = 10;
	/**�Ƿ��Ѿ�������ȫ������*/
	protected boolean mIsLoadedAll = false;
	
	public BaseLazyLoadingAdapter(Context context,int limit){
		super(context);
		if(limit <= 0) throw new IllegalArgumentException("limit should be great than zero.");
		mLimit = limit;
	}
	
	/**
	 * <p>��AdapterView��ʹ���Զ������ء��绬��ListView��������ʱ�ſ�ʼ�µļ���
	 * @param adapterView
	 */
	public void bindLazyLoading(AdapterView<?> adapterView){
		if(adapterView instanceof AbsListView){
			AbsListView absList = (AbsListView)adapterView;
			absList.setOnScrollListener(new AbsListView.OnScrollListener(){
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub
					if(firstVisibleItem + visibleItemCount == totalItemCount && !isLoadedAll()){
						load();
					}
				}
				@Override
				public void onScrollStateChanged(AbsListView view,int scrollState) {
					// TODO Auto-generated method stub
				}
			});
		}else{
			throw new UnsupportedOperationException("Only supports lazy loading for the AdapterView which is AbsListView.");
		}
	}
	
	/**
	 * <p>�����˸����ͬ������������ִ��������
	 */
	@Override
	public boolean load() {
		// TODO Auto-generated method stub
		if(mIsLoading) return false;
		mIsLoading = true;
		mTask = new AsyncTask<Object, Integer, Object>(){
			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				onBeginLoad(mContext);
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
					mIsLoadedAll = true;
					mIsLoading = false;
					onAfterLoad(mContext,null);
				}else if(result instanceof List<?>){
					List<DataHolder> resultList = (List<DataHolder>)result;
					addDataHolders(resultList);    //�÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
					int size = resultList.size();
					mStart = mStart + size;
					if(size == 0) mIsLoadedAll = true;
					else mIsLoadedAll = false;
					mIsLoading = false;
					onAfterLoad(mContext,null);
				}else if(result instanceof Exception){
					mIsLoading = false;
					onAfterLoad(mContext,(Exception)result);
				}
			}
		};
		mTask.execute("");
		return true;
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
	 * <p>�Ƿ��Ѿ�������ȫ������
	 * @return
	 */
	public boolean isLoadedAll(){
		return mIsLoadedAll;
	}
	
	/**
	 * <p>���ڵ�ǰ����ԣ���ʹ��onLoad(int start,int limit)�滻��onLoad()�����ã��ʽ����ʵ���Է�ֹ���౻ǿ��Ҫ��ʵ��
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
