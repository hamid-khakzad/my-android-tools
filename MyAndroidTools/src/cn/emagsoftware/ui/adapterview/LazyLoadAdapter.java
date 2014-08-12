package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;

import java.lang.reflect.Field;
import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

public class LazyLoadAdapter extends LoadAdapter
{

    /** 当前的页码 */
    private int              mPage               = 0;
    /** 总页数 */
    private int              mPages              = -1;
    /** 在没有设置Pages的模式下是否已经加载了全部数据 */
    private boolean          mIsLoadedAllNoPages = false;
    /** 懒加载时的回调对象 */
    private LazyLoadCallback mCallback           = null;
    private int mPagesLimit = 1;

    public LazyLoadAdapter(Context context, LazyLoadCallback callback)
    {
        this(context, callback, 1);
    }

    public LazyLoadAdapter(Context context, LazyLoadCallback callback, int viewTypeCount)
    {
        super(context, viewTypeCount);
        if (callback == null)
            throw new NullPointerException();
        mCallback = callback;
    }

    /**
     * <p>绑定AdapterView，使其自动懒加载 <p>目前只支持AbsListView，当AbsListView滑动到最后面时将自动开始新的加载 <p>bindLazyLoading实际上设置了AbsListView的OnScrollListener监听；
     * 用户若包含自己的OnScrollListener监听，请在bindLazyLoading之前调用setOnScrollListener，bindLazyLoading方法会将用户的逻辑包含进来； 若在bindLazyLoading之后调用setOnScrollListener，将取消bindLazyLoading的作用
     *
     * @param adapterView
     * @param remainingCount 当剩余多少个时开始继续加载，最小值为0，表示直到最后才开始继续加载
     */
    public void bindLazyLoading(AdapterView<? extends Adapter> adapterView, int remainingCount)
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
            throw new UnsupportedOperationException("Only supports lazy loading for the AdapterView which is AbsListView.");
        }
    }

    public void unbindLazyLoading(AdapterView<? extends Adapter> adapterView) {
        if (adapterView instanceof AbsListView) {
            try {
                AbsListView absList = (AbsListView) adapterView;
                Field field = AbsListView.class.getDeclaredField("mOnScrollListener");
                field.setAccessible(true);
                AbsListView.OnScrollListener onScrollListener = (AbsListView.OnScrollListener) field.get(absList);
                if(onScrollListener instanceof WrappedOnScrollListener) {
                    absList.setOnScrollListener(((WrappedOnScrollListener)onScrollListener).getOriginalListener());
                }
            }catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <p>覆盖父类的方法，用来执行懒加载</>
     * @param result
     * @return
     */
    @Override
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

    private static AsyncWeakTask<Object, Integer, Object> createTask(LazyLoadAdapter adapter,LoadResult result) {
        final LazyLoadCallback loader = adapter.mCallback;
        final Object param = adapter.mParam;
        final int start = adapter.getRealCount();
        final int page = adapter.mPage;
        return new AsyncWeakTask<Object, Integer, Object>(adapter,result)
        {
            @Override
            protected Object doInBackgroundImpl(Object... params) throws Exception
            {
                return loader.onLoad(param, start, page + 1);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                LazyLoadAdapter adapter = (LazyLoadAdapter) objs[0];
                LoadResult loadRst = (LoadResult) objs[1];
                adapter.mPage++;
                List<DataHolder> resultList = (List<DataHolder>) result;
                if (resultList != null && resultList.size() > 0)
                    adapter.addDataHolders(resultList); // 该方法需在UI线程中执行且是非线程安全的
                adapter.mIsLoading = false;
                adapter.mIsLoaded = true;
                if (adapter.mPages == -1)
                {
                    if (resultList == null || resultList.size() < adapter.mPagesLimit)
                        adapter.mIsLoadedAllNoPages = true;
                    else
                        adapter.mIsLoadedAllNoPages = false;
                }
                adapter.mIsException = false;
                loadRst.onSuccess(adapter.mContext,param,loader);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                LogManager.logE(LazyLoadAdapter.class, "Execute lazy loading failed.", e);
                LazyLoadAdapter adapter = (LazyLoadAdapter) objs[0];
                LoadResult loadRst = (LoadResult) objs[1];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                loadRst.onException(adapter.mContext,param,loader,e);
            }
        };
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

    public void setPagesLimitWithoutPages(int pagesLimit) {
        if(pagesLimit < 1) throw new IllegalArgumentException("pagesLimit could not be less than 1.");
        this.mPagesLimit = pagesLimit;
    }

    /**
     * <p>设置总页数 <p>通过调用该方法可限制页码范围，从而避免不必要的额外加载，否则只有在懒加载的数据为空时才认为已全部加载
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
        if (mPages == -1)
        {
            return mIsLoadedAllNoPages;
        } else
        {
            return mPage >= mPages;
        }
    }

    /**
     * <p>覆盖父类的方法，以重置当前类的一些属性
     */
    @Override
    public void clearDataHolders()
    {
        super.clearDataHolders();
        mPage = 0;
        mIsLoadedAllNoPages = false;
    }

    public static abstract class LazyLoadCallback extends LoadCallback
    {

        @Override
        protected final List<DataHolder> onLoad(Object param) throws Exception
        {
            throw new UnsupportedOperationException("Unsupported,use onLoad(param,start,page) instead");
        }

        /**
         * <p>加载的具体实现，通过传入的参数可以实现懒加载。该方法由非UI线程回调，所以可以执行耗时操作
         * 
         * @param param
         * @param start 要加载的开始序号，最小值为0
         * @param page 要加载的页码，最小值为1
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object param, int start, int page) throws Exception;

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
            if (view.getVisibility() == View.GONE)
                // 由于在执行layout并显示时总会触发该事件，所以在未layout时禁止不必要的触发（执行setOnScrollListener或修改AbsListView的Item个数时都会触发该事件）
                return;
            if (visibleItemCount == 0) // 通过visibleItemCount为0来判断从未layout的情况，该情况下可见状态可能不是View.GONE，但要排除之
                return;
            if (firstVisibleItem + visibleItemCount + mRemainingCount >= totalItemCount && !isLoadedAll() && !isException())
            {
                load();
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
