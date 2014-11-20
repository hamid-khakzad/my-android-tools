package cn.emagsoftware.ui;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;

import java.lang.reflect.Field;

/**
 * Created by Wendell on 14-9-12.
 */
public abstract class BaseTaskPageLoader<D> extends BaseTaskLoader<D> {

    private int mPageSize;
    private int mStart = 0;
    private int mStartSign;
    private int mPageCount = -1;
    private int mCurPageSize = -1;

    public BaseTaskPageLoader(Context context, int pageSize) {
        super(context);
        if(pageSize <= 0) throw new IllegalArgumentException("pageSize <= 0");
        mPageSize = pageSize;
    }

    protected abstract int getCount(D data);
    /**
     * <p>加载分页数据</>
     * @param isRefresh
     * @param start 起始位置，最小为0
     * @param page 起始页，最小为1
     * @return
     * @throws Exception
     */
    protected abstract D loadPageInBackground(boolean isRefresh,int start,int page) throws Exception;
    protected abstract D merge(D old,D add);

    @Override
    public void forceLoad() {
        super.forceLoad();
        mStart = -1;
    }

    @Override
    public void forceRefresh() {
        super.forceRefresh();
        mStart = -1;
    }

    public void forcePageLoad() {
        super.forceLoad();
        D data = mResult==null?null:mResult.getData();
        mStart = data==null?0:getCount(data);
    }

    @Override
    protected final D loadInBackgroundImpl(boolean isRefresh) throws Exception {
        int start = mStart;
        if(start == -1) {
            start = 0;
            mStartSign = start;
        }else {
            mStartSign = -1;
            if(isRefresh) { //并不是刻意检查取消，此时isRefresh为true会导致逻辑问题
                return null;
            }
        }
        int page = start / mPageSize;
        page = start%mPageSize==0?page+1:page+2;
        return loadPageInBackground(isRefresh,start,page);
    }

    @Override
    protected void deliverLoadedResult(LoaderResult<D> data) {
        if(data != null && data.getException() == null) {
            D pageData = data.getData();
            mCurPageSize = pageData==null?0:getCount(pageData);
            if(mStartSign == -1) {
                D oldData = mResult==null?null:mResult.getData();
                if(oldData != null) {
                    if(pageData == null) {
                        data = new LoaderResult<D>(null,oldData);
                    }else {
                        D allData = merge(oldData,pageData);
                        if(pageData != allData) {
                            onReleaseData(pageData);
                        }
                        data = new LoaderResult<D>(null,allData);
                    }
                }
            }
        }
        super.deliverLoadedResult(data);
    }

    @Override
    protected void onStartLoading() {
        int resumeType = mResumeType;
        super.onStartLoading();
        if(resumeType == 3 && !isLoading()) {
            forcePageLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        boolean isLoading = isLoading();
        super.onStopLoading();
        if(isLoading && mStart != -1) {
            mResumeType = 3;
        }
    }

    public void setPageCount(int pageCount) {
        if(pageCount < 0) throw new IllegalArgumentException("pageCount < 0");
        this.mPageCount = pageCount;
    }

    public boolean isLoadedAll() {
        if(mResult != null) {
            if(mPageCount == -1) {
                if(mCurPageSize != -1) {
                    return mCurPageSize < mPageSize;
                }
            }else {
                D data = mResult.getData();
                int curSize = data==null?0:getCount(data);
                int curPageCount = curSize/mPageSize;
                curPageCount = curSize%mPageSize==0?curPageCount:curPageCount+1;
                return curPageCount >= mPageCount;
            }
        }
        return false;
    }

    public boolean isPageException() {
        if(mResult != null) {
            if(!mResult.mIsRefresh) {
                return mResult.getException() != null;
            }
        }
        return false;
    }

    /**
     * <p>绑定AdapterView，使其自动分页加载</>
     * <p>目前只支持AbsListView，当AbsListView滑动到最后面时将触发OnPageLoading的回调方法</>
     * <p>bindPageLoading实际上设置了AbsListView的OnScrollListener监听；用户若包含自己的OnScrollListener监听，请在bindPageLoading之前调用setOnScrollListener，bindPageLoading方法会将用户的逻辑包含进来； 若在bindPageLoading之后调用setOnScrollListener，将取消bindPageLoading的作用</>
     * @param adapterView
     * @param onPageLoading
     */
    public static void bindPageLoading(AdapterView<? extends Adapter> adapterView,OnPageLoading onPageLoading)
    {
        if (adapterView instanceof AbsListView)
        {
            try
            {
                AbsListView absList = (AbsListView) adapterView;
                Field field = AbsListView.class.getDeclaredField("mOnScrollListener");
                field.setAccessible(true);
                AbsListView.OnScrollListener onScrollListener = (AbsListView.OnScrollListener) field.get(absList);
                if (onScrollListener instanceof WrappedOnScrollListener)
                {
                    absList.setOnScrollListener(new WrappedOnScrollListener(((WrappedOnScrollListener) onScrollListener).getOriginalListener(), onPageLoading, 0));
                } else
                {
                    absList.setOnScrollListener(new WrappedOnScrollListener(onScrollListener, onPageLoading, 0));
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
            throw new UnsupportedOperationException("Only supports page loading for the AdapterView which is AbsListView.");
        }
    }

    public static void unbindPageLoading(AdapterView<? extends Adapter> adapterView) {
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

    public static interface OnPageLoading {
        public void onPageLoading(AdapterView<? extends Adapter> adapterView);
    }

    private static class WrappedOnScrollListener implements AbsListView.OnScrollListener
    {
        private AbsListView.OnScrollListener mOriginalListener = null;
        private OnPageLoading mOnPageLoading = null;
        private int                          mRemainingCount   = 0;

        public WrappedOnScrollListener(AbsListView.OnScrollListener originalListener, OnPageLoading onPageLoading, int remainingCount)
        {
            if (originalListener instanceof WrappedOnScrollListener)
                throw new IllegalArgumentException("the OnScrollListener could not be WrappedOnScrollListener");
            if(onPageLoading == null) throw new NullPointerException("onPageLoading == null");
            if(remainingCount < 0) throw new IllegalArgumentException("remainingCount < 0");
            this.mOriginalListener = originalListener;
            this.mOnPageLoading = onPageLoading;
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
            if (firstVisibleItem + visibleItemCount + mRemainingCount >= totalItemCount)
            {
                mOnPageLoading.onPageLoading(view);
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
