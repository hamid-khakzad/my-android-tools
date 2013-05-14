package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

import android.content.Context;

public abstract class BaseLoadAdapter extends GenericAdapter
{

    /** 是否正在加载 */
    boolean              mIsLoading   = false;
    /** 是否已经加载过 */
    boolean              mIsLoaded    = false;
    /** 当前的加载是否发生了异常 */
    boolean              mIsException = false;
    /** 额外数据 */
    Object               mExtra       = null;
    /** 加载时的回调对象 */
    private LoadCallback mCallback    = null;

    BaseLoadAdapter(Context context, int viewTypeCount)
    {
        super(context, viewTypeCount);
    }

    public BaseLoadAdapter(Context context, BaseLoadAdapter.LoadCallback callback)
    {
        this(context, callback, 1);
    }

    public BaseLoadAdapter(Context context, BaseLoadAdapter.LoadCallback callback, int viewTypeCount)
    {
        super(context, viewTypeCount);
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
     * @return true表示开始加载；false表示已经在加载，本次的调用无效
     */
    public boolean load()
    {
        if (mIsLoading)
            return false;
        mIsLoading = true;
        new AsyncWeakTask<Object, Integer, Object>(this)
        {
            @Override
            protected void onPreExecute(Object[] objs)
            {
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                adapter.onBeginLoad(adapter.mContext, mExtra);
            }

            @Override
            protected Object doInBackgroundImpl(Object... params) throws Exception
            {
                return mCallback.onLoad(mExtra);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                List<DataHolder> resultList = (List<DataHolder>) result;
                if (resultList != null && resultList.size() > 0)
                    adapter.addDataHolders(resultList); // 该方法需在UI线程中执行且是非线程安全的
                adapter.mIsLoading = false;
                adapter.mIsLoaded = true;
                adapter.mIsException = false;
                adapter.onAfterLoad(adapter.mContext, mExtra, null);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                LogManager.logE(BaseLoadAdapter.class, "Execute loading failed.", e);
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                adapter.onAfterLoad(adapter.mContext, mExtra, e);
            }
        }.execute("");
        return true;
    }

    /**
     * <p>设置额外数据
     * 
     * @param extra
     */
    public void setExtra(Object extra)
    {
        if (extra == null)
            throw new NullPointerException();
        this.mExtra = extra;
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
     * @param extra
     */
    protected abstract void onBeginLoad(Context context, Object extra);

    /**
     * <p>加载完成后的回调方法，可以通过判断exception是否为null来获悉加载成功与否，从而给用户一些提示
     * 
     * @param context
     * @param extra
     * @param exception
     */
    protected abstract void onAfterLoad(Context context, Object extra, Exception exception);

    public abstract static class LoadCallback
    {

        /**
         * <p>加载的具体实现，该方法将在非UI线程中执行，要注意不能执行UI的操作
         * 
         * @param extra
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object extra) throws Exception;

    }

}
