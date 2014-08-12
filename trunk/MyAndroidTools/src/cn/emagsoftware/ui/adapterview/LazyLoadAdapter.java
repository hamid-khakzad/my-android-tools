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

    /** ��ǰ��ҳ�� */
    private int              mPage               = 0;
    /** ��ҳ�� */
    private int              mPages              = -1;
    /** ��û������Pages��ģʽ���Ƿ��Ѿ�������ȫ������ */
    private boolean          mIsLoadedAllNoPages = false;
    /** ������ʱ�Ļص����� */
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
     * <p>��AdapterView��ʹ���Զ������� <p>Ŀǰֻ֧��AbsListView����AbsListView�����������ʱ���Զ���ʼ�µļ��� <p>bindLazyLoadingʵ����������AbsListView��OnScrollListener������
     * �û��������Լ���OnScrollListener����������bindLazyLoading֮ǰ����setOnScrollListener��bindLazyLoading�����Ὣ�û����߼����������� ����bindLazyLoading֮�����setOnScrollListener����ȡ��bindLazyLoading������
     *
     * @param adapterView
     * @param remainingCount ��ʣ����ٸ�ʱ��ʼ�������أ���СֵΪ0����ʾֱ�����ſ�ʼ��������
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
     * <p>���Ǹ���ķ���������ִ��������</>
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
                    adapter.addDataHolders(resultList); // �÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
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
     * <p>��ȡ��ǰ��ҳ��
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
     * <p>������ҳ�� <p>ͨ�����ø÷���������ҳ�뷶Χ���Ӷ����ⲻ��Ҫ�Ķ�����أ�����ֻ���������ص�����Ϊ��ʱ����Ϊ��ȫ������
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
     * <p>��ȡ��ҳ��
     *
     * @return
     */
    public int getPages()
    {
        return mPages;
    }

    /**
     * <p>�Ƿ���ȫ������
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
     * <p>���Ǹ���ķ����������õ�ǰ���һЩ����
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
         * <p>���صľ���ʵ�֣�ͨ������Ĳ�������ʵ�������ء��÷����ɷ�UI�̻߳ص������Կ���ִ�к�ʱ����
         * 
         * @param param
         * @param start Ҫ���صĿ�ʼ��ţ���СֵΪ0
         * @param page Ҫ���ص�ҳ�룬��СֵΪ1
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
            // ִ��ԭʼ���������߼�
            if (mOriginalListener != null)
                mOriginalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            if (view.getVisibility() == View.GONE)
                // ������ִ��layout����ʾʱ�ܻᴥ�����¼���������δlayoutʱ��ֹ����Ҫ�Ĵ�����ִ��setOnScrollListener���޸�AbsListView��Item����ʱ���ᴥ�����¼���
                return;
            if (visibleItemCount == 0) // ͨ��visibleItemCountΪ0���жϴ�δlayout�������������¿ɼ�״̬���ܲ���View.GONE����Ҫ�ų�֮
                return;
            if (firstVisibleItem + visibleItemCount + mRemainingCount >= totalItemCount && !isLoadedAll() && !isException())
            {
                load();
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
