package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

import android.content.Context;

public abstract class BaseLoadAdapter extends GenericAdapter
{

    /** 是否正在加载 */
    protected boolean    mIsLoading    = false;
    /** 是否已经加载过 */
    protected boolean    mIsLoaded     = false;
    /** 当前的加载是否发生了异常 */
    protected boolean    mIsException  = false;
    /** 当前的加载条件 */
    protected Object     mCurCondition = null;
    /** 加载时的回调对象 */
    private LoadCallback mCallback     = null;

    BaseLoadAdapter(Context context)
    {
        super(context);
    }

    public BaseLoadAdapter(Context context, BaseLoadAdapter.LoadCallback callback)
    {
        super(context);
        if (callback == null)
            throw new NullPointerException();
        mCallback = callback;
    }

    public LoadCallback getLoadCallback()
    {
        return mCallback;
    }

    /**
     * <p>加载的执行方法
     * 
     * @param condition 加载时需要的条件，没有时可传null
     * @return true表示开始加载；false表示已经在加载，本次的调用无效
     */
    public boolean load(final Object condition)
    {
        if (mIsLoading)
            return false;
        mIsLoading = true;
        mCurCondition = condition;
        onBeginLoad(mContext, condition);
        new AsyncWeakTask<Object, Integer, Object>(this)
        {
            @Override
            protected Object doInBackground(Object... params)
            {
                try
                {
                    return mCallback.onLoad(condition);
                } catch (Exception e)
                {
                    return e;
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                if (result instanceof Exception)
                {
                    Exception e = (Exception) result;
                    LogManager.logE(BaseLoadAdapter.class, "Execute loading failed.", e);
                    adapter.mIsLoading = false;
                    adapter.mIsException = true;
                    adapter.onAfterLoad(adapter.mContext, condition, e);
                } else
                {
                    List<DataHolder> resultList = (List<DataHolder>) result;
                    if (resultList != null && resultList.size() > 0)
                        adapter.addDataHolders(resultList); // 该方法需在UI线程中执行且是非线程安全的
                    adapter.mIsLoading = false;
                    adapter.mIsLoaded = true;
                    adapter.mIsException = false;
                    adapter.onAfterLoad(adapter.mContext, condition, null);
                }
            }
        }.execute("");
        return true;
    }

    /**
     * <p>获取当前的加载条件
     * 
     * @return
     */
    public Object getCurCondition()
    {
        return mCurCondition;
    }

    /**
     * <p>是否正在加载
     * 
     * @return
     */
    public boolean isLoading()
    {
        return mIsLoading;
    }

    /**
     * <p>是否已经加载过
     * 
     * @return
     */
    public boolean isLoaded()
    {
        return mIsLoaded;
    }

    /**
     * <p>当前的加载是否发生了异常
     * 
     * @return
     */
    public boolean isException()
    {
        return mIsException;
    }

    /**
     * <p>在加载之前的回调方法，可以显示一些loading之类的字样。如对于ListView，可以通过addFooterView方法添加一个正在加载的提示
     * 
     * @param context
     * @param condition
     */
    protected abstract void onBeginLoad(Context context, Object condition);

    /**
     * <p>加载完成后的回调方法，可以通过判断exception是否为null来获悉加载成功与否，从而给用户一些提示
     * 
     * @param context
     * @param condition
     * @param exception
     */
    protected abstract void onAfterLoad(Context context, Object condition, Exception exception);

    public abstract static class LoadCallback
    {

        /**
         * <p>加载的具体实现，该方法将在非UI线程中执行，要注意不能执行UI的操作
         * 
         * @param condition
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object condition) throws Exception;

    }

}
