package cn.emagsoftware.ui.adapterview;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
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
	
	private LinkedList<Integer> mPushedPositions = new LinkedList<Integer>();
	private LinkedList<DataHolder> mPushedHolders = new LinkedList<DataHolder>();
	private int mCurExecuteIndex = 0;
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
	
	public void pushAsync(int position,DataHolder dataHolder){
		if(!PUSH_THREAD.execPushingAsync(this,position,dataHolder)) push(position,dataHolder);    //�첽ִ�в���������ʱ�����ڵ�ǰ�߳�ִ�У��������ֻ����һ��ʼ����ʱż��
	}
	
	private void push(int position,DataHolder dataHolder){
		Thread executeThread = null;
		synchronized(mLockExecute){
			ListIterator<Integer> positionIterator = mPushedPositions.listIterator();
			ListIterator<DataHolder> dataHolderIterator = mPushedHolders.listIterator();
			int index = -1;
			boolean isRemoved = false;
			while(positionIterator.hasNext()){
				int curPosition = positionIterator.next();
				DataHolder curDataHolder = dataHolderIterator.next();
				if(++index < mCurExecuteIndex){
					if(position == curPosition && dataHolder.equals(curDataHolder)) return;
				}else if(index == mCurExecuteIndex){
					positionIterator.add(position);
					dataHolderIterator.add(dataHolder);
					index++;
				}else if(position == curPosition){
					positionIterator.remove();
					dataHolderIterator.remove();
					isRemoved = true;
					break;
				}
			}
			if(!isRemoved){
				if(mPushedPositions.size() - mCurExecuteIndex > mMaxWaitCount){
					mPushedPositions.removeLast();
					mPushedHolders.removeLast();
				}
			}
			if(mCurExecuteThreads.size() < mMaxThreadCount){
				executeThread = createExecuteThread(position,dataHolder);
				mCurExecuteThreads.add(executeThread);
				mCurExecuteIndex++;
			}
		}
		if(executeThread != null) ThreadPoolManager.executeThread(executeThread);
	}
	
	private Thread createExecuteThread(final int firstPosition,final DataHolder firstDataHolder){
		return new Thread(){
			private int curPosition;
			private DataHolder curHolder;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while(true){
					if(curHolder == null){
						curPosition = firstPosition;
						curHolder = firstDataHolder;
					}else{
						synchronized(mLockExecute){
							Iterator<Integer> positionIterator = mPushedPositions.iterator();
							Iterator<DataHolder> dataHolderIterator = mPushedHolders.iterator();
							while(positionIterator.hasNext()){
								int onePosition = positionIterator.next();
								DataHolder oneDataHolder = dataHolderIterator.next();
								if(curPosition == onePosition && curHolder.equals(oneDataHolder)){
									positionIterator.remove();
									dataHolderIterator.remove();
									break;
								}
							}
							if(mCurExecuteIndex > 0) mCurExecuteIndex--;
							for(int i = 0;i < curHolder.getAsyncDataCount();i++){
								curHolder.changeAsyncDataToSoftReference(i);
							}
							if(mCurExecuteIndex >= mPushedPositions.size()){
								mCurExecuteThreads.remove(this);
								return;
							}else{
								curPosition = mPushedPositions.get(mCurExecuteIndex);
								curHolder = mPushedHolders.get(mCurExecuteIndex);
								mCurExecuteIndex++;
							}
						}
					}
					//�����ػ���ܻ��ظ�ʹ�õ��첽���ݣ���DataHolder��ִ�й�����ȴ�������ظ�ִ�У�����ִ��ʱҪ����������е��첽�������ǰ��ִ�����Ҫ����Ϊǿ����
					//��ǰ�̻߳���DataHolderȫ��ִ�������Ƴ����к�ͳһ���첽������Ϊ������
					for(int i = 0;i < curHolder.getAsyncDataCount();i++){
						Object curAsyncData = curHolder.getAsyncData(i);
						if(curAsyncData == null){
							try{
								final Object asyncData = onExecute(curPosition,curHolder,i);
								if(asyncData == null) throw new NullPointerException("the method 'onExecute' returns null");
								curHolder.setAsyncData(i, asyncData);
								//���½���
								final int curPositionCopy = curPosition;
								final DataHolder curHolderPoint = curHolder;
								final int iCopy = i;
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										//�����ȡ��С��Χ�ĸ��²��ԣ�ͨ��notifyDataSetChanged���»�Ӱ��Ч��
										if(mAdapterView == null || mGenericAdapter == null) return;
										if(curPositionCopy >= mGenericAdapter.getCount()) return;    //���淢���˸ı�
										if(!curHolderPoint.equals(mGenericAdapter.queryDataHolder(curPositionCopy))) return;    //���淢���˸ı�
										int first = mAdapterView.getFirstVisiblePosition();
										int last = mAdapterView.getLastVisiblePosition();
										int position = curPositionCopy;
										if(mAdapterView instanceof ListView){
											position = position + ((ListView)mAdapterView).getHeaderViewsCount();
										}
										if(position >= first && position <= last){
											curHolderPoint.onAsyncDataExecuted(mAdapterView.getContext(), curPositionCopy, mAdapterView.getChildAt(position - first), asyncData, iCopy);
										}
									}
								});
							}catch(Exception e){
								LogManager.logE(AsyncDataExecutor.class, "execute async data failed(position:"+curPosition+",index:"+i+")", e);
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
		public boolean execPushingAsync(final AsyncDataExecutor executor,final int position,final DataHolder dataHolder){
			if(handler == null) return false;
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					executor.push(position,dataHolder);
				}
			});
			return true;
		}
	}
	
}
