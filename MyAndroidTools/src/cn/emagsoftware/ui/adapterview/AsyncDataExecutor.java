package cn.emagsoftware.ui.adapterview;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;
import android.widget.AdapterView;
import android.widget.ListView;
import cn.emagsoftware.util.LogManager;

public abstract class AsyncDataExecutor {
	
	private static PushThread PUSH_THREAD = new PushThread();
	static{
		ThreadPoolManager.executeThread(PUSH_THREAD);
	}
	
	private int mMaxThreadCount = 5;
	private int mMaxWaitCount = 20;
	
	private AdapterView<?> mAdapterView = null;
	private GenericAdapter mGenericAdapter = null;
	
	private LinkedList<DataHolder> mPushedHolders = new LinkedList<DataHolder>();
	private byte[] mLockExecute = new byte[0];
	private Set<Thread> mCurExecuteThreads = new HashSet<Thread>();
	private Handler mHandler = new Handler(Looper.getMainLooper());
	
	public AsyncDataExecutor(int maxThreadCount,int maxWaitCount){
		if(maxThreadCount <= 0) throw new IllegalArgumentException("maxThreadCount should be great than zero.");
		if(maxWaitCount <= 0) throw new IllegalArgumentException("maxWaitCount should be great than zero.");
		this.mMaxThreadCount = maxThreadCount;
		this.mMaxWaitCount = maxWaitCount;
	}
	
	/**
	 * <p>��ˢ��UIʱʹ�õ���AdapterView��GenericAdapter���������߷����仯����Ҫ���µ��ô˷�������ˢ��
	 * <p>�÷���ֻ����UI�߳��е��ã��������ܱ�֤ͬ��
	 * @param adapterView
	 * @param genericAdapter
	 */
	public void bindForRefresh(AdapterView<?> adapterView,GenericAdapter genericAdapter){
		this.mAdapterView = adapterView;
		this.mGenericAdapter = genericAdapter;
	}
	
	public void pushAsync(DataHolder dataHolder){
		if(!PUSH_THREAD.execPushingAsync(this,dataHolder)) push(dataHolder);    //�첽ִ�в���������ʱ�����ڵ�ǰ�߳�ִ�У��������ֻ����һ��ʼ����ʱż��
	}
	
	private void push(DataHolder dataHolder){
		if(dataHolder.mExecuteConfig.mIsExecuting) return;
		dataHolder.mExecuteConfig.mIsExecuting = true;
		Thread executeThread = null;
		synchronized(mLockExecute){
			if(mCurExecuteThreads.size() < mMaxThreadCount){
				executeThread = createExecuteThread(dataHolder);
				mCurExecuteThreads.add(executeThread);
			}else{
				mPushedHolders.addFirst(dataHolder);
				if(mPushedHolders.size() > mMaxWaitCount) mPushedHolders.removeLast();
			}
		}
		if(executeThread != null) ThreadPoolManager.executeThread(executeThread);
	}
	
	private Thread createExecuteThread(final DataHolder firstDataHolder){
		return new Thread(){
			private DataHolder curHolder;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while(true){
					if(curHolder == null){
						curHolder = firstDataHolder;
					}else{
						curHolder.mExecuteConfig.mIsExecuting = false;
						for(int i = 0;i < curHolder.getAsyncDataCount();i++){
							curHolder.changeAsyncDataToSoftReference(i);
						}
						synchronized(mLockExecute){
							curHolder = mPushedHolders.poll();
							if(curHolder == null){
								mCurExecuteThreads.remove(this);
								return;
							}
						}
					}
					//�����ػ���ܻ��ظ�ʹ�õ��첽���ݣ���DataHolder��ִ�й�����ȴ�������ظ�ִ�У�����ִ��ʱҪ����������е��첽�������ǰ��ִ�����Ҫ����Ϊǿ����
					//��ǰ�̻߳���DataHolderȫ��ִ�������Ƴ����к�ͳһ���첽������Ϊ������
					for(int i = 0;i < curHolder.getAsyncDataCount();i++){
						Object curAsyncData = curHolder.getAsyncData(i);
						if(curAsyncData == null){
							try{
								final Object asyncData = onExecute(curHolder.mExecuteConfig.mPosition,curHolder,i);
								if(asyncData == null) throw new NullPointerException("the method 'onExecute' returns null");
								curHolder.setAsyncData(i, asyncData);
								//���½���
								final DataHolder curHolderPoint = curHolder;
								final int iCopy = i;
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										//�����ȡ��С��Χ�ĸ��²��ԣ�ͨ��notifyDataSetChanged���»�Ӱ��Ч��
										if(mAdapterView == null || mGenericAdapter == null) return;
										int position = curHolderPoint.mExecuteConfig.mPosition;
										if(position >= mGenericAdapter.getCount()) return;    //���淢���˸ı�
										if(!curHolderPoint.equals(mGenericAdapter.queryDataHolder(position))) return;    //���淢���˸ı�
										int first = mAdapterView.getFirstVisiblePosition();
										int last = mAdapterView.getLastVisiblePosition();
										int wrapPosition = position;
										if(mAdapterView instanceof ListView){
											wrapPosition = wrapPosition + ((ListView)mAdapterView).getHeaderViewsCount();
										}
										if(wrapPosition >= first && wrapPosition <= last){
											curHolderPoint.onAsyncDataExecuted(mAdapterView.getContext(), position, mAdapterView.getChildAt(wrapPosition - first), asyncData, iCopy);
										}
									}
								});
							}catch(Exception e){
								LogManager.logE(AsyncDataExecutor.class, "execute async data failed(position:"+curHolder.mExecuteConfig.mPosition+",index:"+i+")", e);
							}
						}else{
							curHolder.setAsyncData(i, curAsyncData);
						}
					}
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
	
	private static class PushThread extends Thread{
		private Handler handler = null;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			Looper.prepare();
			handler = new Handler();
			Looper.loop();
		}
		public boolean execPushingAsync(final AsyncDataExecutor executor,final DataHolder dataHolder){
			if(handler == null) return false;
			handler.postDelayed(new Runnable() {    //ÿ��200����ִ�У��Ա�������ִ�д����Ľ��滬����������
				@Override
				public void run() {
					// TODO Auto-generated method stub
					executor.push(dataHolder);
				}
			},200);
			return true;
		}
	}
	
}
