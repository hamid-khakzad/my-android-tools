package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * <p>该类适合Fragment高度模块化、动态化的场景 <p>这种情况下，相同的Fragment可能会在并列或嵌套的情况下多次使用，甚至这种并列或嵌套是动态的，这就可能由于包含了相同id的ViewGroup导致操作Fragment发生错位，所以可以使用getNextUniqueId方法来严格区分ViewGroup
 * <p>getNextUniqueId方法的使用导致了Fragment在状态恢复时位置无法恢复，需要使用addUniqueId和getUniqueId方法来恢复ViewGroup的id以解决该问题 <p>状态恢复数据savedInstanceState由于会严格区分Fragment，所以并不会因为上面提到的并列或嵌套的原因导致恢复错位
 * 
 * @author Wendell
 */
public class UniqueIdFragment extends Fragment
{

    public static final String KEY_BUNDLE_ID = "KEY_BUNDLE_ID";
    private static int         uniqueId      = 1000;

    public static int getNextUniqueId()
    {
        return uniqueId++;
    }

    @Override
    public void setArguments(Bundle args)
    {
        // TODO Auto-generated method stub
        Bundle oldArgs = getArguments();
        if (args != null && args == oldArgs)
            return;
        if (args.containsKey(KEY_BUNDLE_ID))
            throw new IllegalArgumentException("arguments can not have a key named '" + KEY_BUNDLE_ID + "' in this case");
        Bundle ids = null;
        if (oldArgs != null)
            ids = oldArgs.getBundle(KEY_BUNDLE_ID);
        if (ids == null) // oldArgs不为null时ids也可能为null，因为oldArgs是完全暴露给外部的，可以理解为uniqueId的逻辑也可由另一种方式来控制
            ids = new Bundle();
        args.putBundle(KEY_BUNDLE_ID, ids);
        super.setArguments(args);
    }

    public void addUniqueId(String key, int id)
    {
        Bundle args = getArguments();
        Bundle ids = null;
        if (args == null)
        {
            args = new Bundle();
            super.setArguments(args);
            ids = new Bundle();
            args.putBundle(KEY_BUNDLE_ID, ids);
        } else
        {
            ids = args.getBundle(KEY_BUNDLE_ID);
            if (ids == null) // args不为null时ids也可能为null，因为args是完全暴露给外部的，可以理解为uniqueId的逻辑也可由另一种方式来控制
            {
                if (args.containsKey(KEY_BUNDLE_ID))
                    throw new IllegalArgumentException("arguments can not have a key named '" + KEY_BUNDLE_ID + "' in this case");
                ids = new Bundle();
                args.putBundle(KEY_BUNDLE_ID, ids);
            }
        }
        ids.putInt(key, id);
    }

    public int getUniqueId(String key)
    {
        Bundle args = getArguments();
        if (args == null)
            return View.NO_ID;
        Bundle ids = args.getBundle(KEY_BUNDLE_ID);
        if (ids == null) // args不为null时ids也可能为null，因为args是完全暴露给外部的，可以理解为uniqueId的逻辑也可由另一种方式来控制
            return View.NO_ID;
        return ids.getInt(key, View.NO_ID);
    }

}
