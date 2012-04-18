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
	 * <p>绑定刷新UI时使用到的AdapterView和GenericAdapter，若这两者发生变化，需要重新调用此方法进行刷新
	 * <p>该方法只能在UI线程中调用，这样才能保证同步
	 * @param adapterView
	 * @param genericAdapter
	 */
	public void bindForRefresh(AdapterView<?> adapterView,GenericAdapter genericAdapter){
		mAdapterViewRef = new WeakReference<AdapterView<?>>(adapterView);
		mGenericAdapterRef = new WeakReference<GenericAdapter>(genericAdapter);
	}
	
	public void pushAsync(DataHolder dataHolder){
		if(!PUSH_TASK.execPushingAsync(this,dataHolder)) push(dataHolder);    //异步执行不满足条件时，将在当前线程执行，这种情况只会在一开始调用时偶发
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
					//界面重绘可能会重复使用到异步数据，但DataHolder在执行过程中却不允许重复执行，所以执行时要遍历检查所有的异步项，并且先前已执行完的要升级为强引用
					//当前线程会在DataHolder全部执行完且移出队列后统一将异步数据置为软引用
					for(int i = 0;i < curHolder.getAsyncDataCount();i++){
						Object curAsyncData = curHolder.getAsyncData(i);
						if(curAsyncData == null){
							try{
								Object asyncData = onExecute(curHolder.mExecuteConfig.mPosition,curHolder,i);
								if(asyncData == null) throw new NullPointerException("the method 'onExecute' returns null");
								curHolder.setAsyncData(i, asyncData);
								//更新界面
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
				//这里采取最小范围的更新策略，通过notifyDataSetChanged更新会影响效率
				AdapterView<?> adapterView = mAdapterViewRef.get();
				GenericAdapter genericAdapter = mGenericAdapterRef.get();
				if(adapterView == null || genericAdapter == null) return;
				DataHolder holder = (DataHolder)values[0];
				int position = holder.mExecuteConfig.mPosition;
				if(position >= genericAdapter.getCount()) return;    //界面发生了改变
				if(!holder.equals(genericAdapter.queryDataHolder(position))) return;    //界面发生了改变
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
	 * <p>加载异步数据的回调方法，注意，该方法可能会在多线程的环境中执行，所以要保证该方法是线程安全的
	 * <p>可抛出任何异常，抛出异常时，外部会认为当前的异步数据执行失败
	 * @param position 所在AdapterView中的位置
	 * @param dataHolder 用于AdapterView的DataHolder对象
	 * @param asyncDataIndex 需要加载的DataHolder中异步数据的索引
	 * @return 执行后得到的结果
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
			handler.postDelayed(new Runnable() {    //每隔320毫秒执行，以避免连续执行带来的界面滑动卡顿现象
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
