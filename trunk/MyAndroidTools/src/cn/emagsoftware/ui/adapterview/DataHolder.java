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
     * <p>���캯��
     * 
     * @param data ��Ҫ�õ�������
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
     * <p>ʹ�õ�ǰdata����Viewʱ���� <p>����ͨ��ViewHolder����View�Ľṹ��Ϣ���Ӷ���߸���ʱ��Ч��
     * 
     * @param context
     * @param position
     * @param data
     * @return
     */
    public abstract View onCreateView(Context context, int position, Object data);

    /**
     * <p>ʹ�õ�ǰdata����Viewʱ���������ڽ�Լ��Դ�Ŀ��ǣ�View�ᱻǿ�Ƹ��ã���ʱֻ��Ҫ����View����
     * <p>����View����ͨ��ViewHolder�����Ч��
     * 
     * @param context
     * @param position
     * @param view
     * @param data
     */
    public abstract void onUpdateView(Context context, int position, View view, Object data);

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

    public DisplayImageOptions[] getDisplayImageOptions()
    {
        return mOptions;
    }

}
