package cn.emagsoftware.ui.adapterview;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.view.View;

public abstract class DataHolder
{

    private Object   mData          = null;
    private Object[] mAsyncData     = null;
    ExecuteConfig    mExecuteConfig = new ExecuteConfig();

    /**
     * <p>���캯��
     * 
     * @param data ��Ҫ�õ�������
     * @param asyncDataCount ��Ҫ��ִ�е��첽���ݵĸ���
     */
    public DataHolder(Object data, int asyncDataCount)
    {
        mData = data;
        mAsyncData = new Object[asyncDataCount];
    }

    /**
     * <p>ʹ�õ�ǰdata����Viewʱ���� <p>��������첽���ݵļ��أ��ڴ���Viewʱ��ͨ��getAsyncData(int index)�Ƿ�Ϊnull���ж������첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View�� <p>����ͨ��ViewHolder����View�Ľṹ��Ϣ���Ӷ���߸���ʱ��Ч��
     * 
     * @param context
     * @param position
     * @param data
     * @return
     */
    public abstract View onCreateView(Context context, int position, Object data);

    /**
     * <p>ʹ�õ�ǰdata����Viewʱ���������ڽ�Լ��Դ�Ŀ��ǣ�ViewĬ�ϻᱻ���ã���ʱֻ��Ҫ����View���� <p>��������첽���ݵļ��أ��ڸ���Viewʱ��ͨ��getAsyncData(int index)�Ƿ�Ϊnull���ж������첽�����Ƿ������ɣ���������ɣ�ҲҪ���µ�View�� <p>��ͨ��GenericAdapter�Ĺ��캯��������View�����ã��ɱ��ָ÷�����ʵ��Ϊ��
     * <p>����View����ͨ��ViewHolder�����Ч��
     * 
     * @param context
     * @param position
     * @param view
     * @param data
     */
    public abstract void onUpdateView(Context context, int position, View view, Object data);

    /**
     * <p>��ǰ��ĳһ���첽���ݱ�ִ�гɹ��󴥷�������ǰ���첽���ݸ���Ϊ0���ɱ��ָ÷�����ʵ��Ϊ��
     * 
     * @param context
     * @param position
     * @param view
     * @param asyncData ִ�гɹ�����첽���ݣ�ֻ��ֱ��ʹ�ø�ֵ������ʹ��getAsyncData(asyncDataIndex)����Ϊ�첽������ȫ��ִ�������޸�Ϊ�����ã�ֻ�и�ֵ����ȷ�����첽���ݵľ�������
     * @param asyncDataIndex
     */
    public abstract void onAsyncDataExecuted(Context context, int position, View view, Object asyncData, int asyncDataIndex);

    /**
     * <p>��ȡ���캯���д�������ݶ���
     * 
     * @return
     */
    public Object getData()
    {
        return mData;
    }

    /**
     * <p>���蹹�캯���д�������ݶ���
     * 
     * @param data
     */
    public void setData(Object data)
    {
        mData = data;
    }

    /**
     * <p>��ȡָ��λ�õ��첽���ݣ�δ���ػ��ѱ�����ʱ����null
     * 
     * @param index �첽���ݵ�λ��
     * @return
     */
    public Object getAsyncData(int index)
    {
        Object asyncData = mAsyncData[index];
        if (asyncData instanceof SoftReference<?>)
        {
            SoftReference<?> asyncDataRef = (SoftReference<?>) asyncData;
            asyncData = asyncDataRef.get();
        }
        if (asyncData == null)
            mExecuteConfig.mShouldExecute = true;
        return asyncData;
    }

    /**
     * <p>�ڲ�����������ָ��λ�õ��첽����
     * 
     * @param index �첽���ݵ�λ��
     * @param asyncData
     */
    void setAsyncData(int index, Object asyncData)
    {
        if (asyncData instanceof SoftReference<?>)
            throw new IllegalArgumentException("asyncData can not be a type of SoftReference which is used by itself");
        mAsyncData[index] = asyncData;
    }

    /**
     * <p>�ڲ���������ָ��λ�õ��첽���ݵ���Ϊ�����ã�����GC���л���
     * 
     * @param index
     */
    void changeAsyncDataToSoftReference(int index)
    {
        Object asyncData = mAsyncData[index];
        if (asyncData instanceof SoftReference<?>)
            return;
        mAsyncData[index] = new SoftReference<Object>(asyncData);
    }

    /**
     * <p>��ȡ�첽���ݵĸ���
     * 
     * @return
     */
    public int getAsyncDataCount()
    {
        return mAsyncData.length;
    }

    class ExecuteConfig
    {
        boolean mShouldExecute = false;
        boolean mIsExecuting   = false;
        int     mPosition      = -1;
    }

}
