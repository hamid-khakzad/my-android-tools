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

    private List<WeakReference<Object>> mObjReferences             = null;
    private Handler                     mHandler                   = new Handler();
    private boolean                     mShouldCallbackOnCancelled = true;

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
        mShouldCallbackOnCancelled = false;
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
            cancelWhenRecycled(true);
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
        if (!mShouldCallbackOnCancelled)
            return;
        Object[] objs = getObjects();
        if (objs != null)
            onCancelled(objs);
    }

    @Override
    protected final void onPostExecute(Result result)
    {
        Object[] objs = getObjects();
        if (objs == null)
            cancelWhenRecycled(true);
        else
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
