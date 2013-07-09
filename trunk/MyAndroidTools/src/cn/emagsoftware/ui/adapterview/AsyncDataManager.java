package cn.emagsoftware.ui.adapterview;

import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.WrapperListAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.OptionalExecutorTask;

/**
 * Created by Wendell on 13-7-8.
 */
public final class AsyncDataManager {

    private static Executor EXECUTOR = new ThreadPoolExecutor(0,5,30, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),new ThreadPoolExecutor.CallerRunsPolicy());

    private AsyncDataManager(){}

    public static void computeAsyncData(AdapterView<? extends Adapter> view,AsyncDataExecutor executor)
    {
        if(view == null || executor == null)
            throw new NullPointerException();
        Object adapter = null;
        if(view instanceof ExpandableListView)
        {
            adapter = ((ExpandableListView)view).getExpandableListAdapter();
        }else
        {
            adapter = view.getAdapter();
            if(adapter instanceof WrapperListAdapter)
                adapter = ((WrapperListAdapter)adapter).getWrappedAdapter();
        }
        if(adapter == null)
            throw new IllegalStateException("please call this method after 'setAdapter(adapter)' is called.");
        int firstPos = view.getFirstVisiblePosition();
        int lastPos = view.getLastVisiblePosition();
        List<DataHolder> holders = new ArrayList<DataHolder>();
        if(adapter instanceof GenericAdapter)
        {
            GenericAdapter adapterPoint = (GenericAdapter)adapter;
            if(view instanceof ListView)
            {
                int headerCount = ((ListView)view).getHeaderViewsCount();
                firstPos = firstPos - headerCount;
                lastPos = lastPos - headerCount;
            }
            if(firstPos < 0)
                firstPos = 0;
            int count = adapterPoint.getCount();
            if(++lastPos > count)
                lastPos = count;
            if(firstPos <= lastPos)
                holders = adapterPoint.queryDataHolders(firstPos,lastPos);
        }else if(adapter instanceof GenericExpandableListAdapter)
        {
            GenericExpandableListAdapter adapterPoint = (GenericExpandableListAdapter)adapter;
            ExpandableListView expandableView = (ExpandableListView)view;
            for(int i = firstPos;i <= lastPos;i++)
            {
                long packedPos = expandableView.getExpandableListPosition(i);
                int groupPos = ExpandableListView.getPackedPositionGroup(packedPos);
                int childPos = ExpandableListView.getPackedPositionChild(packedPos);
                if(groupPos == -1)
                    continue;
                DataHolder holder = adapterPoint.queryDataHolder(groupPos);
                if(childPos != -1)
                    holder = ((GroupDataHolder)holder).queryChild(childPos);
                holders.add(holder);
            }
        }else
        {
            throw new IllegalStateException("the adapter for AdapterView can only be GenericAdapter or GenericExpandableListAdapter.");
        }
        if(holders.size() == 0)
            return;










    }

    private static class ExecuteRunnable implements Runnable
    {

        private WeakReference<AdapterView<? extends Adapter>> viewRef = null;
        private WeakReference<Object> adapterRef = null;
        private List<DataHolder> holders = null;
        private AsyncDataExecutor executor = null;
        private boolean isCancelled = false;

        public ExecuteRunnable(AdapterView<? extends Adapter> view,Object adapter,List<DataHolder> holders,AsyncDataExecutor executor)
        {
            this.viewRef = new WeakReference<AdapterView<? extends Adapter>>(view);
            this.adapterRef = new WeakReference<Object>(adapter);
            this.holders = holders;
            this.executor = executor;
        }

        @Override
        public void run()
        {
            for(DataHolder holder:holders)
            {
                if(isCancelled)
                    return;
                AdapterView<? extends Adapter> view = viewRef.get();
                Object adapter = adapterRef.get();
                if(view == null || adapter == null)
                    return;
                if(!holder.mExecuteConfig.mShouldExecute)
                    continue;
                if(holder.mExecuteConfig.mIsExecuting)
                    continue;
                holder.mExecuteConfig.mIsExecuting = true;
                ExecuteTask task = new ExecuteTask(view,adapter,holder,executor);
                view = null;
                adapter = null;
                // 采用的ThreadPoolExecutor.CallerRunsPolicy线程池策略可能会堵塞当前线程，故当前操作要在子线程执行，且view和adapter要提前置为null
                task.executeOnExecutor(EXECUTOR);
            }
        }

        public void cancel()
        {
            isCancelled = true;
        }

    }

    private static class ExecuteTask extends OptionalExecutorTask<Object,Object,Object>
    {

        private WeakReference<AdapterView<? extends Adapter>> viewRef = null;
        private WeakReference<Object> adapterRef = null;
        private DataHolder holder = null;
        private AsyncDataExecutor executor = null;

        public ExecuteTask(AdapterView<? extends Adapter> view,Object adapter,DataHolder holder,AsyncDataExecutor executor)
        {
            this.viewRef = new WeakReference<AdapterView<? extends Adapter>>(view);
            this.adapterRef = new WeakReference<Object>(adapter);
            this.holder = holder;
            this.executor = executor;
        }

        @Override
        protected Object doInBackground(Object... params) {
            boolean shouldChangeSoftRefByPublish = false;
            // 更新界面publishProgress可能导致界面重构，从而重复使用到异步数据，但DataHolder在执行过程中却不允许重复执行，所以执行时要遍历检查所有的异步项，并且先前已执行完的要升级为强引用
            for (int i = 0; i < holder.getAsyncDataCount(); i++)
            {
                Object curAsyncData = holder.getAsyncData(i);
                if (curAsyncData == null)
                {
                    try
                    {
                        Object asyncData = executor.onExecute(holder.mExecuteConfig.mPosition, holder, i);
                        if (asyncData == null)
                            throw new NullPointerException("the method 'AsyncDataExecutor.onExecute' returns null");
                        holder.setAsyncData(i, asyncData);
                        // 更新界面
                        publishProgress(asyncData, i);
                        shouldChangeSoftRefByPublish = true;
                    } catch (Exception e)
                    {
                        LogManager.logE(AsyncDataManager.class, "execute async data failed(position:" + holder.mExecuteConfig.mPosition + ",index:" + i + ")", e);
                    }
                } else
                {
                    holder.setAsyncData(i, curAsyncData);
                }
            }
            holder.mExecuteConfig.mIsExecuting = false;
            if (shouldChangeSoftRefByPublish)
            {
                // 统一置为软引用可能需要通过publish方式来进行，因为需要保证该操作发生在更新界面publishProgress可能导致的界面重构之后
                publishProgress();
            } else
            {
                for (int i = 0; i < holder.getAsyncDataCount(); i++)
                {
                    holder.changeAsyncDataToSoftReference(i);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            if(values.length == 0)
            {
                // 再次publish，这样才能保证统一置为软引用操作发生在更新界面publishProgress可能导致的界面重构之后
                publishProgress("");
            }else if(values.length == 1)
            {
                for (int i = 0; i < holder.getAsyncDataCount(); i++)
                {
                    holder.changeAsyncDataToSoftReference(i);
                }
            }else
            {
                Object adapterObj = adapterRef.get();
                if (adapterObj == null)
                    return;
                if (executor.isNotifyAsyncDataForAll())
                {
                    if(adapterObj instanceof GenericAdapter)
                        ((GenericAdapter)adapterObj).notifyDataSetChanged();
                    else if(adapterObj instanceof GenericExpandableListAdapter)
                        ((GenericExpandableListAdapter)adapterObj).notifyDataSetChanged();
                } else
                {
                    AdapterView<? extends Adapter> adapterView = viewRef.get();
                    if (adapterView == null)
                        return;
                    int position = holder.mExecuteConfig.mPosition;
                    int wholePosition = -1;
                    if(adapterObj instanceof GenericAdapter)
                    {
                        GenericAdapter adapter = (GenericAdapter)adapterObj;
                        if (position >= adapter.getCount())
                            return; // DataHolder被执行就意味着Adapter和AdapterView已经同步，此时再判断下Adapter的变化，即可解决所有的不一致问题
                        if (!holder.equals(adapter.queryDataHolder(position)))
                            return; // DataHolder被执行就意味着Adapter和AdapterView已经同步，此时再判断下Adapter的变化，即可解决所有的不一致问题
                        wholePosition = position;
                        if (adapterView instanceof ListView)
                            wholePosition = wholePosition + ((ListView) adapterView).getHeaderViewsCount();
                    }else if(adapterObj instanceof GenericExpandableListAdapter)
                    {
                        GenericExpandableListAdapter adapter = (GenericExpandableListAdapter)adapterObj;
                        int groupPos = holder.mExecuteConfig.mGroupPosition;
                        long packedPosition = -1;
                        if(groupPos == -1)
                        {
                            if (position >= adapter.getGroupCount())
                                return; // DataHolder被执行就意味着Adapter和AdapterView已经同步，此时再判断下Adapter的变化，即可解决所有的不一致问题
                            if (!holder.equals(adapter.queryDataHolder(position)))
                                return; // DataHolder被执行就意味着Adapter和AdapterView已经同步，此时再判断下Adapter的变化，即可解决所有的不一致问题
                            packedPosition = ExpandableListView.getPackedPositionForGroup(position);
                        }else
                        {
                            if(groupPos >= adapter.getGroupCount())
                                return; // DataHolder被执行就意味着Adapter和AdapterView已经同步，此时再判断下Adapter的变化，即可解决所有的不一致问题
                            GroupDataHolder group = adapter.queryDataHolder(groupPos);
                            if (position >= group.getChildrenCount())
                                return; // DataHolder被执行就意味着Adapter和AdapterView已经同步，此时再判断下Adapter的变化，即可解决所有的不一致问题
                            if (!holder.equals(group.queryChild(position)))
                                return; // DataHolder被执行就意味着Adapter和AdapterView已经同步，此时再判断下Adapter的变化，即可解决所有的不一致问题
                            packedPosition = ExpandableListView.getPackedPositionForChild(groupPos,position);
                        }
                        wholePosition = ((ExpandableListView)adapterView).getFlatListPosition(packedPosition);
                    }
                    int first = adapterView.getFirstVisiblePosition();
                    int last = adapterView.getLastVisiblePosition();
                    if (wholePosition >= first && wholePosition <= last)
                        holder.onAsyncDataExecuted(adapterView.getContext(), position, adapterView.getChildAt(wholePosition - first), values[0], (Integer) values[1]);
                }
            }
        }

    }

    private static class PushTask extends OptionalExecutorTask<Object,Object,Object>
    {

        private LinkedList<ExecuteRunnable> runnables = new LinkedList<ExecuteRunnable>();

        @Override
        protected Object doInBackground(Object... params) {
            while(true)
            {
                ExecuteRunnable curRunnable = null;
                synchronized (runnables)
                {
                    if(runnables.size() > 0)
                        curRunnable = runnables.removeFirst();
                }
                if(curRunnable == null)
                {

                }else
                {
                    curRunnable.run();
                }
            }
        }

        public void push(ExecuteRunnable runnable)
        {
            synchronized (runnables)
            {
                runnables.addFirst(runnable);
            }
        }

    }

}
