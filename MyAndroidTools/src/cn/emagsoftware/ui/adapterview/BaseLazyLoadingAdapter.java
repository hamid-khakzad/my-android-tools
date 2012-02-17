package cn.emagsoftware.ui.adapterview;

import java.lang.reflect.Field;
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
	
	public BaseLazyLoadingAdapter(Context context,int limit){
		super(context);
		if(limit <= 0) throw new IllegalArgumentException("limit should be great than zero.");
		mLimit = limit;
	}
	
	/**
	 * <p>��AdapterView��ʹ���Զ�������
	 * <p>Ŀǰֻ֧��AbsListView����AbsListView�����������ʱ���Զ���ʼ�µļ���
	 * <p>AbsListView��bindLazyLoadingʵ��ʵ����ִ����OnScrollListener�¼���
	 *    �û��������Լ���OnScrollListener�߼�������bindLazyLoading֮ǰ����setOnScrollListener��bindLazyLoading�����Ὣ�û����߼�����������
	 *    ����bindLazyLoading֮�����setOnScrollListener����ȡ��bindLazyLoading������
	 * @param adapterView
	 * @param remainingCount ��ʣ����ٸ�ʱ��ʼ�������أ���СֵΪ0����ʾֱ�����ſ�ʼ��������
	 */
	public void bindLazyLoading(AdapterView<?> adapterView,int remainingCount){
		if(adapterView instanceof AbsListView){
			try{
				AbsListView absList = (AbsListView)adapterView;
				Field field = AbsListView.class.getDeclaredField("mOnScrollListener");
				field.setAccessible(true);
				AbsListView.OnScrollListener onScrollListener = (AbsListView.OnScrollListener)field.get(absList);
				if(onScrollListener != null && onScrollListener instanceof LazyLoadingListener){
					absList.setOnScrollListener(new LazyLoadingListener(((LazyLoadingListener)onScrollListener).getOriginalListener(), remainingCount));
				}else{
					absList.setOnScrollListener(new LazyLoadingListener(onScrollListener, remainingCount));
				}
			}catch(NoSuchFieldException e){
				throw new RuntimeException(e);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}
		}else{
			throw new UnsupportedOperationException("Only supports lazy loading for the AdapterView which is AbsListView.");
		}
	}
	
	/**
	 * <p>�����˸����ͬ������������ִ��������
	 */
	@Override
	public boolean load(final Object condition) {
		// TODO Auto-generated method stub
		if(mIsLoading) return false;
		mIsLoading = true;
		mCurCondition = condition;
		onBeginLoad(mContext,condition);    //��load�е��ö�������UIThread�У���ʹUI�߳��ν�һ�£����������ͬ�������
		ThreadPoolManager.executeThread(new UIThread(mContext){
			@Override
			protected Object onRunNoUI(Context context) throws Exception {
				// TODO Auto-generated method stub
				super.onRunNoUI(context);
				return onLoad(context,condition,mStart,mLimit);
			}
			@SuppressWarnings("unchecked")
			@Override
			protected void onSuccessUI(Context context,Object result) {
				// TODO Auto-generated method stub
				super.onSuccessUI(context,result);
				if(result == null){
					mIsLoading = false;
					mIsLoaded = true;
					mIsLoadedAll = true;
					mIsException = false;
					onAfterLoad(context,condition,null);
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
					onAfterLoad(context,condition,null);
				}
			}
			@Override
			protected void onExceptionUI(Context context,Exception e) {
				// TODO Auto-generated method stub
				super.onExceptionUI(context,e);
				Log.e("BaseLazyLoadingAdapter","Execute lazy loading failed.",e);
				mIsLoading = false;
				mIsException = true;
				onAfterLoad(context,condition,e);
			}
		});
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
	 * <p>���ڵ�ǰ����ԣ���ʹ��onLoad(Context context,Object condition,int start,int limit)�滻�˵�ǰ���������ã��ʽ����ʵ���Է�ֹ���౻ǿ��Ҫ��ʵ��
	 */
	@Override
	public List<DataHolder> onLoad(Context context,Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * <p>���صľ���ʵ�֣�ͨ������Ĳ�������ʵ�ֶַε������ء��÷����ɷ�UI�̻߳ص������Կ���ִ�к�ʱ����
	 * @param context
	 * @param condition
	 * @param start ���μ��صĿ�ʼ���
	 * @param limit ���μ��صĳ���
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(Context context,Object condition,int start,int limit) throws Exception;
	
	private class LazyLoadingListener implements AbsListView.OnScrollListener{
		private AbsListView.OnScrollListener mOriginalListener = null;
		private int mRemainingCount = 0;
		public LazyLoadingListener(AbsListView.OnScrollListener originalListener,int remainingCount){
			if(originalListener != null && originalListener instanceof LazyLoadingListener) throw new IllegalArgumentException("the OnScrollListener could not be LazyLoadingListener");
			this.mOriginalListener = originalListener;
			this.mRemainingCount = remainingCount;
		}
		@Override
		public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
			// TODO Auto-generated method stub
			//ִ��ԭʼ���������߼�
			if(mOriginalListener != null) mOriginalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			//ִ��setOnScrollListenerʱ�ͻᴥ��onScroll����ʱҪ�ų�AbsListView���ɼ���ɼ�Item����Ϊ0�����
			//�޸�AbsListView��Item����ʱ�ᴥ��onScroll����ʱҪ�ų�AbsListView���ɼ������
			if(visibleItemCount == 0) return;
			if(firstVisibleItem + visibleItemCount + mRemainingCount >= totalItemCount && !isLoadedAll() && !isException()){
				load(mCurCondition);
			}
		}
		@Override
		public void onScrollStateChanged(AbsListView view,int scrollState) {
			// TODO Auto-generated method stub
			//ִ��ԭʼ���������߼�
			if(mOriginalListener != null) mOriginalListener.onScrollStateChanged(view, scrollState);
		}
		public AbsListView.OnScrollListener getOriginalListener(){
			return mOriginalListener;
		}
	}
	
}
