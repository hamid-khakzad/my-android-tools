package cn.emagsoftware.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;

/**
 * <p>此类适用于执行依赖于特定数据(尤其为大数据)的异步操作且依赖数据被回收后任务可取消的情况，该类内部对依赖数据使用了虚引用，不妨碍其被回收
 * 
 * @author Wendell
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class AsyncWeakTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{

    private List<WeakReference<Object>> mObjReferences = null;
    private Handler                     mHandler       = new Handler();
    private boolean                     mIsRecycled    = false;

    public AsyncWeakTask(Object... objs)
    {
        mObjReferences = new ArrayList<WeakReference<Object>>(objs.length);
        for (Object obj : objs)
        {
            if (obj == null)
                throw new NullPointerException();
            mObjReferences.add(new WeakReference<Object>(obj));
        }
    }

    private boolean cancelWhenRecycled(boolean mayInterruptIfRunning)
    {
        mIsRecycled = true;
        return cancel(mayInterruptIfRunning);
    }

    protected void onPreExecute(Object[] objs)
    {
    }

    protected abstract Result doInBackgroundImpl(Params... params) throws Exception;

    protected void onProgressUpdate(Object[] objs, Progress... values)
    {
    }

    protected void onCancelled(Object[] objs)
    {
    }

    protected void onPostExecute(Object[] objs, Result result)
    {
    }

    protected void onException(Object[] objs, Exception e)
    {
    }

    @Override
    protected final void onPreExecute()
    {
        Object[] objs = getObjects();
        if (objs == null)
            cancelWhenRecycled(true);
        else
            onPreExecute(objs);
    }

    @Override
    protected final Result doInBackground(Params... params)
    {
        try
        {
            return doInBackgroundImpl(params);
        } catch (final Exception e)
        {
            if (e instanceof InterruptedException && mIsRecycled)
            { // cancel(true)可能导致InterruptedException，但内部cancelWhenRecycled导致该异常时不能回调onException。若用户后续调用cancel(true)导致了该异常，由于依赖数据已被回收，同样不会执行onException，所以这里的直接返回对该情况同样适用
                return null; // 可直接返回，因为mIsRecycled为true时onCancelled不会被实质执行
            }
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    Object[] objs = getObjects();
                    if (objs != null)
                        onException(objs, e);
                }
            });
            cancelWhenRecycled(false); // 在内部调用并传true时会打印出InterruptedException，这里传false避免之，尽管该异常不影响结果
            return null;
        }
    }

    @Override
    protected final void onProgressUpdate(Progress... values)
    {
        Object[] objs = getObjects();
        if (objs == null)
            cancelWhenRecycled(true);
        else
            onProgressUpdate(objs, values);
    }

    @Override
    protected final void onCancelled()
    {
        if (mIsRecycled)
            return;
        Object[] objs = getObjects();
        if (objs != null)
            onCancelled(objs);
    }

    @Override
    protected final void onPostExecute(Result result)
    {
        Object[] objs = getObjects();
        if (objs != null)
            onPostExecute(objs, result);
    }

    private Object[] getObjects()
    {
        Object[] objs = new Object[mObjReferences.size()];
        Iterator<WeakReference<Object>> objIterator = mObjReferences.iterator();
        for (int i = 0; i < objs.length; i++)
        {
            objs[i] = objIterator.next().get();
            if (objs[i] == null)
                return null; // 只要有一个Object被回收，就返回null，因为传出的Object个数若和传入的不一致，可能带来一系列问题
        }
        return objs;
    }

}
