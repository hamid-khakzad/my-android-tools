package cn.emagsoftware.ui.adapterview;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.AdapterView;
import android.widget.ListView;
import cn.emagsoftware.util.LogManager;

public abstract class AsyncDataExecutor {
	
	private static PushTask PUSH_TASK = new PushTask();
	static{
		PUSH_TASK.execute("");
	}
	
	private int mMaxTaskCount = 5;
	private int mMaxWaitCount = 20;
	
	private WeakReference<AdapterView<?>> mAdapterViewRef = null;
	private WeakReference<GenericAdapter> mGenericAdapterRef = null;
	
	private LinkedList<DataHolder> mPushedHolders = new LinkedList<DataHolder>();
	private byte[] mLockExecute = new byte[0];
	private Set<AsyncTask<DataHolder, Object, Object>> mCurExecuteTasks = new HashSet<AsyncTask<DataHolder, Object, Object>>();
	
	public AsyncDataExecutor(int maxTaskCount,int maxWaitCount){
		if(maxTaskCount <= 0) throw new IllegalArgumentException("maxTaskCount should be great than zero.");
		if(maxWaitCount <= 0) throw new IllegalArgumentException("maxWaitCount should be great than zero.");
		this.mMaxTaskCount = maxTaskCount;
		this.mMaxWaitCount = maxWaitCount;
	}
	
	/**
	 * <p>��ˢ��UIʱʹ�õ���AdapterView��GenericAdapter���������߷����仯����Ҫ���µ��ô˷�������ˢ��
	 * <p>�÷���ֻ����UI�߳��е��ã��������ܱ�֤ͬ��
	 * @param adapterView
	 * @param genericAdapter
	 */
	public void bindForRefresh(AdapterView<?> adapterView,GenericAdapter genericAdapter){
		mAdapterViewRef = new WeakReference<AdapterView<?>>(adapterView);
		mGenericAdapterRef = new WeakReference<GenericAdapter>(genericAdapter);
	}
	
	public void pushAsync(DataHolder dataHolder){
		if(!PUSH_TASK.execPushingAsync(this,dataHolder)) push(dataHolder);    //�첽ִ�в���������ʱ�����ڵ�ǰ�߳�ִ�У��������ֻ����һ��ʼ����ʱż��
	}
	
	private void push(DataHolder dataHolder){
		if(dataHolder.mExecuteConfig.mIsExecuting) return;
		dataHolder.mExecuteConfig.mIsExecuting = true;
		AsyncTask<DataHolder, Object, Object> executeTask = null;
		synchronized(mLockExecute){
			if(mCurExecuteTasks.size() < mMaxTaskCount){
				executeTask = createExecuteTask();
				mCurExecuteTasks.add(executeTask);
			}else{
				mPushedHolders.addFirst(dataHolder);
				if(mPushedHolders.size() > mMaxWaitCount) mPushedHolders.removeLast();
			}
		}
		if(executeTask != null) executeTask.execute(dataHolder);
	}
	
	private AsyncTask<DataHolder, Object, Object> createExecuteTask(){
		return new AsyncTask<DataHolder, Object, Object>(){
			private DataHolder curHolder;
			@Override
			protected Object doInBackground(DataHolder... params) {
				// TODO Auto-generated method stub
				while(true){
					if(curHolder == null){
						curHolder = params[0];
						params[0] = null;
					}else{
						curHolder.mExecuteConfig.mIsExecuting = false;
						for(int i = 0;i < curHolder.getAsyncDataCount();i++){
							curHolder.changeAsyncDataToSoftReference(i);
						}
						synchronized(mLockExecute){
							curHolder = mPushedHolders.poll();
							if(curHolder == null){
								mCurExecuteTasks.remove(this);
								return null;
							}
						}
					}
					//�����ػ���ܻ��ظ�ʹ�õ��첽���ݣ���DataHolder��ִ�й�����ȴ�������ظ�ִ�У�����ִ��ʱҪ����������е��첽�������ǰ��ִ�����Ҫ����Ϊǿ����
					//��ǰ�̻߳���DataHolderȫ��ִ�������Ƴ����к�ͳһ���첽������Ϊ������
					for(int i = 0;i < curHolder.getAsyncDataCount();i++){
						Object curAsyncData = curHolder.getAsyncData(i);
						if(curAsyncData == null){
							try{
								Object asyncData = onExecute(curHolder.mExecuteConfig.mPosition,curHolder,i);
								if(asyncData == null) throw new NullPointerException("the method 'onExecute' returns null");
								curHolder.setAsyncData(i, asyncData);
								//���½���
								publishProgress(curHolder,asyncData,i);
							}catch(Exception e){
								LogManager.logE(AsyncDataExecutor.class, "execute async data failed(position:"+curHolder.mExecuteConfig.mPosition+",index:"+i+")", e);
							}
						}else{
							curHolder.setAsyncData(i, curAsyncData);
						}
					}
				}
			}
			@Override
			protected void onProgressUpdate(Object... values) {
				// TODO Auto-generated method stub
				super.onProgressUpdate(values);
				//�����ȡ��С��Χ�ĸ��²��ԣ�ͨ��notifyDataSetChanged���»�Ӱ��Ч��
				AdapterView<?> adapterView = mAdapterViewRef.get();
				GenericAdapter genericAdapter = mGenericAdapterRef.get();
				if(adapterView == null || genericAdapter == null) return;
				DataHolder holder = (DataHolder)values[0];
				int position = holder.mExecuteConfig.mPosition;
				if(position >= genericAdapter.getCount()) return;    //���淢���˸ı�
				if(!holder.equals(genericAdapter.queryDataHolder(position))) return;    //���淢���˸ı�
				int first = adapterView.getFirstVisiblePosition();
				int last = adapterView.getLastVisiblePosition();
				int wrapPosition = position;
				if(adapterView instanceof ListView){
					wrapPosition = wrapPosition + ((ListView)adapterView).getHeaderViewsCount();
				}
				if(wrapPosition >= first && wrapPosition <= last){
					holder.onAsyncDataExecuted(adapterView.getContext(), position, adapterView.getChildAt(wrapPosition - first), values[1], (Integer)values[2]);
				}
			}
		};
	}
	
	/**
	 * <p>�����첽���ݵĻص�������ע�⣬�÷������ܻ��ڶ��̵߳Ļ�����ִ�У�����Ҫ��֤�÷������̰߳�ȫ��
	 * <p>���׳��κ��쳣���׳��쳣ʱ���ⲿ����Ϊ��ǰ���첽����ִ��ʧ��
	 * @param position ����AdapterView�е�λ��
	 * @param dataHolder ����AdapterView��DataHolder����
	 * @param asyncDataIndex ��Ҫ���ص�DataHolder���첽���ݵ�����
	 * @return ִ�к�õ��Ľ��
	 * @throws Exception
	 */
	public abstract Object onExecute(int position,DataHolder dataHolder,int asyncDataIndex) throws Exception;
	
	private static class PushTask extends AsyncTask<Object, Integer, Object>{
		private Handler handler = null;
		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			Looper.prepare();
			handler = new Handler();
			Looper.loop();
			return null;
		}
		public boolean execPushingAsync(final AsyncDataExecutor executor,final DataHolder dataHolder){
			if(handler == null) return false;
			handler.postDelayed(new Runnable() {    //ÿ��320����ִ�У��Ա�������ִ�д����Ľ��滬����������
				@Override
				public void run() {
					// TODO Auto-generated method stub
					executor.push(dataHolder);
				}
			},320);
			return true;
		}
	}
	
}
