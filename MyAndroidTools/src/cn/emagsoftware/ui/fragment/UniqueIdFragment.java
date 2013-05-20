package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * <p>该类适合Fragment高度模块化、动态化的场景 <p>这种情况下，相同的Fragment可能会在并列或嵌套的情况下多次使用，甚至这种并列或嵌套是动态的，这就可能由于包含了相同id的ViewGroup导致操作Fragment发生错位，所以可以使用getNextUniqueId方法来严格区分ViewGroup
 * <p>getNextUniqueId方法的使用导致了Fragment在状态恢复时位置无法恢复，需要使用addUniqueId和getUniqueId方法来恢复ViewGroup的id以解决该问题 <p>状态恢复数据savedInstanceState由于会严格区分Fragment，所以并不会因为上面提到的并列或嵌套的原因导致恢复错位
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
        initIds(savedInstanceState);
    }

    private void initIds(Bundle savedInstanceState)
    {
        if (!isIdsInit)
        {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        View returnVal = super.onCreateView(inflater, container, savedInstanceState);
        initIds(savedInstanceState);
        return returnVal;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        initIds(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        initIds(savedInstanceState);
    }

    public void addUniqueId(String key, int id)
    {
        ids.putInt(key, id);
    }

    public int getUniqueId(String key)
    {
        if (!isIdsInit)
            throw new IllegalStateException("can not call this method before ids is initialized");
        return ids.getInt(key, View.NO_ID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        if (!isIdsInit)
            throw new IllegalStateException("can not call this method before ids is initialized");
        outState.putBundle(KEY_BUNDLE_IDS, ids);
    }

}
