package cn.emagsoftware.ui.adapterview;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Looper;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.OptionalExecutorTask;

public abstract class AsyncDataExecutor
{
    private static Executor EXECUTOR = new ThreadPoolExecutor(0,6,60, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),new ThreadPoolExecutor.CallerRunsPolicy());
    private static PushTask                               PUSH_TASK              = new PushTask();
    static
    {
        PUSH_TASK.executeOnExecutor(EXECUTOR,"");
    }

    private int                                           mMaxTaskCount          = 2;

    private int                                           mMaxWaitCount          = 20;
    private WeakReference<AdapterView<? extends Adapter>> mAdapterViewRef        = null;
    private WeakReference<GenericAdapter>                 mGenericAdapterRef     = null;

    private LinkedList<DataHolder>                        mPushedHolders         = new LinkedList<DataHolder>();
    private byte[]                                        mLockExecute           = new byte[0];
    private Set<OptionalExecutorTask<DataHolder, Object, Object>>    mCurExecuteTasks       = new HashSet<OptionalExecutorTask<DataHolder, Object, Object>>();

    private boolean                                       mNotifyAsyncDataForAll = false;

    public AsyncDataExecutor(int maxTaskCount)
    {
        if (maxTaskCount < 1 || maxTaskCount > 5) // 需要给线程池留一个供PushTask长期占用
            throw new IllegalArgumentException("maxTaskCount should be between 1 and 5.");
        this.mMaxTaskCount = maxTaskCount;
    }

    /**
     * <p>设置在加载完GenericAdapter的异步数据后是否通知该GenericAdapter对应的所有AdapterView <p>一般不要调用该方法并传true，这会影响到性能，除非当前的GenericAdapter被多个AdapterView共用，且在异步数据加载完后需要即时同步到各个AdapterView
     * 
     * @param notifyAsyncDataForAll
     */
    public void setNotifyAsyncDataForAll(boolean notifyAsyncDataForAll)
    {
        this.mNotifyAsyncDataForAll = notifyAsyncDataForAll;
    }

    void refreshVariables(AdapterView<? extends Adapter> adapterView, GenericAdapter genericAdapter)
    {
        mMaxWaitCount = adapterView.getLastVisiblePosition() - adapterView.getFirstVisiblePosition() + 2; // AdapterView在第一次布局显示时可能需要加2，所以这里统一加2
        mAdapterViewRef = new WeakReference<AdapterView<? extends Adapter>>(adapterView);
        mGenericAdapterRef = new WeakReference<GenericAdapter>(genericAdapter);
    }

    void pushAsync(DataHolder dataHolder)
    {
        if (!PUSH_TASK.execPushingAsync(this, dataHolder))
            push(dataHolder); // 异步执行不满足条件时，将在当前线程执行，这种情况只会在一开始调用时偶发
    }

    private void push(DataHolder dataHolder)
    {
        if (dataHolder.mExecuteConfig.mIsExecuting)
            return;
        dataHolder.mExecuteConfig.mIsExecuting = true;
        OptionalExecutorTask<DataHolder, Object, Object> executeTask = null;
        synchronized (mLockExecute)
        {
            if (mCurExecuteTasks.size() < mMaxTaskCount)
            {
                executeTask = createExecuteTask();
                mCurExecuteTasks.add(executeTask);
            } else
            {
                mPushedHolders.addFirst(dataHolder);
                int excessCount = mPushedHolders.size() - mMaxWaitCount;
                while (excessCount > 0)
                {
                    mPushedHolders.removeLast().mExecuteConfig.mIsExecuting = false;
                    excessCount--;
                }
            }
        }
        if (executeTask != null)
            executeTask.executeOnExecutor(EXECUTOR,dataHolder);
    }

    private OptionalExecutorTask<DataHolder, Object, Object> createExecuteTask()
    {
        return new OptionalExecutorTask<DataHolder, Object, Object>()
        {
            private DataHolder curHolder;
            private boolean    shouldChangeSoftRefByPublish;

            @Override
            protected Object doInBackground(DataHolder... params)
            {
                // TODO Auto-generated method stub
                while (true)
                {
                    if (curHolder == null)
                    {
                        curHolder = params[0];
                        params[0] = null;
                    } else
                    {
                        curHolder.mExecuteConfig.mIsExecuting = false;
                        if (shouldChangeSoftRefByPublish)
                        {
                            // 统一置为软引用可能需要通过publish方式来进行，因为需要保证该操作发生在更新界面publishProgress可能导致的界面重构之后
                            publishProgress(curHolder);
                        } else
                        {
                            for (int i = 0; i < curHolder.getAsyncDataCount(); i++)
                            {
                                curHolder.changeAsyncDataToSoftReference(i);
                            }
                        }
                        synchronized (mLockExecute)
                        {
                            curHolder = mPushedHolders.poll();
                            if (curHolder == null)
                            {
                                mCurExecuteTasks.remove(this);
                                return null;
                            }
                        }
                    }
                    // 更新界面publishProgress可能导致界面重构，从而重复使用到异步数据，但DataHolder在执行过程中却不允许重复执行，所以执行时要遍历检查所有的异步项，并且先前已执行完的要升级为强引用
                    shouldChangeSoftRefByPublish = false;
                    for (int i = 0; i < curHolder.getAsyncDataCount(); i++)
                    {
                        Object curAsyncData = curHolder.getAsyncData(i);
                        if (curAsyncData == null)
                        {
                            try
                            {
                                Object asyncData = onExecute(curHolder.mExecuteConfig.mPosition, curHolder, i);
                                if (asyncData == null)
                                    throw new NullPointerException("the method 'onExecute' returns null");
                                curHolder.setAsyncData(i, asyncData);
                                // 更新界面
                                publishProgress(curHolder, asyncData, i);
                                shouldChangeSoftRefByPublish = true;
                            } catch (Exception e)
                            {
                                LogManager.logE(AsyncDataExecutor.class, "execute async data failed(position:" + curHolder.mExecuteConfig.mPosition + ",index:" + i + ")", e);
                            }
                        } else
                        {
                            curHolder.setAsyncData(i, curAsyncData);
                        }
                    }
                }
            }

            @Override
            protected void onProgressUpdate(Object... values)
            {
                // TODO Auto-generated method stub
                super.onProgressUpdate(values);
                if (values.length == 1)
                {
                    // 再次publish，这样才能保证统一置为软引用操作发生在更新界面publishProgress可能导致的界面重构之后
                    publishProgress(values[0], 0);
                } else if (values.length == 2)
                {
                    DataHolder holder = (DataHolder) values[0];
                    for (int i = 0; i < holder.getAsyncDataCount(); i++)
                    {
                        holder.changeAsyncDataToSoftReference(i);
                    }
                } else
                {
                    GenericAdapter genericAdapter = mGenericAdapterRef.get();
                    if (genericAdapter == null)
                        return;
                    if (mNotifyAsyncDataForAll)
                    {
                        genericAdapter.notifyDataSetChanged();
                    } else
                    {
                        AdapterView<? extends Adapter> adapterView = mAdapterViewRef.get();
                        if (adapterView == null)
                            return;
                        DataHolder holder = (DataHolder) values[0];
                        int position = holder.mExecuteConfig.mPosition;
                        if (position >= genericAdapter.getCount())
                            return; // DataHolder被执行就意味着Adapter和AdapterView已经同步，此时再判断下Adapter的变化，即可解决所有的不一致问题
                        if (!holder.equals(genericAdapter.queryDataHolder(position)))
                            return; // DataHolder被执行就意味着Adapter和AdapterView已经同步，此时再判断下Adapter的变化，即可解决所有的不一致问题
                        int wrapPosition = position;
                        if (adapterView instanceof ListView)
                            wrapPosition = wrapPosition + ((ListView) adapterView).getHeaderViewsCount();
                        int first = adapterView.getFirstVisiblePosition();
                        int last = adapterView.getLastVisiblePosition();
                        if (wrapPosition >= first && wrapPosition <= last)
                            holder.onAsyncDataExecuted(adapterView.getContext(), position, adapterView.getChildAt(wrapPosition - first), values[1], (Integer) values[2]);
                    }
                }
            }
        };
    }

    /**
     * <p>加载异步数据的回调方法 <p>可抛出任何异常，抛出异常时，外部会认为当前的异步数据执行失败
     * 
     * @param position 所在AdapterView中的位置
     * @param dataHolder 用于AdapterView的DataHolder对象
     * @param asyncDataIndex 需要加载的DataHolder中异步数据的索引
     * @return 执行后得到的结果
     * @throws Exception
     */
    public abstract Object onExecute(int position, DataHolder dataHolder, int asyncDataIndex) throws Exception;

    private static class PushTask extends OptionalExecutorTask<Object, Integer, Object>
    {
        private Handler handler = null;

        @Override
        protected Object doInBackground(Object... params)
        {
            // TODO Auto-generated method stub
            Looper.prepare();
            handler = new Handler();
            Looper.loop();
            return null;
        }

        public boolean execPushingAsync(final AsyncDataExecutor executor, final DataHolder dataHolder)
        {
            if (handler == null)
                return false;
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    // TODO Auto-generated method stub
                    executor.push(dataHolder);
                }
            });
            return true;
        }
    }

}
