package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.ui.UIThread;

import android.content.Context;
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
	/**��ǰ�ļ����Ƿ������쳣*/
	protected boolean mIsException = false;
	
	public BaseLazyLoadingAdapter(Context context,int limit){
		super(context);
		if(limit <= 0) throw new IllegalArgumentException("limit should be great than zero.");
		mLimit = limit;
	}
	
	/**
	 * <p>��AdapterView��ʹ���Զ�������
	 * <p>Ŀǰֻ֧��AbsListView����AbsListView�����������ʱ���Զ���ʼ�µļ���
	 *    ������AbsListView��setAdapter��addFooterView�ȷ���ʱҲ���Զ��������أ���Ҫ����������ε��Զ����أ��ɽ���Щ�����ĵ��÷��ڵ�ǰ����֮ǰ
	 * @param adapterView
	 */
	public void bindLazyLoading(AdapterView<?> adapterView){
		if(adapterView instanceof AbsListView){
			AbsListView absList = (AbsListView)adapterView;
			absList.setOnScrollListener(new AbsListView.OnScrollListener(){
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
					// TODO Auto-generated method stub
					if(firstVisibleItem + visibleItemCount == totalItemCount && !isLoadedAll() && !mIsException){
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
		ThreadPoolManager.executeThread(new UIThread(mContext,new UIThread.Callback(){
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
				return onLoad(context, mStart, mLimit);
			}
			@Override
			public void onSuccessUI(Context context, Object result) {
				// TODO Auto-generated method stub
				super.onSuccessUI(context, result);
				if(result == null){
					mIsLoading = false;
					mIsLoaded = true;
					mIsLoadedAll = true;
					mIsException = false;
					onAfterLoad(context,null);
				}else{
					List<DataHolder> resultList = (List<DataHolder>)result;
					addDataHolders(resultList);    //�÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
					mIsLoading = false;
					mIsLoaded = true;
					int size = resultList.size();
					mStart = mStart + size;
					if(size == 0) mIsLoadedAll = true;
					else mIsLoadedAll = false;
					mIsException = false;
					onAfterLoad(context,null);
				}
			}
			@Override
			public void onExceptionUI(Context context, Exception e) {
				// TODO Auto-generated method stub
				super.onExceptionUI(context, e);
				Log.e("BaseLazyLoadingAdapter", "Execute lazy loading failed.", e);
				mIsLoading = false;
				mIsException = true;
				onAfterLoad(context,e);
			}
		}));
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
	 * <p>���ڵ�ǰ����ԣ���ʹ��onLoad(Context context,int start,int limit)�滻��onLoad(Context context)�����ã��ʽ����ʵ���Է�ֹ���౻ǿ��Ҫ��ʵ��
	 */
	@Override
	public List<DataHolder> onLoad(Context context) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * <p>���صľ���ʵ�֣�ͨ������Ĳ�������ʵ�ֶַε������ء��÷����ɷ�UI�̻߳ص������Կ���ִ�к�ʱ����
	 * @param context
	 * @param start ���μ��صĿ�ʼ���
	 * @param limit ���μ��صĳ���
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(Context context,int start,int limit) throws Exception;
	
}
