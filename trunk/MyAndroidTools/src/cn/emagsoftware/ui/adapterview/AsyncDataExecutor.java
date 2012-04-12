package cn.emagsoftware.ui.adapterview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cn.emagsoftware.util.LogManager;

import android.os.Handler;
import android.os.Looper;
import android.widget.AdapterView;
import android.widget.ListView;

public abstract class AsyncDataExecutor {
	
	private static PushThread PUSH_THREAD = new PushThread();
	static{
		ThreadPoolManager.executeThread(PUSH_THREAD);
	}
	
	private int mMaxThreadCount = 5;
	private int mMaxPushedCount = 20;
	
	private AdapterView<?> mAdapterView = null;
	private GenericAdapter mGenericAdapter = null;
	
	private List<Integer> mPushedPositions = new LinkedList<Integer>();
	private List<DataHolder> mPushedHolders = new LinkedList<DataHolder>();
	private int mCurExecuteIndex = 0;
	private byte[] mLockExecute = new byte[0];
	private Set<Thread> mCurExecuteThreads = new HashSet<Thread>();
	private Handler mHandler = new Handler(Looper.getMainLooper());
	
	public AsyncDataExecutor(int maxThreadCount,int maxPushedCount){
		if(maxThreadCount <= 0) throw new IllegalArgumentException("maxThreadCount should be great than zero.");
		if(maxPushedCount <= 0) throw new IllegalArgumentException("maxPushedCount should be great than zero.");
		this.mMaxThreadCount = maxThreadCount;
		this.mMaxPushedCount = maxPushedCount;
	}
	
	public void bindForRefresh(AdapterView<?> adapterView,GenericAdapter genericAdapter){
		if(adapterView == null || genericAdapter == null) throw new NullPointerException();
		this.mAdapterView = adapterView;
		this.mGenericAdapter = genericAdapter;
	}
	
	public void pushAsync(int position,DataHolder dataHolder){
		if(!PUSH_THREAD.execPushingAsync(this, position, dataHolder)) push(position,dataHolder);    //�첽ִ�в���������ʱ�����ڵ�ǰ�߳�ִ�У��������ֻ����һ��ʼ����ʱż��
	}
	
	private void push(int position,DataHolder dataHolder){
		int asyncDataCount = dataHolder.getAsyncDataCount();
		List<Integer> asyncDataIndexes = new ArrayList<Integer>(asyncDataCount);
		for(int i = 0;i < asyncDataCount;i++){
			if(dataHolder.getAsyncData(i) == null) asyncDataIndexes.add(i);
		}
		
		
		
		
		
		Thread executeThread = null;
		synchronized(mLockExecute){
			int index = mPushedHolders.indexOf(dataHolder);
			if(index == -1){
				mPushedPositions.add(mCurExecuteIndex, position);
				mPushedHolders.add(mCurExecuteIndex, dataHolder);
				int size = mPushedPositions.size();
				if(size > mMaxPushedCount){
					mPushedPositions.remove(size - 1);
					mPushedHolders.remove(size - 1);
				}
				if(asyncDataIndexes.size() == 0) mCurExecuteIndex++;
				else if(mCurExecuteThreads.size() < mMaxThreadCount){
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
				if(asyncDataIndexes.size() == 0) mCurExecuteIndex++;
				else if(mCurExecuteThreads.size() < mMaxThreadCount){
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
					for(int i = 0;i < curHolder.getAsyncDataCount();i++){
						if(curHolder.getAsyncData(i) == null){
							try{
								Object asyncData = onExecute(curPosition,curHolder,i);
								curHolder.setAsyncData(i, asyncData);
								//���½���
								if(mAdapterView != null && mGenericAdapter != null){
									//�ж�����ִ��UI���������UI������Ч��
									if(mGenericAdapter.isConvertView()){
										mHandler.post(new Runnable() {
											@Override
											public void run() {
												// TODO Auto-generated method stub
												//�����ȡ��С��Χ�ĸ��²��ԣ�ͨ��notifyDataSetChanged���»�Ӱ��Ч��
												int count = mAdapterView.getChildCount();    //������header��footer�ĸ���
												if(count <= 0) return;
												int headerCount = 0;
												if(mAdapterView instanceof ListView) headerCount = ((ListView)mAdapterView).getHeaderViewsCount();
												int first = mAdapterView.getFirstVisiblePosition() - headerCount;
												int last = mAdapterView.getLastVisiblePosition() - headerCount;
												int end = count - 1 + first;
												if(first > end) return;
												if(last > end) last = end;
												
												
												
												
												int position = curPositions.next();
												if(position >= first && position <= last){
													DataHolder dholder = curResolvedHolders.get(position);
													int convertPosition = position - first;
													//getChildAt������header��footer������
													dholder.onUpdateView(mAdapterView.getContext(), position, mAdapterView.getChildAt(convertPosition), dholder.getData(), true);
												}
											}
										});
									}else{
										mHandler.post(new Runnable() {
											@Override
											public void run() {
												// TODO Auto-generated method stub
												//�����ȡ��С��Χ�ĸ��²��ԣ�ͨ��notifyDataSetChanged���»�Ӱ��Ч��
												int count = mAdapterView.getChildCount();    //������header��footer�ĸ���
												if(count <= 0) return;
												int first = mAdapterView.getFirstVisiblePosition() - mHeaderCount;
												int last = mAdapterView.getLastVisiblePosition() - mHeaderCount;
												int end = count - 1;
												if(first > end) return;
												if(last > end) last = end;
												
												int position = curPositions.next();
												if(position >= first && position <= last){
													DataHolder dholder = curResolvedHolders.get(position);
													int convertPosition = position;
													//getChildAt������header��footer������
													dholder.onUpdateView(mAdapterView.getContext(), position, mAdapterView.getChildAt(convertPosition), dholder.getData(), true);
												}
											}
										});
									}
								}
							}catch(Exception e){
								LogManager.logE(AsyncDataExecutor.class, "execute async data failed(position:"+curPosition+",index:"+i+")", e);
							}
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
					executor.push(position, dataHolder);
				}
			});
			return true;
		}
	}
	
}
