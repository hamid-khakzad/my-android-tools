package cn.emagsoftware.ui.adapterview;

import android.graphics.Bitmap;
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

    private static PushTask PUSH_TASK = new PushTask();
    static
    {
        PUSH_TASK.execute();
    }
    private static Executor EXECUTOR = new ThreadPoolExecutor(0,5,45, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),new ThreadPoolExecutor.CallerRunsPolicy());
    private static AdapterViewWrapper WRAPPER = null;

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
            if(lastPos >= count)
                lastPos = count - 1;
            for(int i = firstPos;i <= lastPos;i++)
            {
                DataHolder holder = adapterPoint.queryDataHolder(i);
                if(holder.mExecuteConfig.mShouldExecute)
                    holders.add(holder);
            }
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
                if(holder.mExecuteConfig.mShouldExecute)
                    holders.add(holder);
            }
        }else
        {
            throw new IllegalStateException("the adapter for AdapterView can only be GenericAdapter or GenericExpandableListAdapter.");
        }
        if(holders.size() == 0)
            return;
        if(WRAPPER == null || WRAPPER.getAdapterView() != view)
            WRAPPER = new AdapterViewWrapper(view);
        else
            WRAPPER.getExecuteRunnable().cancel();
        ExecuteRunnable runnable = new ExecuteRunnable(view,adapter,holders,executor);
        PUSH_TASK.push(runnable);
        WRAPPER.setExecuteRunnable(runnable);
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
                if(holder.mExecuteConfig.mIsExecuting)
                    continue;
                holder.mExecuteConfig.mIsExecuting = true;
                // 由于线程池策略可能导致阻塞，所以当前操作会安排在子线程执行，由于同样使用OptionalExecutorTask的PushTask此时已经在主线程初始化了静态的Handler，所以ExecuteTask不会因为Handler报错，且能保证onProgressUpdate在主线程被执行
                ExecuteTask task = new ExecuteTask(view,adapter,holder,executor);
                // 提前置为null，因为下面可能会阻塞
                view = null;
                adapter = null;
                // 未实现onPreExecute()，所以安排在子线程执行无影响
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
            for (int i = 0; i < holder.getAsyncDataCount(); i++)
            {
                String globalId = holder.asyncDataShouldExecute(i);
                if (globalId != null)
                {
                    try
                    {
                        Object asyncData = executor.onExecute(holder.mExecuteConfig.mPosition, holder, i, globalId);
                        if (asyncData == null)
                            throw new NullPointerException("'AsyncDataExecutor.onExecute' can not return null");
                        if(!(asyncData instanceof Bitmap) && !(asyncData instanceof byte[]))
                            throw new UnsupportedOperationException("'AsyncDataExecutor.onExecute' should return only Bitmap or byte[]");
                        holder.setAsyncData(globalId, asyncData);
                        publishProgress(asyncData, i);
                    } catch (Exception e)
                    {
                        LogManager.logE(AsyncDataManager.class, "execute async data failed(position:" + holder.mExecuteConfig.mPosition + ",index:" + i + ")", e);
                    }
                }
            }
            holder.mExecuteConfig.mIsExecuting = false;
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
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
                    try
                    {
                        Thread.sleep(100);
                    }catch (InterruptedException e)
                    {
                    }
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

    private static class AdapterViewWrapper
    {

        private WeakReference<AdapterView<? extends Adapter>> viewRef = null;
        private ExecuteRunnable runnable = null;

        public AdapterViewWrapper(AdapterView<? extends Adapter> view)
        {
            this.viewRef = new WeakReference<AdapterView<? extends Adapter>>(view);
        }

        public AdapterView<? extends Adapter> getAdapterView()
        {
            return viewRef.get();
        }

        public void setExecuteRunnable(ExecuteRunnable runnable)
        {
            if(runnable == null) throw new NullPointerException();
            this.runnable = runnable;
        }

        public ExecuteRunnable getExecuteRunnable()
        {
            return runnable;
        }

    }

}
