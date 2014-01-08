package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 13-6-17.
 */
public class GenericExpandableListAdapter extends BaseExpandableListAdapter
{

    Context mContext       = null;
    private List<GroupDataHolder> mHolders = null;
    /** 异步数据的执行对象 */
    private AsyncDataExecutor mExecutor      = null;
    private Handler mHandler = new Handler();
    private ViewGroup mRunView = null;

    public GenericExpandableListAdapter(Context context)
    {
        if(context == null)
            throw new NullPointerException();
        mContext = context;
        mHolders = new ArrayList<GroupDataHolder>();
    }

    public GenericExpandableListAdapter(Context context,List<GroupDataHolder> holders)
    {
        if(context == null || holders == null)
            throw new NullPointerException();
        mContext = context;
        mHolders = new ArrayList<GroupDataHolder>(holders);
    }

    public void bindAsyncDataExecutor(AsyncDataExecutor executor)
    {
        mExecutor = executor;
    }

    public void addDataHolder(GroupDataHolder holder)
    {
        mHolders.add(holder);
        notifyDataSetChanged();
    }

    public void addDataHolder(int location, GroupDataHolder holder)
    {
        mHolders.add(location, holder);
        notifyDataSetChanged();
    }

    public void addDataHolders(List<GroupDataHolder> holders)
    {
        mHolders.addAll(holders);
        notifyDataSetChanged();
    }

    public void addDataHolders(int location, List<GroupDataHolder> holders)
    {
        mHolders.addAll(location, holders);
        notifyDataSetChanged();
    }

    public void removeDataHolder(int location)
    {
        mHolders.remove(location);
        notifyDataSetChanged();
    }

    public void removeDataHolder(GroupDataHolder holder)
    {
        mHolders.remove(holder);
        notifyDataSetChanged();
    }

    public GroupDataHolder queryDataHolder(int location)
    {
        return mHolders.get(location);
    }

    public int queryDataHolder(GroupDataHolder holder)
    {
        return mHolders.indexOf(holder);
    }

    public void clearDataHolders()
    {
        mHolders.clear();
        notifyDataSetChanged();
    }

    @Override
    public final int getGroupCount() {
        return mHolders.size();
    }

    @Override
    public final int getChildrenCount(int i) {
        return queryDataHolder(i).getChildrenCount();
    }

    @Override
    public final Object getGroup(int i) {
        return queryDataHolder(i);
    }

    @Override
    public final Object getChild(int i, int i2) {
        return queryDataHolder(i).queryChild(i2);
    }

    @Override
    public final long getGroupId(int i) {
        return i;
    }

    @Override
    public final long getChildId(int i, int i2) {
        return i2;
    }

    @Override
    public final boolean hasStableIds() {
        return true;
    }

    @Override
    public final View getGroupView(int i, boolean b, View view, final ViewGroup viewGroup) {
        GroupDataHolder holder = queryDataHolder(i);
        View returnVal;
        holder.setExpanded(b);
        holder.mExecuteConfig.mGroupPosition = -1;
        holder.mExecuteConfig.mPosition = i;
        if (view == null)
        {
            returnVal = holder.onCreateView(mContext, i, holder.getData());
        } else
        {
            returnVal = view;
            holder.onUpdateView(mContext, i, view, holder.getData());
        }
        if(mExecutor != null)
        {
            if(mRunView != viewGroup)
            {
                mRunView = viewGroup;
                final AsyncDataExecutor curExecutor = mExecutor;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AsyncDataManager.computeAsyncData((AdapterView<? extends Adapter>)viewGroup,curExecutor);
                        if(mRunView == viewGroup)
                            mRunView = null;
                    }
                },1000);
            }
        }
        return returnVal;
    }

    @Override
    public final View getChildView(int i, int i2, boolean b, View view, final ViewGroup viewGroup) {
        DataHolder holder = queryDataHolder(i).queryChild(i2);
        View returnVal;
        holder.mExecuteConfig.mGroupPosition = i;
        holder.mExecuteConfig.mPosition = i2;
        if (view == null)
        {
            returnVal = holder.onCreateView(mContext, i2, holder.getData());
        } else
        {
            returnVal = view;
            holder.onUpdateView(mContext, i2, view, holder.getData());
        }
        if(mExecutor != null)
        {
            if(mRunView != viewGroup)
            {
                mRunView = viewGroup;
                final AsyncDataExecutor curExecutor = mExecutor;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AsyncDataManager.computeAsyncData((AdapterView<? extends Adapter>)viewGroup,curExecutor);
                        if(mRunView == viewGroup)
                            mRunView = null;
                    }
                },1000);
            }
        }
        return returnVal;
    }

    @Override
    public final boolean isChildSelectable(int i, int i2) {
        return true;
    }

}
