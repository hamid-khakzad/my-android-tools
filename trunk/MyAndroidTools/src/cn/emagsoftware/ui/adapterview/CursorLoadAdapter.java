package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.database.Cursor;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

public class CursorLoadAdapter extends GenericCursorAdapter
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

    CursorLoadAdapter(Context context, int viewTypeCount)
    {
        super(context, null, viewTypeCount);
    }

    public CursorLoadAdapter(Context context, LoadCallback callback)
    {
        this(context, callback, 1);
    }

    public CursorLoadAdapter(Context context, LoadCallback callback, int viewTypeCount)
    {
        super(context, null, viewTypeCount);
        if (callback == null)
            throw new NullPointerException();
        mCallback = callback;
    }

    public LoadCallback getLoadCallback()
    {
        return mCallback;
    }

    /**
     * <p>加载的执行方法</>
     * @param result
     * @return
     */
    public boolean load(LoadResult result)
    {
        return load(null,result);
    }

    public boolean load(Object param, LoadResult result) {
        if(result == null) throw new NullPointerException("result == null");
        if (mIsLoading)
            return false;
        mIsLoading = true;
        mParam = param;
        mTask = createTask(this,param,result);
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

    private static AsyncWeakTask<Object, Integer, Object> createTask(CursorLoadAdapter adapter,final Object param,LoadResult result) {
        final LoadCallback loader = adapter.mCallback;
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
                CursorLoadAdapter adapter = (CursorLoadAdapter) objs[0];
                LoadResult loadRst = (LoadResult)objs[1];
                adapter.mIsLoading = false;
                adapter.mIsLoaded = true;
                adapter.mIsException = false;
                Cursor cursor = (Cursor) result;
                adapter.changeCursor(cursor);
                loadRst.onSuccess(adapter,param);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                LogManager.logE(CursorLoadAdapter.class, "Execute loading failed.", e);
                CursorLoadAdapter adapter = (CursorLoadAdapter) objs[0];
                LoadResult loadRst = (LoadResult)objs[1];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                loadRst.onException(adapter,param,e);
            }
        };
    }

    public Object getCurParam() {
        return mParam;
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
         * <p>加载的具体实现，该方法将在非UI线程中执行，要注意不能执行UI的操作</>
         * @param param
         * @return
         * @throws Exception
         */
        protected abstract Cursor onLoad(Object param) throws Exception;

    }

    public abstract static class LoadResult {

        protected abstract void onSuccess(CursorLoadAdapter adapter, Object param);

        protected abstract void onException(CursorLoadAdapter adapter, Object param, Exception exception);

    }

}
