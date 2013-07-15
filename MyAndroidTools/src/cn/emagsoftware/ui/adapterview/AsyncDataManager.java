package cn.emagsoftware.ui.adapterview;

import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.OptionalExecutorTask;

/**
 * Created by Wendell on 13-7-8.
 */
final class AsyncDataManager {

    private static PushTask PUSH_TASK = new PushTask();
    static
    {
        PUSH_TASK.execute();
    }
    private static Executor EXECUTOR = new ThreadPoolExecutor(0,5,45, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),new ThreadPoolExecutor.CallerRunsPolicy());

    private AsyncDataManager(){}

    public static void push(AdapterView<? extends Adapter> view,Object adapter,DataHolder holder,AsyncDataExecutor executor)
    {
        TreeMap<Integer,ExecuteRunnable> map = null;
        try
        {
            map = (TreeMap<Integer,ExecuteRunnable>)view.getTag();
        }catch (ClassCastException e)
        {
            throw new IllegalStateException("can not use 'AdapterView.setTag(tag)' outside when you call 'bindAsyncDataExecutor(executor)'");
        }
        int pos;
        if(adapter instanceof GenericAdapter)
        {
            pos = holder.mExecuteConfig.mPosition;
        }else
        {
            int groupPos = holder.mExecuteConfig.mGroupPosition;
            if(groupPos == -1)
                pos = ((ExpandableListView)view).getFlatListPosition(ExpandableListView.getPackedPositionForGroup(holder.mExecuteConfig.mPosition));
            else
                pos = ((ExpandableListView)view).getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPos,holder.mExecuteConfig.mPosition));
        }
        ExecuteRunnable runnable = new ExecuteRunnable(view,adapter,holder,executor);
        if(map == null)
        {
            map = new TreeMap<Integer, ExecuteRunnable>();
            map.put(pos,runnable);
            view.setTag(map);
        }else
        {
            int firstPos = map.firstKey();
            int lastPos = map.lastKey();
            if(pos < firstPos)
            {
                map.put(pos,runnable);
                int visibleCount = view.getLastVisiblePosition() - view.getFirstVisiblePosition() + 2; // AdapterView在第一次布局显示时可能需要加2，所以这里统一加2
                int excessCount = lastPos - pos + 1 - visibleCount; // 不能使用map.size() - visibleCount，因为GridView向上滑动时position的变化可能是3,4,5->0,1,2
                while(excessCount > 0)
                {
                    map.remove(map.lastKey()).cancel();
                    excessCount--;
                }
            }else if(pos > lastPos)
            {
                map.put(pos,runnable);
                int visibleCount = view.getLastVisiblePosition() - view.getFirstVisiblePosition() + 2; // AdapterView在第一次布局显示时可能需要加2，所以这里统一加2
                int excessCount = pos - firstPos + 1 - visibleCount; // 与上面保持一致的写法
                while(excessCount > 0)
                {
                    map.remove(map.firstKey()).cancel();
                    excessCount--;
                }
            }else
            {
                ExecuteRunnable oldRunnable = map.remove(pos);
                if(oldRunnable != null) // 可能为null，因为GridView向上滑动时position的变化可能是3,4,5->0,1,2
                    oldRunnable.cancel();
                map.put(pos,runnable);
            }
        }
        if(!holder.mExecuteConfig.mShouldExecute)
            return;
        PUSH_TASK.push(runnable);
    }

    private static class ExecuteRunnable implements Runnable
    {

        private WeakReference<AdapterView<? extends Adapter>> viewRef = null;
        private WeakReference<Object> adapterRef = null;
        private DataHolder holder = null;
        private AsyncDataExecutor executor = null;
        private boolean isCancelled = false;

        public ExecuteRunnable(AdapterView<? extends Adapter> view,Object adapter,DataHolder holder,AsyncDataExecutor executor)
        {
            this.viewRef = new WeakReference<AdapterView<? extends Adapter>>(view);
            this.adapterRef = new WeakReference<Object>(adapter);
            this.holder = holder;
            this.executor = executor;
        }

        @Override
        public void run()
        {
            if(isCancelled)
                return;
            AdapterView<? extends Adapter> view = viewRef.get();
            Object adapter = adapterRef.get();
            if(view == null || adapter == null)
                return;
            if(holder.mExecuteConfig.mIsExecuting)
                return;
            holder.mExecuteConfig.mIsExecuting = true;
            // 由于线程池策略可能导致阻塞，所以当前操作会安排在子线程执行，由于同样使用OptionalExecutorTask的PushTask此时已经在主线程初始化了静态的Handler，所以ExecuteTask不会因为Handler报错，且能保证onProgressUpdate在主线程被执行
            ExecuteTask task = new ExecuteTask(view,adapter,holder,executor);
            // 提前置为null，因为下面可能会阻塞
            view = null;
            adapter = null;
            // 未实现onPreExecute()，所以安排在子线程执行无影响
            task.executeOnExecutor(EXECUTOR);
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
                            throw new NullPointerException("the method 'AsyncDataExecutor.onExecute(position,dataHolder,asyncDataIndex)' returns null");
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

}
