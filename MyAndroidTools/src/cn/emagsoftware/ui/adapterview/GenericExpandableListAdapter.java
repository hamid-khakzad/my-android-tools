package cn.emagsoftware.ui.adapterview;

import android.content.Context;
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
    private boolean           mIsConvertView = true;
    /** 异步数据的执行对象 */
    private AsyncDataExecutor mExecutor      = null;

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

    /**
     * <p>Android 2.1及以前版本不支持多View Type，通过调用setConvertView(false)可以对老版本的多View Type情况提供一个稍欠理想的解决方案</>
     * @param isConvertView
     */
    public void setConvertView(boolean isConvertView)
    {
        mIsConvertView = isConvertView;
    }

    public boolean isConvertView()
    {
        return mIsConvertView;
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
    public final View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        GroupDataHolder holder = queryDataHolder(i);
        View returnVal;
        holder.setExpanded(b);
        holder.mExecuteConfig.mShouldExecute = false;
        if (view == null || !mIsConvertView)
        {
            returnVal = holder.onCreateView(mContext, i, holder.getData());
        } else
        {
            returnVal = view;
            holder.onUpdateView(mContext, i, view, holder.getData());
        }
        if (mExecutor != null)
        {
            mExecutor.refreshVariables((AdapterView<? extends Adapter>) viewGroup, this);
            holder.mExecuteConfig.mGroupPosition = -1;
            holder.mExecuteConfig.mPosition = i;
            if (holder.mExecuteConfig.mShouldExecute)
                mExecutor.pushAsync(holder);
        }
        return returnVal;
    }

    @Override
    public final View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
        DataHolder holder = queryDataHolder(i).queryChild(i2);
        View returnVal;
        holder.mExecuteConfig.mShouldExecute = false;
        if (view == null || !mIsConvertView)
        {
            returnVal = holder.onCreateView(mContext, i2, holder.getData());
        } else
        {
            returnVal = view;
            holder.onUpdateView(mContext, i2, view, holder.getData());
        }
        if (mExecutor != null)
        {
            mExecutor.refreshVariables((AdapterView<? extends Adapter>) viewGroup, this);
            holder.mExecuteConfig.mGroupPosition = i;
            holder.mExecuteConfig.mPosition = i2;
            if (holder.mExecuteConfig.mShouldExecute)
                mExecutor.pushAsync(holder);
        }
        return returnVal;
    }

    @Override
    public final boolean isChildSelectable(int i, int i2) {
        return true;
    }

}
