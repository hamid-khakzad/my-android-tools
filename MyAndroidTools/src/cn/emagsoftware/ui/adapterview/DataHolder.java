package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * <p>DataHolder
 * 
 * @author Wendell
 */
public abstract class DataHolder
{

    private Object   mData          = null;
    private DisplayImageOptions[] mOptions = null;

    /**
     * <p>构造函数
     * 
     * @param data 需要用到的数据
     */
    public DataHolder(Object data)
    {
        mData = data;
    }

    public DataHolder(Object data,DisplayImageOptions... options)
    {
        mData = data;
        mOptions = options;
    }

    /**
     * <p>使用当前data创建View时触发 <p>可以通过ViewHolder来绑定View的结构信息，从而提高更新时的效率
     * 
     * @param context
     * @param position
     * @param data
     * @return
     */
    public abstract View onCreateView(Context context, int position, Object data);

    /**
     * <p>使用当前data更新View时触发，出于节约资源的考虑，View会被强制复用，此时只需要更新View即可
     * <p>更新View可以通过ViewHolder来提高效率
     * 
     * @param context
     * @param position
     * @param view
     * @param data
     */
    public abstract void onUpdateView(Context context, int position, View view, Object data);

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

    public DisplayImageOptions[] getDisplayImageOptions()
    {
        return mOptions;
    }

}
