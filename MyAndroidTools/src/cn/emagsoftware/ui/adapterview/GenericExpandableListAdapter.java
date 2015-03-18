package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 13-6-17.
 */
public class GenericExpandableListAdapter extends BaseExpandableListAdapter
{

    Context mContext       = null;
    private List<GroupDataHolder> mHolders = new ArrayList<GroupDataHolder>();
    private int mGroupTypeCount = 1;
    private int mChildTypeCount = 1;

    public GenericExpandableListAdapter(Context context)
    {
        this(context,1,1);
    }

    public GenericExpandableListAdapter(Context context,int groupTypeCount,int childTypeCount) {
        if(context == null)
            throw new NullPointerException();
        if (groupTypeCount <= 0)
            throw new IllegalArgumentException("groupTypeCount should great than zero.");
        if (childTypeCount <= 0)
            throw new IllegalArgumentException("childTypeCount should great than zero.");
        mContext = context;
        this.mGroupTypeCount = groupTypeCount;
        this.mChildTypeCount = childTypeCount;
    }

    public GenericExpandableListAdapter(Context context,List<GroupDataHolder> holders)
    {
        this(context,holders,1,1);
    }

    public GenericExpandableListAdapter(Context context,List<GroupDataHolder> holders,int groupTypeCount,int childTypeCount) {
        if(context == null)
            throw new NullPointerException();
        if (groupTypeCount <= 0)
            throw new IllegalArgumentException("groupTypeCount should great than zero.");
        if (childTypeCount <= 0)
            throw new IllegalArgumentException("childTypeCount should great than zero.");
        mContext = context;
        if(holders != null)
            mHolders = new ArrayList<GroupDataHolder>(holders);
        this.mGroupTypeCount = groupTypeCount;
        this.mChildTypeCount = childTypeCount;
    }

    public void setDataHolders(List<GroupDataHolder> holders) {
        if(holders == null)
            mHolders = new ArrayList<GroupDataHolder>();
        else
            mHolders = new ArrayList<GroupDataHolder>(holders);
        notifyDataSetChanged();
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

    /**
     * @deprecated use setDataHolders(null) instead.
     */
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
        if(i >= getGroupCount()) return -1; // Resolve Android Bug
        return queryDataHolder(i).getId();
    }

    @Override
    public final long getChildId(int i, int i2) {
        if(i >= getGroupCount()) return -1; // Resolve Android Bug
        GroupDataHolder holder = queryDataHolder(i);
        if(i2 >= holder.getChildrenCount()) return -1; // Resolve Android Bug
        return holder.queryChild(i2).getId();
    }

    @Override
    public int getGroupTypeCount() {
        return mGroupTypeCount;
    }

    @Override
    public int getChildTypeCount() {
        return mChildTypeCount;
    }

    @Override
    public int getGroupType(int groupPosition) {
        return queryDataHolder(groupPosition).getType();
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return queryDataHolder(groupPosition).queryChild(childPosition).getType();
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
        if (view == null)
        {
            returnVal = holder.onCreateView(mContext, i, holder.getData());
        } else
        {
            returnVal = view;
            holder.onUpdateView(mContext, i, view, holder.getData());
        }
        return returnVal;
    }

    @Override
    public final View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
        DataHolder holder = queryDataHolder(i).queryChild(i2);
        View returnVal;
        if (view == null)
        {
            returnVal = holder.onCreateView(mContext, i2, holder.getData());
        } else
        {
            returnVal = view;
            holder.onUpdateView(mContext, i2, view, holder.getData());
        }
        return returnVal;
    }

    @Override
    public final boolean isChildSelectable(int i, int i2) {
        return true;
    }

}
