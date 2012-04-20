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

    /** ��ǰ��ҳ�� */
    private int              mPage        = 0;
    /** ��ҳ�� */
    private int              mPages       = -1;
    /** �Ƿ��Ѿ�������ȫ������ */
    private boolean          mIsLoadedAll = false;
    /** �ֲ�����ʱ�Ļص����� */
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
     * <p>��AdapterView��ʹ���Զ��ֲ����� <p>Ŀǰֻ֧��AbsListView����AbsListView�����������ʱ���Զ���ʼ�µļ��� <p>AbsListView��bindStepLoadingʵ��ʵ����ִ����OnScrollListener�¼���
     * �û��������Լ���OnScrollListener�߼�������bindStepLoading֮ǰ����setOnScrollListener��bindStepLoading�����Ὣ�û����߼����������� ����bindStepLoading֮�����setOnScrollListener����ȡ��bindStepLoading������
     * 
     * @param adapterView
     * @param remainingCount ��ʣ����ٸ�ʱ��ʼ�������أ���СֵΪ0����ʾֱ�����ſ�ʼ��������
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
     * <p>�����˸����ͬ������������ִ�зֲ�����
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
                    mCallback.onAfterLoad(adapter.mContext, condition, null);
                }
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
     * <p>������ҳ�� <p>ͨ�����ø÷���������ҳ�뷶Χ���Ӷ����ⲻ��Ҫ�Ķ�����أ�����ֻ���ڷֲ����ص�����Ϊ��ʱ����Ϊ��ȫ������
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

    public static abstract class StepLoadCallback
    {
        /**
         * <p>�ڼ���֮ǰ�Ļص�������������ʾһЩloading֮��������������ListView������ͨ��addFooterView�������һ�����ڼ��ص���ʾ
         * 
         * @param context
         * @param condition
         */
        protected abstract void onBeginLoad(Context context, Object condition);

        /**
         * <p>���صľ���ʵ�֣�ͨ������Ĳ�������ʵ�ֲַ����ء��÷����ɷ�UI�̻߳ص������Կ���ִ�к�ʱ����
         * 
         * @param condition
         * @param start Ҫ���صĿ�ʼ��ţ���СֵΪ0
         * @param page Ҫ���ص�ҳ�룬��СֵΪ1
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object condition, int start, int page) throws Exception;

        /**
         * <p>������ɺ�Ļص�����������ͨ���ж�exception�Ƿ�Ϊnull����Ϥ���سɹ���񣬴Ӷ����û�һЩ��ʾ
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
            // ִ��ԭʼ���������߼�
            if (mOriginalListener != null)
                mOriginalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            // ִ��setOnScrollListenerʱ�ͻᴥ��onScroll����ʱҪ�ų�AbsListView���ɼ���ɼ�Item����Ϊ0�����
            // �޸�AbsListView��Item����ʱ�ᴥ��onScroll����ʱҪ�ų�AbsListView���ɼ������
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
