package cn.emagsoftware.ui.adapterview;

import android.content.Context;

import java.util.List;

import cn.emagsoftware.util.AsyncWeakTask;
import cn.emagsoftware.util.LogManager;

public class LoadAdapter extends GenericAdapter
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
    AsyncWeakTask<Object, Integer, Object> mTask = null;

    LoadAdapter(Context context, int viewTypeCount)
    {
        super(context, viewTypeCount);
    }

    public LoadAdapter(Context context, LoadCallback callback)
    {
        this(context, callback, 1);
    }

    public LoadAdapter(Context context, LoadCallback callback, int viewTypeCount)
    {
        super(context, viewTypeCount);
        if (callback == null)
            throw new NullPointerException();
        mCallback = callback;
    }

    /**
     * <p>���ص�ִ�з���</>
     * @param result
     * @return
     */
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

    public boolean cancelLoad() {
        if(!mIsLoading) return false;
        mTask.cancel(true);
        mTask = null;
        mIsLoading = false;
        return true;
    }

    private static AsyncWeakTask<Object, Integer, Object> createTask(LoadAdapter adapter,LoadResult result) {
        final LoadCallback loader = adapter.mCallback;
        final Object param = adapter.mParam;
        return new AsyncWeakTask<Object, Integer, Object>(adapter,result)
        {
            @Override
            protected Object doInBackgroundImpl(Object... params) throws Exception
            {
                return loader.onLoad(param);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onPostExecute(Object[] objs, Object result)
            {
                LoadAdapter adapter = (LoadAdapter) objs[0];
                LoadResult loadRst = (LoadResult)objs[1];
                List<DataHolder> resultList = (List<DataHolder>) result;
                if (resultList != null && resultList.size() > 0)
                    adapter.addDataHolders(resultList); // �÷�������UI�߳���ִ�����Ƿ��̰߳�ȫ��
                adapter.mIsLoading = false;
                adapter.mIsLoaded = true;
                adapter.mIsException = false;
                loadRst.onSuccess(adapter.mContext,param,loader);
            }

            @Override
            protected void onException(Object[] objs, Exception e)
            {
                LogManager.logE(LoadAdapter.class, "Execute loading failed.", e);
                LoadAdapter adapter = (LoadAdapter) objs[0];
                LoadResult loadRst = (LoadResult)objs[1];
                adapter.mIsLoading = false;
                adapter.mIsException = true;
                loadRst.onException(adapter.mContext,param,loader,e);
            }
        };
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

    public abstract static class LoadResult {

        protected abstract void onSuccess(Context context, Object param, LoadCallback loader);

        protected abstract void onException(Context context, Object param, LoadCallback loader, Exception exception);

    }

}
