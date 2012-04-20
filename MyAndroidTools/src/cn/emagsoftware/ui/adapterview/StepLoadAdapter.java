package cn.emagsoftware.ui.adapterview;

import java.lang.reflect.Field;
import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.AdapterView;

public class StepLoadAdapter extends LoadAdapter
{

    /** 当前的页码 */
    private int              mPage        = 0;
    /** 总页数 */
    private int              mPages       = -1;
    /** 是否已经加载了全部数据 */
    private boolean          mIsLoadedAll = false;
    /** 分步加载时的回调对象 */
    private StepLoadCallback mCallback    = null;

    public StepLoadAdapter(Context context, StepLoadAdapter.StepLoadCallback callback)
    {
        super(context);
        if (callback == null)
            throw new NullPointerException();
        mCallback = callback;
    }

    public StepLoadCallback getStepLoadCallback()
    {
        return mCallback;
    }

    @Override
    public LoadCallback getLoadCallback()
    {
        throw new UnsupportedOperationException("Unsupported,use getStepLoadCallback() instead");
    }

    /**
     * <p>绑定AdapterView，使其自动分步加载 <p>目前只支持AbsListView，当AbsListView滑动到最后面时将自动开始新的加载 <p>AbsListView的bindStepLoading实现实际上执行了OnScrollListener事件；
     * 用户若包含自己的OnScrollListener逻辑，请在bindStepLoading之前调用setOnScrollListener，bindStepLoading方法会将用户的逻辑包含进来； 若在bindStepLoading之后调用setOnScrollListener，将取消bindStepLoading的作用
     * 
     * @param adapterView
     * @param remainingCount 当剩余多少个时开始继续加载，最小值为0，表示直到最后才开始继续加载
     */
    public void bindStepLoading(AdapterView<?> adapterView, int remainingCount)
    {
        if (adapterView instanceof AbsListView)
        {
            try
            {
                AbsListView absList = (AbsListView) adapterView;
                Field field = AbsListView.class.getDeclaredField("mOnScrollListener");
                field.setAccessible(true);
                AbsListView.OnScrollListener onScrollListener = (AbsListView.OnScrollListener) field.get(absList);
                if (onScrollListener != null && onScrollListener instanceof WrappedOnScrollListener)
                {
                    absList.setOnScrollListener(new WrappedOnScrollListener(((WrappedOnScrollListener) onScrollListener).getOriginalListener(), remainingCount));
                } else
                {
                    absList.setOnScrollListener(new WrappedOnScrollListener(onScrollListener, remainingCount));
                }
            } catch (NoSuchFieldException e)
            {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        } else
        {
            throw new UnsupportedOperationException("Only supports step loading for the AdapterView which is AbsListView.");
        }
    }

    /**
     * <p>覆盖了父类的同名方法，用来执行分步加载
     */
    @Override
    public boolean load(final Object condition)
    {
        if (mIsLoading)
            return false;
        mIsLoading = true;
        mCurCondition = condition;
        mCallback.onBeginLoad(mContext, condition);
        final int start = getRealCount();
        final int page = mPage;
        new AsyncWeakTask<Object, Integer, Object>(this)
        {
            @Override
            protected Object doInBackground(Object... params)
            {
                try
                {
                    return mCallback.onLoad(condition, start, page + 1);
                } catch (Exception e)
                {
                    return e;
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                StepLoadAdapter adapter = (StepLoadAdapter) objs[0];
                if (result instanceof Exception)
                {
                    Exception e = (Exception) result;
                    LogManager.logE(StepLoadAdapter.class, "Execute step loading failed.", e);
                    adapter.mIsLoading = false;
                    adapter.mIsException = true;
                    mCallback.onAfterLoad(adapter.mContext, condition, e);
                } else
                {
                    adapter.mPage++;
                    List<DataHolder> resultList = (List<DataHolder>) result;
                    if (resultList != null && resultList.size() > 0)
                        adapter.addDataHolders(resultList); // 该方法需在UI线程中执行且是非线程安全的
                    adapter.mIsLoading = false;
                    adapter.mIsLoaded = true;
                    if (adapter.mPages == -1)
                    {
                        if (resultList == null || resultList.size() == 0)
                            adapter.mIsLoadedAll = true;
                        else
                            adapter.mIsLoadedAll = false;
                    } else
                    {
                        if (adapter.mPage >= adapter.mPages)
                            adapter.mIsLoadedAll = true;
                        else
                            adapter.mIsLoadedAll = false;
                    }
                    adapter.mIsException = false;
                    mCallback.onAfterLoad(adapter.mContext, condition, null);
                }
            }
        }.execute("");
        return true;
    }

    /**
     * <p>获取当前的页码
     * 
     * @return
     */
    public int getPage()
    {
        return mPage;
    }

    /**
     * <p>设置总页数 <p>通过调用该方法可限制页码范围，从而避免不必要的额外加载，否则只有在分步加载的数据为空时才认为已全部加载
     * 
     * @param pages
     */
    public void setPages(int pages)
    {
        if (pages < 0)
            throw new IllegalArgumentException("pages could not be less than zero.");
        this.mPages = pages;
    }

    /**
     * <p>获取总页数
     * 
     * @return
     */
    public int getPages()
    {
        return mPages;
    }

    /**
     * <p>是否已全部加载
     * 
     * @return
     */
    public boolean isLoadedAll()
    {
        return mIsLoadedAll;
    }

    /**
     * <p>覆盖父类的方法，以重置当前类的一些属性
     */
    @Override
    public void clearDataHolders()
    {
        super.clearDataHolders();
        mPage = 0;
    }

    public static abstract class StepLoadCallback
    {
        /**
         * <p>在加载之前的回调方法，可以显示一些loading之类的字样。如对于ListView，可以通过addFooterView方法添加一个正在加载的提示
         * 
         * @param context
         * @param condition
         */
        protected abstract void onBeginLoad(Context context, Object condition);

        /**
         * <p>加载的具体实现，通过传入的参数可以实现分步加载。该方法由非UI线程回调，所以可以执行耗时操作
         * 
         * @param condition
         * @param start 要加载的开始序号，最小值为0
         * @param page 要加载的页码，最小值为1
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object condition, int start, int page) throws Exception;

        /**
         * <p>加载完成后的回调方法，可以通过判断exception是否为null来获悉加载成功与否，从而给用户一些提示
         * 
         * @param context
         * @param condition
         * @param exception
         */
        protected abstract void onAfterLoad(Context context, Object condition, Exception exception);
    }

    private class WrappedOnScrollListener implements AbsListView.OnScrollListener
    {
        private AbsListView.OnScrollListener mOriginalListener = null;
        private int                          mRemainingCount   = 0;

        public WrappedOnScrollListener(AbsListView.OnScrollListener originalListener, int remainingCount)
        {
            if (originalListener != null && originalListener instanceof WrappedOnScrollListener)
                throw new IllegalArgumentException("the OnScrollListener could not be WrappedOnScrollListener");
            this.mOriginalListener = originalListener;
            this.mRemainingCount = remainingCount;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
            // 执行原始监听器的逻辑
            if (mOriginalListener != null)
                mOriginalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            // 执行setOnScrollListener时就会触发onScroll，此时要排除AbsListView不可见或可见Item个数为0的情况
            // 修改AbsListView的Item个数时会触发onScroll，此时要排除AbsListView不可见的情况
            if (visibleItemCount == 0)
                return;
            if (firstVisibleItem + visibleItemCount + mRemainingCount >= totalItemCount && !isLoadedAll() && !isException())
            {
                load(mCurCondition);
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState)
        {
            // 执行原始监听器的逻辑
            if (mOriginalListener != null)
                mOriginalListener.onScrollStateChanged(view, scrollState);
        }

        public AbsListView.OnScrollListener getOriginalListener()
        {
            return mOriginalListener;
        }
    }

}
