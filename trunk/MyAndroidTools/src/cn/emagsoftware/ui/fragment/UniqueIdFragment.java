package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * <p>�����ʺ�Fragment�߶�ģ�黯����̬���ĳ��� <p>��������£���ͬ��Fragment���ܻ��ڲ��л�Ƕ�׵�����¶��ʹ�ã��������ֲ��л�Ƕ���Ƕ�̬�ģ���Ϳ������ڰ�������ͬid��ViewGroup���²���Fragment������λ�����Կ���ʹ��getNextUniqueId�������ϸ�����ViewGroup
 * <p>getNextUniqueId������ʹ�õ�����Fragment��״̬�ָ�ʱλ���޷��ָ�����Ҫʹ��addUniqueId��getUniqueId�������ָ�ViewGroup��id�Խ�������� <p>״̬�ָ�����savedInstanceState���ڻ��ϸ�����Fragment�����Բ�������Ϊ�����ᵽ�Ĳ��л�Ƕ�׵�ԭ���»ָ���λ
 * 
 * @author Wendell
 */
public class UniqueIdFragment extends Fragment
{

    private static int          uniqueId       = 1000;
    private static final String KEY_BUNDLE_IDS = "UniqueIdFragment.KEY_BUNDLE_IDS";
    private boolean             isIdsInit      = false;
    private Bundle              ids            = new Bundle();

    public static int getNextUniqueId()
    {
        return uniqueId++;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            Bundle oldIds = savedInstanceState.getBundle(KEY_BUNDLE_IDS);
            if (oldIds == null)
                throw new IllegalStateException("please call super method when you override 'onSaveInstanceState'");
            oldIds.putAll(ids);
            this.ids = oldIds;
        }
        isIdsInit = true;
    }

    public void addUniqueId(String key, int id)
    {
        ids.putInt(key, id);
    }

    public int getUniqueId(String key)
    {
        if (!isIdsInit)
            throw new IllegalStateException("can not call this method before 'onCreate'");
        return ids.getInt(key, View.NO_ID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        if (!isIdsInit)
            throw new IllegalStateException("can not call this method before 'onCreate'");
        outState.putBundle(KEY_BUNDLE_IDS, ids);
    }

}
