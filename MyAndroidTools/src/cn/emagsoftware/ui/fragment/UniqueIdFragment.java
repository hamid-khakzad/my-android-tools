package cn.emagsoftware.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

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
