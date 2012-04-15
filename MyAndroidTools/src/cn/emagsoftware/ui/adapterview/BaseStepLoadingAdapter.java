package cn.emagsoftware.ui.adapterview;

import java.lang.reflect.Field;
import java.util.List;

import cn.emagsoftware.ui.UIThread;
import cn.emagsoftware.util.LogManager;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.AdapterView;

public abstract class BaseStepLoadingAdapter extends BaseLoadingAdapter {
	
	/**��ǰ��ҳ��*/
	private int mPage = 0;
	/**��ҳ��*/
	private int mPages = -1;
	/**�Ƿ��Ѿ�������ȫ������*/
	private boolean mIsLoadedAll = false;
	
	public BaseStepLoadingAdapter(Context context) {
		super(context);
	}
	
	/**
	 * <p>��AdapterView��ʹ���Զ��ֲ�����
	 * <p>Ŀǰֻ֧��AbsListView����AbsListView�����������ʱ���Զ���ʼ�µļ���
	 * <p>AbsListView��bindStepLoadingʵ��ʵ����ִ����OnScrollListener�¼���
	 *    �û��������Լ���OnScrollListener�߼�������bindStepLoading֮ǰ����setOnScrollListener��bindStepLoading�����Ὣ�û����߼�����������
	 *    ����bindStepLoading֮�����setOnScrollListener����ȡ��bindStepLoading������
	 * @param adapterView
	 * @param remainingCount ��ʣ����ٸ�ʱ��ʼ�������أ���СֵΪ0����ʾֱ�����ſ�ʼ��������
	 */
	public void bindStepLoading(AdapterView<?> adapterView,int remainingCount){
		if(adapterView instanceof AbsListView){
			try{
				AbsListView absList = (AbsListView)adapterView;
				Field field = AbsListView.class.getDeclaredField("mOnScrollListener");
				field.setAccessible(true);
				AbsListView.OnScrollListener onScrollListener = (AbsListView.OnScrollListener)field.get(absList);
				if(onScrollListener != null && onScrollListener instanceof StepLoadingListener){
					absList.setOnScrollListener(new StepLoadingListener(((StepLoadingListener)onScrollListener).getOriginalListener(), remainingCount));
				}else{
					absList.setOnScrollListener(new StepLoadingListener(onScrollListener, remainingCount));
				}
			}catch(NoSuchFieldException e){
				throw new RuntimeException(e);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}
		}else{
			throw new UnsupportedOperationException("Only supports step loading for the AdapterView which is AbsListView.");
		}
	}
	
	/**
	 * <p>�����˸����ͬ������������ִ�зֲ�����
	 */
	@Override
	public boolean load(final Object condition) {
		// TODO Auto-generated method stub
		if(mIsLoading) return false;
		mIsLoading = true;
		mCurCondition = condition;
		onBeginLoad(mContext,condition);    //��load�е��ö�������UIThread��onBeginUI�У���ʹUI�߳��ν�һ�£���������ⲿ�Ĳ�ͬ�����
		final int start = getRealCount();
		ThreadPoolManager.executeThread(new UIThread(mContext){
			@Override
			protected Object onRunNoUI(Context context) throws Exception {
				// TODO Auto-generated method stub
				super.onRunNoUI(context);
				return onLoad(context,condition,start,mPage + 1);
			}
			@SuppressWarnings("unchecked")
			@Override
			protected void onSuccessUI(Context context,Object result) {
				// TODO Auto-generated method stub
				super.onSuccessUI(context,result);
				mPage++;
				List<DataHolder> resultList = (List<DataHolder>)result;
				if(resultList != null && resultList.size() > 0) addDataHolders(resultList);    //�÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
				mIsLoading = false;
				mIsLoaded = true;
				if(mPages == -1){
					if(resultList == null || resultList.size() == 0) mIsLoadedAll = true;
					else mIsLoadedAll = false;
				}else{
					if(mPage >= mPages) mIsLoadedAll = true;
					else mIsLoadedAll = false;
				}
				mIsException = false;
				onAfterLoad(context,condition,null);
			}
			@Override
			protected void onExceptionUI(Context context,Exception e) {
				// TODO Auto-generated method stub
				super.onExceptionUI(context,e);
				LogManager.logE(BaseStepLoadingAdapter.class, "Execute step loading failed.", e);
				mIsLoading = false;
				mIsException = true;
				onAfterLoad(context,condition,e);
			}
		});
		return true;
	}
	
	/**
	 * <p>��ȡ��ǰ��ҳ��
	 * @return
	 */
	public int getPage(){
		return mPage;
	}
	
	/**
	 * <p>������ҳ��
	 * <p>ͨ�����ø÷���������ҳ�뷶Χ���Ӷ����ⲻ��Ҫ�Ķ�����أ�����ֻ���ڷֲ����ص�����Ϊ��ʱ����Ϊ��ȫ������
	 * @param pages
	 */
	public void setPages(int pages){
		if(pages < 0) throw new IllegalArgumentException("pages could not be less than zero.");
		this.mPages = pages;
	}
	
	/**
	 * <p>��ȡ��ҳ��
	 * @return
	 */
	public int getPages(){
		return mPages;
	}
	
	/**
	 * <p>�Ƿ���ȫ������
	 * @return
	 */
	public boolean isLoadedAll(){
		return mIsLoadedAll;
	}
	
	/**
	 * <p>���Ǹ���ķ����������õ�ǰ���һЩ����
	 */
	@Override
	public void clearDataHolders() {
		// TODO Auto-generated method stub
		super.clearDataHolders();
		mPage = 0;
	}
	
	/**
	 * <p>���ڵ�ǰ����ԣ���ʹ��onLoad(Context context,Object condition,int start,int page)�滻�˵�ǰ���������ã��ʽ����ʵ���Է�ֹ���౻ǿ��Ҫ��ʵ��
	 */
	@Override
	public List<DataHolder> onLoad(Context context,Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * <p>���صľ���ʵ�֣�ͨ������Ĳ�������ʵ�ֲַ����ء��÷����ɷ�UI�̻߳ص������Կ���ִ�к�ʱ����
	 * @param context
	 * @param condition
	 * @param start Ҫ���صĿ�ʼ��ţ���СֵΪ0
	 * @param page Ҫ���ص�ҳ�룬��СֵΪ1
	 * @return
	 * @throws Exception
	 */
	public abstract List<DataHolder> onLoad(Context context,Object condition,int start,int page) throws Exception;
	
	private class StepLoadingListener implements AbsListView.OnScrollListener{
		private AbsListView.OnScrollListener mOriginalListener = null;
		private int mRemainingCount = 0;
		public StepLoadingListener(AbsListView.OnScrollListener originalListener,int remainingCount){
			if(originalListener != null && originalListener instanceof StepLoadingListener) throw new IllegalArgumentException("the OnScrollListener could not be StepLoadingListener");
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
