package cn.emagsoftware.ui.recreate;

import java.util.LinkedList;

import android.app.Activity;

public final class RecreateManager
{

    private static LinkedList<Activity> mActivities = new LinkedList<Activity>();

    private RecreateManager()
    {
    }

    static boolean addRecreateActivity(Activity activity)
    {
        return mActivities.add(activity);
    }

    static boolean removeRecreateActivity(Activity activity)
    {
        return mActivities.remove(activity);
    }

    @SuppressWarnings("unchecked")
    public static void recreateAll()
    {
        LinkedList<Activity> curActivities = (LinkedList<Activity>) mActivities.clone();
        for (Activity activity : curActivities)
        {
            if (activity instanceof RecreateActivity)
            {
                ((RecreateActivity) activity).recreateMe();
            } else if (activity instanceof RecreateFragmentActivity)
            {
                ((RecreateFragmentActivity) activity).recreateMe();
            } else if (activity instanceof RecreateActionBarActivity)
            {
                ((RecreateActionBarActivity) activity).recreateMe();
            }
        }
    }

}
