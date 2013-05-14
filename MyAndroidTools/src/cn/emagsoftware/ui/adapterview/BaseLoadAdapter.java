package cn.emagsoftware.ui.adapterview;

import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

import android.content.Context;

public abstract class BaseLoadAdapter extends GenericAdapter
{

    /** �Ƿ����ڼ��� */
    boolean              mIsLoading   = false;
    /** �Ƿ��Ѿ����ع� */
    boolean              mIsLoaded    = false;
    /** ��ǰ�ļ����Ƿ������쳣 */
    boolean              mIsException = false;
    /** �������� */
    Object               mExtra       = null;
    /** ����ʱ�Ļص����� */
    private LoadCallback mCallback    = null;

    BaseLoadAdapter(Context context, int viewTypeCount)
    {
        super(context, viewTypeCount);
    }

    public BaseLoadAdapter(Context context, BaseLoadAdapter.LoadCallback callback)
    {
        this(context, callback, 1);
    }

    public BaseLoadAdapter(Context context, BaseLoadAdapter.LoadCallback callback, int viewTypeCount)
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
                adapter.onBeginLoad(adapter.mContext, mExtra);
            }

            @Override
            protected Object doInBackgroundImpl(Object... params) throws Exception
            {
                return mCallback.onLoad(mExtra);
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
                adapter.onAfterLoad(adapter.mContext, mExtra, null);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                LogManager.logE(BaseLoadAdapter.class, "Execute loading failed.", e);
                BaseLoadAdapter adapter = (BaseLoadAdapter) objs[0];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                adapter.onAfterLoad(adapter.mContext, mExtra, e);
            }
        }.execute("");
        return true;
    }

    /**
     * <p>���ö�������
     * 
     * @param extra
     */
    public void setExtra(Object extra)
    {
        if (extra == null)
            throw new NullPointerException();
        this.mExtra = extra;
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
     * @param extra
     */
    protected abstract void onBeginLoad(Context context, Object extra);

    /**
     * <p>������ɺ�Ļص�����������ͨ���ж�exception�Ƿ�Ϊnull����Ϥ���سɹ���񣬴Ӷ����û�һЩ��ʾ
     * 
     * @param context
     * @param extra
     * @param exception
     */
    protected abstract void onAfterLoad(Context context, Object extra, Exception exception);

    public abstract static class LoadCallback
    {

        /**
         * <p>���صľ���ʵ�֣��÷������ڷ�UI�߳���ִ�У�Ҫע�ⲻ��ִ��UI�Ĳ���
         * 
         * @param extra
         * @return
         * @throws Exception
         */
        protected abstract List<DataHolder> onLoad(Object extra) throws Exception;

    }

}
