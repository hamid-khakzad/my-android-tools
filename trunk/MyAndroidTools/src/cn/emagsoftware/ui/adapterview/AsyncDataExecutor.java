package cn.emagsoftware.ui.adapterview;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import cn.emagsoftware.util.OptionalExecutorTask;

public abstract class AsyncDataExecutor
{

    private static PushTask                               PUSH_TASK              = new PushTask();
    static
    {
        PUSH_TASK.execute();
    }

    /**
     * <p>加载异步数据的回调方法 <p>可抛出任何异常，抛出异常时，外部会认为当前的异步数据执行失败
     * 
     * @param position 所在Adapter中的位置
     * @param dataHolder 用于Adapter的DataHolder对象
     * @param asyncDataIndex 需要加载的DataHolder中异步数据的索引
     * @return 执行后得到的结果
     * @throws Exception
     */
    public abstract Object onExecute(int position, DataHolder dataHolder, int asyncDataIndex) throws Exception;

    /**
     * <p>如果需要在加载完异步数据后通知使用了当前AdapterView的Adapter的所有AdapterView，则可以覆盖该方法并返回true，但不要轻易这么做，因为会影响到性能</>
     * @return
     */
    public boolean isNotifyAsyncDataForAll()
    {
        return false;
    }

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
                     * 异步数据线程可以在普通线程中初始化和启动的原因是：
                     * 1.与异步数据线程同样使用OptionalExecutorTask的PushTask此时已经在主线程初始化了静态的Handler
                     * 2.异步数据线程不存在onPreExecute()的逻辑，避免了在普通线程中执行可能导致的错误
                     */
                    queue.poll(Long.MAX_VALUE,TimeUnit.SECONDS).run();
                }catch(InterruptedException e)
                {
                }
            }
        }

    }

}
