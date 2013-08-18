package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;

/**
 * <p>��ǰ��û���ṩsetData��������Ϊ����ܻ�Ӱ���첽���ݼ��ػ��ƣ������Ҫ�ı�data����ʹ����data�������µ�DataHolderʵ��
 * 
 * @author Wendell
 */
public abstract class DataHolder
{

    /**�������첽�������п�����Ϊ�����ⲿ���ö����ܼ�ʱ�����գ����������GLOBAL_CACHE������Ϊ����AdapterView����󻺴����Ϊ���ʣ��ʷ���Ĵ�Сֻ��2.5M*/
    private static LruCache<String,Object> GLOBAL_CACHE = new LruCache<String, Object>((int)(2.5 * 1024)){
        @Override
        protected int sizeOf(String key, Object value) {
            if(value instanceof Bitmap)
            {
                Bitmap bitmap = (Bitmap)value;
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }else if(value instanceof byte[])
            {
                return ((byte[])value).length / 1024;
            }
            return super.sizeOf(key, value);
        }
    };

    private Object   mData          = null;
    private String[] mGlobalIds     = null;
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
        mGlobalIds = new String[asyncDataCount];
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
     * @param asyncData
     * @param asyncDataIndex
     */
    public abstract void onAsyncDataExecuted(Context context, int position, View view, Object asyncData, int asyncDataIndex);

    /**
     * <p>���DataHolder��Ҫ������ͬ��View������Ҫ���Ǹ÷������ṩ��ͬ���͵���ֵ��ʾ���÷�����Ҫ��GenericAdapter����viewTypeCount�����Ĺ��캯��һ��ʹ��
     * 
     * @return
     */
    public int getType()
    {
        return 0;
    }

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
     * <p>��ȡָ�����첽���ݣ�δ���ػ��ѱ�����ʱ����null</>
     * @param index
     * @param globalId
     * @return
     */
    public Object getAsyncData(int index, String globalId)
    {
        if(globalId == null)
            throw new NullPointerException();
        mGlobalIds[index] = globalId;
        Object asyncData = GLOBAL_CACHE.get(globalId);
        if (asyncData == null)
            mExecuteConfig.mShouldExecute = true;
        return asyncData;
    }

    /**
     * <p>�ڲ�������ָ�����첽�����Ƿ���Ҫ��ִ��</>
     * @param index
     * @return
     */
    String asyncDataShouldExecute(int index)
    {
        String globalId = mGlobalIds[index];
        if(globalId == null)
            return null;
        return GLOBAL_CACHE.get(globalId) == null ? globalId : null;
    }

    /**
     * <p>�ڲ�����������ָ�����첽����</>
     * @param globalId
     * @param asyncData
     */
    void setAsyncData(String globalId, Object asyncData)
    {
        GLOBAL_CACHE.put(globalId,asyncData);
    }

    /**
     * <p>��ȡ�첽���ݵĸ���
     * 
     * @return
     */
    public int getAsyncDataCount()
    {
        return mGlobalIds.length;
    }

    class ExecuteConfig
    {
        boolean mShouldExecute = false;
        boolean mIsExecuting   = false;
        int     mGroupPosition = -1;
        int     mPosition      = -1;
    }

}
