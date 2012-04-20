package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

import android.content.Context;

public class GenericLoadAdapter extends GenericAdapter
{

    /** �Ƿ����ڼ��� */
    protected boolean    mIsLoading    = false;
    /** �Ƿ��Ѿ����ع� */
    protected boolean    mIsLoaded     = false;
    /** ��ǰ�ļ����Ƿ������쳣 */
    protected boolean    mIsException  = false;
    /** ��ǰ�ļ������� */
    protected Object     mCurCondition = null;
    /** ����ʱ�Ļص����� */
    private LoadCallback mLoadCallback = null;

    GenericLoadAdapter(Context context)
    {
        super(context);
    }

    public GenericLoadAdapter(Context context, GenericLoadAdapter.LoadCallback callback)
    {
        super(context);
        if (callback == null)
            throw new NullPointerException();
        mLoadCallback = callback;
    }

    /**
     * <p>���ص�ִ�з���
     * 
     * @param condition ����ʱ��Ҫ��������û��ʱ�ɴ�null
     * @return true��ʾ��ʼ���أ�false��ʾ�Ѿ��ڼ��أ����εĵ�����Ч
     */
    public boolean load(final Object condition)
    {
        if (mIsLoading)
            return false;
        mIsLoading = true;
        mCurCondition = condition;
        mLoadCallback.onBeginLoad(mContext, condition);
        new AsyncWeakTask<Object, Integer, Object>(this)
        {
            @Override
            protected Object doInBackground(Object... params)
            {
                try
                {
                    return mLoadCallback.onLoad(condition);
                } catch (Exception e)
                {
                    return e;
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                GenericLoadAdapter adapter = (GenericLoadAdapter) objs[0];
                if (result instanceof Exception)
                {
                    Exception e = (Exception) result;
                    LogManager.logE(GenericLoadAdapter.class, "Execute loading failed.", e);
                    adapter.mIsLoading = false;
                    adapter.mIsException = true;
                    mLoadCallback.onAfterLoad(adapter.mContext, condition, e);
                } else
                {
                    List<DataHolder> resultList = (List<DataHolder>) result;
                    if (resultList != null && resultList.size() > 0)
                        adapter.addDataHolders(resultList); // �÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
                    adapter.mIsLoading = false;
                    adapter.mIsLoaded = true;
                    adapter.mIsException = false;
                    mLoadCallback.onAfterLoad(adapter.mContext, condition, null);
                }
            }
        }.execute("");
        return true;
    }

    /**
     * <p>��ȡ��ǰ�ļ�������
     * 
     * @return
     */
    public Object getCurCondition()
    {
        return mCurCondition;
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

    public abstract static class LoadCallback
    {
        /**
         * <p>�ڼ���֮ǰ�Ļص�������������ʾһЩloading֮��������������ListView������ͨ��addFooterView�������һ�����ڼ��ص���ʾ
         * 
         * @param context
         * @param condition
         */
        protected abstract void onBeginLoad(Context context, Object condition);

        /**
         * <p>���صľ���ʵ�֣��÷������ڷ�UI�߳���ִ�У�Ҫע�ⲻ��ִ��UI�Ĳ���
         * 
         * @param condition
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object condition) throws Exception;

        /**
         * <p>������ɺ�Ļص�����������ͨ���ж�exception�Ƿ�Ϊnull����Ϥ���سɹ���񣬴Ӷ����û�һЩ��ʾ
         * 
         * @param context
         * @param condition
         * @param exception
         */
        protected abstract void onAfterLoad(Context context, Object condition, Exception exception);
    }

}
