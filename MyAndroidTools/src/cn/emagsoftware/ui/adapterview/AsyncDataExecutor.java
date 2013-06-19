package cn.emagsoftware.ui.adapterview;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.OptionalExecutorTask;

public abstract class AsyncDataExecutor
{
    private static Executor EXECUTOR = new ThreadPoolExecutor(0,5,30, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),new ThreadPoolExecutor.CallerRunsPolicy());
    private static PushTask                               PUSH_TASK              = new PushTask();
    static
    {
        PUSH_TASK.execute("");
    }

    private int                                           mMaxTaskCount          = 2;

    private int                                           mMaxWaitCount          = 20;
    private WeakReference<AdapterView<? extends Adapter>> mAdapterViewRef        = null;
    private WeakReference<Object>                 mGenericAdapterRef     = null;

    private LinkedList<DataHolder>                        mPushedHolders         = new LinkedList<DataHolder>();
    private byte[]                                        mLockExecute           = new byte[0];
    private Set<OptionalExecutorTask<DataHolder, Object, Object>>    mCurExecuteTasks       = new HashSet<OptionalExecutorTask<DataHolder, Object, Object>>();

    private boolean                                       mNotifyAsyncDataForAll = false;

    public AsyncDataExecutor(int maxTaskCount)
    {
        if (maxTaskCount < 1 || maxTaskCount > 5)
            throw new IllegalArgumentException("maxTaskCount should be between 1 and 5.");
        this.mMaxTaskCount = maxTaskCount;
    }

    /**
     * <p>�����ڼ�����GenericAdapter���첽���ݺ��Ƿ�֪ͨ��GenericAdapter��Ӧ������AdapterView <p>һ�㲻Ҫ���ø÷�������true�����Ӱ�쵽���ܣ����ǵ�ǰ��GenericAdapter�����AdapterView���ã������첽���ݼ��������Ҫ��ʱͬ��������AdapterView
     * 
     * @param notifyAsyncDataForAll
     */
    public void setNotifyAsyncDataForAll(boolean notifyAsyncDataForAll)
    {
        this.mNotifyAsyncDataForAll = notifyAsyncDataForAll;
    }

    void refreshVariables(AdapterView<? extends Adapter> adapterView, Object genericAdapter)
    {
        mMaxWaitCount = adapterView.getLastVisiblePosition() - adapterView.getFirstVisiblePosition() + 2; // AdapterView�ڵ�һ�β�����ʾʱ������Ҫ��2����������ͳһ��2
        mAdapterViewRef = new WeakReference<AdapterView<? extends Adapter>>(adapterView);
        mGenericAdapterRef = new WeakReference<Object>(genericAdapter);
    }

    void pushAsync(DataHolder dataHolder)
    {
        /**
         * ʹ������ʵ�ֲ�����Ҫ������:
         * 1.�ɼ������߳�ѹ��,ʹAdapterView����������
         * 2.�첽����ִ�в�����ThreadPoolExecutor.CallerRunsPolicy���̳߳ز��ԣ���ʵ�ֿ�ȷ�����������̶߳���
         */
        PUSH_TASK.execPushingAsync(this, dataHolder);
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
                            // ͳһ��Ϊ�����ÿ�����Ҫͨ��publish��ʽ�����У���Ϊ��Ҫ��֤�ò��������ڸ��½���publishProgress���ܵ��µĽ����ع�֮��
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
                    // ���½���publishProgress���ܵ��½����ع����Ӷ��ظ�ʹ�õ��첽���ݣ���DataHolder��ִ�й�����ȴ�������ظ�ִ�У�����ִ��ʱҪ����������е��첽�������ǰ��ִ�����Ҫ����Ϊǿ����
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
                                // ���½���
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
                    // �ٴ�publish���������ܱ�֤ͳһ��Ϊ�����ò��������ڸ��½���publishProgress���ܵ��µĽ����ع�֮��
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
                    Object genericAdapter = mGenericAdapterRef.get();
                    if (genericAdapter == null)
                        return;
                    if (mNotifyAsyncDataForAll)
                    {
                        if(genericAdapter instanceof GenericAdapter)
                            ((GenericAdapter)genericAdapter).notifyDataSetChanged();
                        else if(genericAdapter instanceof GenericExpandableListAdapter)
                            ((GenericExpandableListAdapter)genericAdapter).notifyDataSetChanged();
                    } else
                    {
                        AdapterView<? extends Adapter> adapterView = mAdapterViewRef.get();
                        if (adapterView == null)
                            return;
                        DataHolder holder = (DataHolder) values[0];
                        int position = holder.mExecuteConfig.mPosition;
                        int wholePosition = position;
                        if(genericAdapter instanceof GenericAdapter)
                        {
                            GenericAdapter adapter = (GenericAdapter)genericAdapter;
                            if (position >= adapter.getCount())
                                return; // DataHolder��ִ�о���ζ��Adapter��AdapterView�Ѿ�ͬ������ʱ���ж���Adapter�ı仯�����ɽ�����еĲ�һ������
                            if (!holder.equals(adapter.queryDataHolder(position)))
                                return; // DataHolder��ִ�о���ζ��Adapter��AdapterView�Ѿ�ͬ������ʱ���ж���Adapter�ı仯�����ɽ�����еĲ�һ������
                        }else if(genericAdapter instanceof GenericExpandableListAdapter)
                        {
                            GenericExpandableListAdapter adapter = (GenericExpandableListAdapter)genericAdapter;
                            int groupPos = holder.mExecuteConfig.mGroupPosition;
                            if(groupPos == -1)
                            {
                                if (position >= adapter.getGroupCount())
                                    return; // DataHolder��ִ�о���ζ��Adapter��AdapterView�Ѿ�ͬ������ʱ���ж���Adapter�ı仯�����ɽ�����еĲ�һ������
                                if (!holder.equals(adapter.queryDataHolder(position)))
                                    return; // DataHolder��ִ�о���ζ��Adapter��AdapterView�Ѿ�ͬ������ʱ���ж���Adapter�ı仯�����ɽ�����еĲ�һ������
                                wholePosition = (int)adapter.getCombinedGroupId(position);
                            }else
                            {
                                if(groupPos >= adapter.getGroupCount())
                                    return; // DataHolder��ִ�о���ζ��Adapter��AdapterView�Ѿ�ͬ������ʱ���ж���Adapter�ı仯�����ɽ�����еĲ�һ������
                                GroupDataHolder group = adapter.queryDataHolder(groupPos);
                                if (position >= group.getChildrenCount())
                                    return; // DataHolder��ִ�о���ζ��Adapter��AdapterView�Ѿ�ͬ������ʱ���ж���Adapter�ı仯�����ɽ�����еĲ�һ������
                                if (!holder.equals(group.queryChild(position)))
                                    return; // DataHolder��ִ�о���ζ��Adapter��AdapterView�Ѿ�ͬ������ʱ���ж���Adapter�ı仯�����ɽ�����еĲ�һ������
                                wholePosition = (int)adapter.getCombinedChildId(groupPos,position);
                            }
                        }
                        if (adapterView instanceof ListView)
                            wholePosition = wholePosition + ((ListView) adapterView).getHeaderViewsCount();
                        int first = adapterView.getFirstVisiblePosition();
                        int last = adapterView.getLastVisiblePosition();
                        if (wholePosition >= first && wholePosition <= last)
                            holder.onAsyncDataExecuted(adapterView.getContext(), position, adapterView.getChildAt(wholePosition - first), values[1], (Integer) values[2]);
                    }
                }
            }
        };
    }

    /**
     * <p>�����첽���ݵĻص����� <p>���׳��κ��쳣���׳��쳣ʱ���ⲿ����Ϊ��ǰ���첽����ִ��ʧ��
     * 
     * @param position ����AdapterView�е�λ��
     * @param dataHolder ����AdapterView��DataHolder����
     * @param asyncDataIndex ��Ҫ���ص�DataHolder���첽���ݵ�����
     * @return ִ�к�õ��Ľ��
     * @throws Exception
     */
    public abstract Object onExecute(int position, DataHolder dataHolder, int asyncDataIndex) throws Exception;

    private static class PushTask extends OptionalExecutorTask<Object, Integer, Object>
    {
        private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

        @Override
        protected Object doInBackground(Object... params)
        {
            while(true)
            {
                try
                {
                    /**
                     * �첽�����߳̿�������ͨ�߳��г�ʼ����������ԭ���ǣ�
                     * 1.���첽�����߳�ͬ��ʹ��OptionalExecutorTask��PushTask��ʱ�Ѿ������̳߳�ʼ���˾�̬��Handler
                     * 2.�첽�����̲߳�����onPreExecute()���߼�������������ͨ�߳���ִ�п��ܵ��µĴ���
                     */
                    queue.poll(Long.MAX_VALUE,TimeUnit.SECONDS).run();
                }catch(InterruptedException e)
                {
                }
            }
        }

        public void execPushingAsync(final AsyncDataExecutor executor, final DataHolder dataHolder)
        {
            queue.offer(new Runnable() {
                @Override
                public void run() {
                    executor.push(dataHolder);
                }
            });
        }
    }

}
