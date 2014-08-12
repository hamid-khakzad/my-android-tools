package cn.emagsoftware.ui.adapterview;

import android.content.Context;

import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

public class LoadAdapter extends GenericAdapter
{

    /** 是否正在加载 */
    boolean              mIsLoading   = false;
    /** 是否已经加载过 */
    boolean              mIsLoaded    = false;
    /** 当前的加载是否发生了异常 */
    boolean              mIsException = false;
    /** 参数数据 */
    Object               mParam       = null;
    /** 加载时的回调对象 */
    private LoadCallback mCallback    = null;
    AsyncWeakTask<Object, Integer, Object> mTask = null;

    LoadAdapter(Context context, int viewTypeCount)
    {
        super(context, viewTypeCount);
    }

    public LoadAdapter(Context context, LoadCallback callback)
    {
        this(context, callback, 1);
    }

    public LoadAdapter(Context context, LoadCallback callback, int viewTypeCount)
    {
        super(context, viewTypeCount);
        if (callback == null)
            throw new NullPointerException();
        mCallback = callback;
    }

    /**
     * <p>加载的执行方法</>
     * @param result
     * @return
     */
    public boolean load(LoadResult result)
    {
        if(result == null) throw new NullPointerException("result == null");
        if (mIsLoading)
            return false;
        mIsLoading = true;
        mTask = createTask(this,result);
        mTask.execute();
        return true;
    }

    public boolean cancelLoad() {
        if(!mIsLoading) return false;
        mTask.cancel(true);
        mTask = null;
        mIsLoading = false;
        return true;
    }

    private static AsyncWeakTask<Object, Integer, Object> createTask(LoadAdapter adapter,LoadResult result) {
        final LoadCallback loader = adapter.mCallback;
        final Object param = adapter.mParam;
        return new AsyncWeakTask<Object, Integer, Object>(adapter,result)
        {
            @Override
            protected Object doInBackgroundImpl(Object... params) throws Exception
            {
                return loader.onLoad(param);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                LoadAdapter adapter = (LoadAdapter) objs[0];
                LoadResult loadRst = (LoadResult)objs[1];
                List<DataHolder> resultList = (List<DataHolder>) result;
                if (resultList != null && resultList.size() > 0)
                    adapter.addDataHolders(resultList); // 该方法需在UI线程中执行且是非线程安全的
                adapter.mIsLoading = false;
                adapter.mIsLoaded = true;
                adapter.mIsException = false;
                loadRst.onSuccess(adapter.mContext,param,loader);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                LogManager.logE(LoadAdapter.class, "Execute loading failed.", e);
                LoadAdapter adapter = (LoadAdapter) objs[0];
                LoadResult loadRst = (LoadResult)objs[1];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                loadRst.onException(adapter.mContext,param,loader,e);
            }
        };
    }

    /**
     * <p>设置参数数据
     * 
     * @param param
     */
    public void setParam(Object param)
    {
        if (param == null)
            throw new NullPointerException();
        this.mParam = param;
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

    public abstract static class LoadCallback
    {

        private Object extra = null;

        public void setExtra(Object extra)
        {
            if (extra == null)
                throw new NullPointerException();
            this.extra = extra;
        }

        public Object getExtra()
        {
            return extra;
        }

        /**
         * <p>加载的具体实现，该方法将在非UI线程中执行，要注意不能执行UI的操作
         * 
         * @param param
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object param) throws Exception;

    }

    public abstract static class LoadResult {

        protected abstract void onSuccess(Context context, Object param, LoadCallback loader);

        protected abstract void onException(Context context, Object param, LoadCallback loader, Exception exception);

    }

}
