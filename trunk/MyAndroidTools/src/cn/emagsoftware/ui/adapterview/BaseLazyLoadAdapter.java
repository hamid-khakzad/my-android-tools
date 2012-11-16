package cn.emagsoftware.ui.adapterview;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

public abstract class BaseLazyLoadAdapter extends BaseLoadAdapter
{

    /** ��ǰ��ҳ�� */
    private int              mPage        = 0;
    /** ��ҳ�� */
    private int              mPages       = -1;
    /** �Ƿ��Ѿ�������ȫ������ */
    private boolean          mIsLoadedAll = false;
    /** ������ʱ�Ļص����� */
    private LazyLoadCallback mCallback    = null;

    public BaseLazyLoadAdapter(Context context, BaseLazyLoadAdapter.LazyLoadCallback callback)
    {
        this(context, callback, 1);
    }

    public BaseLazyLoadAdapter(Context context, BaseLazyLoadAdapter.LazyLoadCallback callback, int viewTypeCount)
    {
        super(context, viewTypeCount);
        if (callback == null)
            throw new NullPointerException();
        mCallback = callback;
    }

    public LazyLoadCallback getLazyLoadCallback()
    {
        return mCallback;
    }

    @Override
    public LoadCallback getLoadCallback()
    {
        throw new UnsupportedOperationException("Unsupported,use getLazyLoadCallback() instead");
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

    /**
     * <p>�����˸����ͬ������������ִ��������
     */
    @Override
    public boolean load(final Object condition)
    {
        if (mIsLoading)
            return false;
        mIsLoading = true;
        mCurCondition = condition;
        final int start = getRealCount();
        final int page = mPage;
        new AsyncWeakTask<Object, Integer, Object>(this)
        {
            @Override
            protected void onPreExecute(Object[] objs)
            {
                BaseLazyLoadAdapter adapter = (BaseLazyLoadAdapter) objs[0];
                adapter.onBeginLoad(adapter.mContext, condition);
            }

            @Override
            protected Object doInBackgroundImpl(Object... params) throws Exception
            {
                return mCallback.onLoad(condition, start, page + 1);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                BaseLazyLoadAdapter adapter = (BaseLazyLoadAdapter) objs[0];
                adapter.mPage++;
                List<DataHolder> resultList = (List<DataHolder>) result;
                if (resultList != null && resultList.size() > 0)
                    adapter.addDataHolders(resultList); // �÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
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
                adapter.onAfterLoad(adapter.mContext, condition, null);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                LogManager.logE(BaseLazyLoadAdapter.class, "Execute lazy loading failed.", e);
                BaseLazyLoadAdapter adapter = (BaseLazyLoadAdapter) objs[0];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                adapter.onAfterLoad(adapter.mContext, condition, e);
            }
        }.execute("");
        return true;
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
        return mIsLoadedAll;
    }

    /**
     * <p>���Ǹ���ķ����������õ�ǰ���һЩ����
     */
    @Override
    public void clearDataHolders()
    {
        super.clearDataHolders();
        mPage = 0;
    }

    public static abstract class LazyLoadCallback
    {

        /**
         * <p>���صľ���ʵ�֣�ͨ������Ĳ�������ʵ�������ء��÷����ɷ�UI�̻߳ص������Կ���ִ�к�ʱ����
         * 
         * @param condition
         * @param start Ҫ���صĿ�ʼ��ţ���СֵΪ0
         * @param page Ҫ���ص�ҳ�룬��СֵΪ1
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object condition, int start, int page) throws Exception;

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
            if (view.getVisibility() != View.VISIBLE)
                // ������ִ��layout����ʾʱ�ܻᴥ�����¼���������δ��ʾʱ��ֹ����Ҫ�Ĵ�����ִ��setOnScrollListener���޸�AbsListView��Item����ʱ���ᴥ�����¼���
                // ע�⣬��INVISIBLE��VISIBLE����TabLayout��ʵ�֣�������������layout�����²��ᴥ�����¼�����֮ǰ�����ѽ�ֹ�����¼��Ĵ��������������������ʾʱ����Ҫ�ֹ�����load����
                return;
            if (visibleItemCount == 0) // ͨ��visibleItemCountΪ0���жϴ�δlayout�������������¿ɼ�״̬Ҳ����ΪView.VISIBLE��Ҫ�ų�֮
                return;
            if (firstVisibleItem + visibleItemCount + mRemainingCount >= totalItemCount && !isLoadedAll() && !isException())
            {
                load(mCurCondition);
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
