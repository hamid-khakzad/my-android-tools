package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.LoaderResult;

/**
 * Created by Wendell on 14-8-24.
 */
public abstract class BasePageLoader extends BaseLoader implements PageInterface<List<DataHolder>> {

    private int mPageSize;
    private int mStart = 0;
    private int mStartSign;
    private int mPageCount = -1;
    private int mCurPageSize = -1;

    public BasePageLoader(Context context, int pageSize) {
        super(context);
        if(pageSize <= 0) throw new IllegalArgumentException("pageSize <= 0");
        mPageSize = pageSize;
    }

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

    @Override
    public void forcePageLoad() {
        super.forceLoad();
        List<DataHolder> data = mResult==null?null:mResult.getData();
        mStart = data==null?0:data.size();
    }

    @Override
    protected final List<DataHolder> loadInBackgroundImpl(boolean isRefresh) throws Exception {
        int start = mStart;
        if(start == -1) {
            start = 0;
            mStartSign = start;
        }else {
            int calcStart = loadCountInBackground();
            if(calcStart != -1 && calcStart < 0) {
                throw new IllegalStateException("loadCountInBackground()'value should (==-1 or >=0)");
            }
            if(calcStart == -1) {
                mStartSign = -1;
            }else {
                start = calcStart;
                mStartSign = start;
            }
            if(isRefresh) { //���ͬ������
                isRefresh = false;
            }
        }
        int page = start / mPageSize;
        page = start%mPageSize==0?page+1:page+2;
        return loadPageInBackground(isRefresh,start,page);
    }

    @Override
    protected void deliverLoadedResult(LoaderResult<List<DataHolder>> data) {
        if(data != null && data.getException() == null) {
            List<DataHolder> pageData = data.getData();
            if(mStartSign == -1) {
                List<DataHolder> oldData = mResult==null?null:mResult.getData();
                mCurPageSize = pageData==null?0:pageData.size();
                if(oldData != null) {
                    if(pageData == null) {
                        data = new LoaderResult<List<DataHolder>>(null,oldData);
                    }else {
                        List<DataHolder> all = new ArrayList<DataHolder>(oldData.size() + pageData.size());
                        all.addAll(oldData);
                        all.addAll(pageData);
                        data = new LoaderResult<List<DataHolder>>(null,all);
                    }
                }
            }else {
                int allSize = pageData==null?0:pageData.size();
                mCurPageSize = allSize - mStartSign;
                if(mCurPageSize < 0) {
                    mCurPageSize = 0;
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

    @Override
    public void setPageCount(int pageCount) {
        if(pageCount < 0) throw new IllegalArgumentException("pageCount < 0");
        this.mPageCount = pageCount;
    }

    @Override
    public boolean isLoadedAll() {
        if(mResult != null) {
            if(mPageCount == -1) {
                if(mCurPageSize != -1) {
                    return mCurPageSize < mPageSize;
                }
            }else {
                List<DataHolder> data = mResult.getData();
                int curSize = data==null?0:data.size();
                int curPageCount = curSize/mPageSize;
                curPageCount = curSize%mPageSize==0?curPageCount:curPageCount+1;
                return curPageCount >= mPageCount;
            }
        }
        return false;
    }

    @Override
    public boolean isException() {
        if(mResult != null) {
            return mResult.getException() != null;
        }
        return false;
    }

    /**
     * <p>��AdapterView��ʹ���Զ���ҳ����</>
     * <p>Ŀǰֻ֧��AbsListView����AbsListView�����������ʱ������OnPageLoading�Ļص�����</>
     * <p>bindPageLoadingʵ����������AbsListView��OnScrollListener�������û��������Լ���OnScrollListener����������bindPageLoading֮ǰ����setOnScrollListener��bindPageLoading�����Ὣ�û����߼����������� ����bindPageLoading֮�����setOnScrollListener����ȡ��bindPageLoading������</>
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
            // ִ��ԭʼ���������߼�
            if (mOriginalListener != null)
                mOriginalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            if (view.getVisibility() == View.GONE)
                // ������ִ��layout����ʾʱ�ܻᴥ�����¼���������δlayoutʱ��ֹ����Ҫ�Ĵ�����ִ��setOnScrollListener���޸�AbsListView��Item����ʱ���ᴥ�����¼���
                return;
            if (visibleItemCount == 0) // ͨ��visibleItemCountΪ0���жϴ�δlayout�������������¿ɼ�״̬���ܲ���View.GONE����Ҫ�ų�֮
                return;
            if (firstVisibleItem + visibleItemCount + mRemainingCount >= totalItemCount)
            {
                mOnPageLoading.onPageLoading(view);
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState)
        {
            // ִ��ԭʼ���������߼�
            if (mOriginalListener != null)
                mOriginalListener.onScrollStateChanged(view, scrollState);
        }

        public AbsListView.OnScrollListener getOriginalListener()
        {
            return mOriginalListener;
        }
    }

}
