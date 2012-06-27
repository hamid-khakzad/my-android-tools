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
     * <p>构造函数
     * 
     * @param data 需要用到的数据
     * @param asyncDataCount 需要被执行的异步数据的个数
     */
    public DataHolder(Object data, int asyncDataCount)
    {
        mData = data;
        mAsyncData = new Object[asyncDataCount];
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
     * @param asyncData 执行成功后的异步数据，只能直接使用该值而不能使用getAsyncData(asyncDataIndex)，因为异步数据在全部执行完后会修改为软引用，只有该值可以确保对异步数据的绝对引用
     * @param asyncDataIndex
     */
    public abstract void onAsyncDataExecuted(Context context, int position, View view, Object asyncData, int asyncDataIndex);

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
     * <p>重设构造函数中传入的数据对象
     * 
     * @param data
     */
    public void setData(Object data)
    {
        mData = data;
    }

    /**
     * <p>获取指定位置的异步数据，未加载或已被回收时返回null
     * 
     * @param index 异步数据的位置
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
     * <p>内部方法：设置指定位置的异步数据
     * 
     * @param index 异步数据的位置
     * @param asyncData
     */
    void setAsyncData(int index, Object asyncData)
    {
        if (asyncData instanceof SoftReference<?>)
            throw new IllegalArgumentException("asyncData can not be a type of SoftReference which is used by itself");
        mAsyncData[index] = asyncData;
    }

    /**
     * <p>内部方法：把指定位置的异步数据调整为弱引用，方便GC进行回收
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
     * <p>获取异步数据的个数
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
