package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

import android.content.Context;

/**
 * @deprecated use {@link cn.emagsoftware.ui.adapterview.DataHolderLoader} instead.
 */
public abstract class BaseLoadAdapter extends GenericAdapter
{

    /** �Ƿ����ڼ��� */
    boolean              mIsLoading   = false;
    /** �Ƿ��Ѿ����ع� */
    boolean              mIsLoaded    = false;
    /** ��ǰ�ļ����Ƿ������쳣 */
    boolean              mIsException = false;
    /** �������� */
    Object               mParam       = null;
    /** ����ʱ�Ļص����� */
    private LoadCallback mCallback    = null;

    BaseLoadAdapter(Context context, int viewTypeCount)
    {
        super(context, viewTypeCount);
    }

    public BaseLoadAdapter(Context context, LoadCallback callback)
    {
        this(context, callback, 1);
    }

    public BaseLoadAdapter(Context context, LoadCallback callback, int viewTypeCount)
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
     * <p>���ص�ִ�з���
     * 
     * @return true��ʾ��ʼ���أ�false��ʾ�Ѿ��ڼ��أ����εĵ�����Ч
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
                adapter.onBeginLoad(adapter.mContext, mParam);
            }

            @Override
            protected Object doInBackgroundImpl(Object... params) throws Exception
            {
                return mCallback.onLoad(mParam);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                List<DataHolder> resultList = (List<DataHolder>) result;
                if (resultList != null && resultList.size() > 0)
                    adapter.addDataHolders(resultList); // �÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
                adapter.mIsLoading = false;
                adapter.mIsLoaded = true;
                adapter.mIsException = false;
                adapter.onAfterLoad(adapter.mContext, mParam, null);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                LogManager.logE(BaseLoadAdapter.class, "Execute loading failed.", e);
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                adapter.onAfterLoad(adapter.mContext, mParam, e);
            }
        }.execute("");
        return true;
    }

    /**
     * <p>���ò�������
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
     * <p>�Ƿ����ڼ���
     * 
     * @return
     */
    public boolean isLoading()
    {
        return mIsLoading;
    }

    /**
     * <p>�Ƿ��Ѿ����ع�
     * 
     * @return
     */
    public boolean isLoaded()
    {
        return mIsLoaded;
    }

    /**
     * <p>��ǰ�ļ����Ƿ������쳣
     * 
     * @return
     */
    public boolean isException()
    {
        return mIsException;
    }

    /**
     * <p>�ڼ���֮ǰ�Ļص�������������ʾһЩloading֮��������������ListView������ͨ��addFooterView�������һ�����ڼ��ص���ʾ
     * 
     * @param context
     * @param param
     */
    protected abstract void onBeginLoad(Context context, Object param);

    /**
     * <p>������ɺ�Ļص�����������ͨ���ж�exception�Ƿ�Ϊnull����Ϥ���سɹ���񣬴Ӷ����û�һЩ��ʾ
     * 
     * @param context
     * @param param
     * @param exception
     */
    protected abstract void onAfterLoad(Context context, Object param, Exception exception);

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
         * <p>���صľ���ʵ�֣��÷������ڷ�UI�߳���ִ�У�Ҫע�ⲻ��ִ��UI�Ĳ���
         * 
         * @param param
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object param) throws Exception;

    }

}
