package cn.emagsoftware.ui.adapterview;

import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.WrapperListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wendell on 13-7-8.
 */
public final class AsyncDataManager {

    private AsyncDataManager(){}

    public static void computeAsyncData(AdapterView<? extends Adapter> view,AsyncDataExecutor executor)
    {
        if(view == null || executor == null)
            throw new NullPointerException();
        Object adapter = null;
        if(view instanceof ExpandableListView)
        {
            adapter = ((ExpandableListView)view).getExpandableListAdapter();
        }else
        {
            adapter = view.getAdapter();
            if(adapter instanceof WrapperListAdapter)
                adapter = ((WrapperListAdapter)adapter).getWrappedAdapter();
        }
        if(adapter == null)
            throw new IllegalStateException("please call this method after 'setAdapter(adapter)' is called.");
        int firstPos = view.getFirstVisiblePosition();
        int lastPos = view.getLastVisiblePosition();
        List<DataHolder> holders = null;
        if(adapter instanceof GenericAdapter)
        {
            GenericAdapter adapterPoint = (GenericAdapter)adapter;
            if(view instanceof ListView)
            {
                int headerCount = ((ListView)view).getHeaderViewsCount();
                firstPos = firstPos - headerCount;
                lastPos = lastPos - headerCount;
            }
            if(firstPos < 0)
                firstPos = 0;
            int count = adapterPoint.getCount();
            if(++lastPos > count)
                lastPos = count;
            if(firstPos <= lastPos)
                holders = adapterPoint.queryDataHolders(firstPos,lastPos);
        }else if(adapter instanceof GenericExpandableListAdapter)
        {

        }else
        {
            throw new IllegalStateException("the adapter for AdapterView can only be GenericAdapter or GenericExpandableListAdapter.");
        }







    }

}
