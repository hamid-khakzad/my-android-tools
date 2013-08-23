package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>��ǰ��û���ṩsetData��������Ϊ����ܻ�Ӱ���첽���ݼ��ػ��ƣ������Ҫ�ı�data����ʹ����data�������µ�DataHolderʵ��
 * 
 * @author Wendell
 */
public abstract class DataHolder
{

    /**�������첽�������п�����Ϊ�����ⲿ���ö����ܼ�ʱ�����գ����������GLOBAL_CACHE������Ϊ����AdapterView����󻺴����Ϊ����*/
    private static LruCache<String,Object> GLOBAL_CACHE = new LruCache<String, Object>((int)(Runtime.getRuntime().maxMemory()/8)){
        @Override
        protected int sizeOf(String key, Object value) {
            if(value instanceof Bitmap)
            {
                Bitmap bitmap = (Bitmap)value;
                return bitmap.getRowBytes() * bitmap.getHeight();
            }else if(value instanceof byte[])
            {
                return ((byte[])value).length;
            }
            return super.sizeOf(key, value);
        }
    };

    private Object   mData          = null;
    private int mAsyncDataCount;
    ExecuteConfig    mExecuteConfig = new ExecuteConfig();

    /**
     * <p>���캯��
     * 
     * @param data ��Ҫ�õ�������
     * @param asyncDataCount ��Ҫ��ִ�е��첽���ݵĸ���
     */
    public DataHolder(Object data, int asyncDataCount)
    {
        if(asyncDataCount < 0)
            throw new IllegalArgumentException("asyncDataCount < 0");
        mData = data;
        mAsyncDataCount = asyncDataCount;
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
        if(index < 0 || index >= mAsyncDataCount)
            throw new IllegalArgumentException("index < 0 || index >= mAsyncDataCount");
        Object asyncData = GLOBAL_CACHE.get(globalId);
        if (asyncData == null)
            mExecuteConfig.mUnits.put(index,globalId);
        return asyncData;
    }

    /**
     * <p>�ڲ���������ȡָ�����첽����</>
     * @param globalId
     * @return
     */
    Object getAsyncData(String globalId)
    {
        return GLOBAL_CACHE.get(globalId);
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
        return mAsyncDataCount;
    }

    class ExecuteConfig
    {
        boolean mShouldRefresh = true;
        HashMap<Integer,String> mUnits = new HashMap<Integer, String>();
        int     mGroupPosition = -1;
        int     mPosition      = -1;
        Map<Integer,String> mUnitsClone = null;
        Map<Integer,String> mUnitsExecute = null;
        boolean mIsExecuting   = false;
    }

}
