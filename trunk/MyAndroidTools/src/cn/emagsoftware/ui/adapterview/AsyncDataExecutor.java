package cn.emagsoftware.ui.adapterview;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.WrapperListAdapter;
import cn.emagsoftware.util.LogManager;

public abstract class AsyncDataExecutor {
	
	private static PushThread PUSH_THREAD = new PushThread();
	static{
		ThreadPoolManager.executeThread(PUSH_THREAD);
	}
	
	private int mMaxThreadCount = 5;
	private int mMaxWaitCount = 20;
	
	private AdapterView<?> mAdapterView = null;
	
	private List<Integer> mPushedPositions = new LinkedList<Integer>();
	private List<DataHolder> mPushedHolders = new LinkedList<DataHolder>();
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
	
	public void bindViewForRefresh(AdapterView<?> adapterView){
		this.mAdapterView = adapterView;
	}
	
	public void pushAsync(int position,DataHolder dataHolder){
		if(!PUSH_THREAD.execPushingAsync(this,position,dataHolder)) push(position,dataHolder);    //�첽ִ�в���������ʱ�����ڵ�ǰ�߳�ִ�У��������ֻ����һ��ʼ����ʱż��
	}
	
	private void push(int position,DataHolder dataHolder){
		Thread executeThread = null;
		synchronized(mLockExecute){
			int index = mPushedHolders.indexOf(dataHolder);
			if(index == -1){
				mPushedPositions.add(mCurExecuteIndex, position);
				mPushedHolders.add(mCurExecuteIndex, dataHolder);
				int size = mPushedPositions.size();
				if(size - mCurExecuteIndex > mMaxWaitCount){
					mPushedPositions.remove(size - 1);
					mPushedHolders.remove(size - 1);
				}
				if(mCurExecuteThreads.size() < mMaxThreadCount){
					executeThread = createExecuteThread(position,dataHolder);
					mCurExecuteThreads.add(executeThread);
					mCurExecuteIndex++;
				}
			}else if(index >= mCurExecuteIndex){    //��δִ�е�
				mPushedPositions.remove(index);
				mPushedPositions.add(mCurExecuteIndex, position);
				if(index != mCurExecuteIndex){
					mPushedHolders.remove(index);
					mPushedHolders.add(mCurExecuteIndex, dataHolder);
				}
				if(mCurExecuteThreads.size() < mMaxThreadCount){
					executeThread = createExecuteThread(position,dataHolder);
					mCurExecuteThreads.add(executeThread);
					mCurExecuteIndex++;
				}
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
							mPushedPositions.remove((Integer)curPosition);
							mPushedHolders.remove(curHolder);
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
								final int iCopy = i;
								final DataHolder curHolderPoint = curHolder;
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										//�����ȡ��С��Χ�ĸ��²��ԣ�ͨ��notifyDataSetChanged���»�Ӱ��Ч��
										AdapterView<?> adapterViewPoint = mAdapterView;
										if(adapterViewPoint == null) return;
										Adapter adapter = adapterViewPoint.getAdapter();
										if(adapter == null) return;
										if(adapter instanceof WrapperListAdapter) adapter = ((WrapperListAdapter)adapter).getWrappedAdapter();
										if(!(adapter instanceof GenericAdapter)) return;
										GenericAdapter genericAdapter = (GenericAdapter)adapter;    //�趯̬��ȡAdapter���Ա�֤���ݺ�UI��һ����
										int count = adapterViewPoint.getChildCount();    //������header��footer�ĸ���
										if(count <= 0) return;
										int headerCount = 0;
										if(adapterViewPoint instanceof ListView) headerCount = ((ListView)adapterViewPoint).getHeaderViewsCount();
										int first = adapterViewPoint.getFirstVisiblePosition() - headerCount;
										int last = adapterViewPoint.getLastVisiblePosition() - headerCount;
										int nowPosition = -1;
										int size = genericAdapter.getCount();
										for(int i = first;i <= last;i++){    //ֻѭ���ɼ���Χ�Է�ֹ����ռ��UI�߳�
											if(i >= size) break;
											if(curHolderPoint.equals(genericAdapter.queryDataHolder(i))){
												nowPosition = i;
												break;
											}
										}
										if(nowPosition != -1){    //��ǰDataHolder������λ�����ڿɼ���Χ��
											int convertPosition = nowPosition;
											if(genericAdapter.isConvertView()) convertPosition = nowPosition - first;
											//getChildAt������header��footer������
											curHolderPoint.onAsyncDataExecuted(adapterViewPoint.getContext(), nowPosition, adapterViewPoint.getChildAt(convertPosition), asyncData, iCopy);
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
