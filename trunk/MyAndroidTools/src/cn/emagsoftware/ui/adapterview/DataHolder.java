package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>当前类没有提供setData方法，因为这可能会影响异步数据加载机制，如果需要改变data，可使用新data来创建新的DataHolder实例
 * 
 * @author Wendell
 */
public abstract class DataHolder
{

    /**丢弃的异步数据仍有可能因为存在外部引用而不能即时被回收，所以这里的GLOBAL_CACHE被认作为单个AdapterView的最大缓存则更为合适*/
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
     * <p>构造函数
     * 
     * @param data 需要用到的数据
     * @param asyncDataCount 需要被执行的异步数据的个数
     */
    public DataHolder(Object data, int asyncDataCount)
    {
        if(asyncDataCount < 0)
            throw new IllegalArgumentException("asyncDataCount < 0");
        mData = data;
        mAsyncDataCount = asyncDataCount;
    }

    /**
     * <p>使用当前data创建View时触发 <p>如果存在异步数据的加载，在创建View时需通过getAsyncData(int index)是否为null来判断所有异步数据是否加载完成，若加载完成，也要更新到View上 <p>可以通过ViewHolder来绑定View的结构信息，从而提高更新时的效率
     * 
     * @param context
     * @param position
     * @param data
     * @return
     */
    public abstract View onCreateView(Context context, int position, Object data);

    /**
     * <p>使用当前data更新View时触发，出于节约资源的考虑，View默认会被复用，此时只需要更新View即可 <p>如果存在异步数据的加载，在更新View时需通过getAsyncData(int index)是否为null来判断所有异步数据是否加载完成，若加载完成，也要更新到View上 <p>若通过GenericAdapter的构造函数设置了View不复用，可保持该方法的实现为空
     * <p>更新View可以通过ViewHolder来提高效率
     * 
     * @param context
     * @param position
     * @param view
     * @param data
     */
    public abstract void onUpdateView(Context context, int position, View view, Object data);

    /**
     * <p>当前的某一个异步数据被执行成功后触发，若当前的异步数据个数为0，可保持该方法的实现为空
     * 
     * @param context
     * @param position
     * @param view
     * @param asyncData
     * @param asyncDataIndex
     */
    public abstract void onAsyncDataExecuted(Context context, int position, View view, Object asyncData, int asyncDataIndex);

    /**
     * <p>如果DataHolder需要产生不同的View，则需要覆盖该方法以提供不同类型的数值表示。该方法需要和GenericAdapter带有viewTypeCount参数的构造函数一起使用
     * 
     * @return
     */
    public int getType()
    {
        return 0;
    }

    /**
     * <p>获取构造函数中传入的数据对象
     * 
     * @return
     */
    public Object getData()
    {
        return mData;
    }

    /**
     * <p>获取指定的异步数据，未加载或已被丢弃时返回null</>
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
     * <p>内部方法：获取指定的异步数据</>
     * @param globalId
     * @return
     */
    Object getAsyncData(String globalId)
    {
        return GLOBAL_CACHE.get(globalId);
    }

    /**
     * <p>内部方法：设置指定的异步数据</>
     * @param globalId
     * @param asyncData
     */
    void setAsyncData(String globalId, Object asyncData)
    {
        GLOBAL_CACHE.put(globalId,asyncData);
    }

    /**
     * <p>获取异步数据的个数
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
