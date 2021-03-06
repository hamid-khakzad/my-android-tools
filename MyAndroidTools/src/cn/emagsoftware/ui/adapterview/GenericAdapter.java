package cn.emagsoftware.ui.adapterview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class GenericAdapter extends BaseAdapter
{

    Context                   mContext       = null;
    private List<DataHolder>  mHolders       = new ArrayList<DataHolder>();
    /** 是否循环显示View */
    private boolean           mIsLoopView    = false;
    /** View类型的个数 */
    private int               mViewTypeCount = 1;

    public GenericAdapter(Context context)
    {
        this(context, 1);
    }

    public GenericAdapter(Context context, int viewTypeCount)
    {
        if (context == null)
            throw new NullPointerException();
        if (viewTypeCount <= 0)
            throw new IllegalArgumentException("viewTypeCount should great than zero.");
        mContext = context;
        this.mViewTypeCount = viewTypeCount;
    }

    public GenericAdapter(Context context, List<DataHolder> holders)
    {
        this(context, holders, 1);
    }

    public GenericAdapter(Context context, List<DataHolder> holders, int viewTypeCount)
    {
        if (context == null)
            throw new NullPointerException();
        if (viewTypeCount <= 0)
            throw new IllegalArgumentException("viewTypeCount should great than zero.");
        mContext = context;
        if(holders != null)
            mHolders = new ArrayList<DataHolder>(holders);
        this.mViewTypeCount = viewTypeCount;
    }

    public void setDataHolders(List<DataHolder> holders) {
        if(holders == null)
            mHolders = new ArrayList<DataHolder>();
        else
            mHolders = new ArrayList<DataHolder>(holders);
        notifyDataSetChanged();
    }

    public void addDataHolder(DataHolder holder)
    {
        mHolders.add(holder);
        notifyDataSetChanged();
    }

    public void addDataHolder(int location, DataHolder holder)
    {
        if (mIsLoopView)
            location = getRealPosition(location);
        mHolders.add(location, holder);
        notifyDataSetChanged();
    }

    public void addDataHolders(List<DataHolder> holders)
    {
        mHolders.addAll(holders);
        notifyDataSetChanged();
    }

    public void addDataHolders(int location, List<DataHolder> holders)
    {
        if (mIsLoopView)
            location = getRealPosition(location);
        mHolders.addAll(location, holders);
        notifyDataSetChanged();
    }

    public void removeDataHolder(int location)
    {
        if (mIsLoopView)
            location = getRealPosition(location);
        mHolders.remove(location);
        notifyDataSetChanged();
    }

    public void removeDataHolder(DataHolder holder)
    {
        mHolders.remove(holder);
        notifyDataSetChanged();
    }

    public DataHolder queryDataHolder(int location)
    {
        if (mIsLoopView)
            location = getRealPosition(location);
        return mHolders.get(location);
    }

    public int queryDataHolder(DataHolder holder)
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

    public void setLoopView(boolean isLoopView)
    {
        mIsLoopView = isLoopView;
        notifyDataSetChanged();
    }

    public boolean isLoopView()
    {
        return mIsLoopView;
    }

    @Override
    public final int getCount()
    {
        int size = mHolders.size();
        if (size == 0)
            return size;
        if (mIsLoopView)
            return Integer.MAX_VALUE;
        else
            return size;
    }

    public int getRealCount()
    {
        return mHolders.size();
    }

    public int getRealPosition(int position)
    {
        return position % getRealCount();
    }

    public int getMiddleFirstPosition()
    {
        int realCount = getRealCount();
        if (realCount == 0)
            throw new UnsupportedOperationException("the count for adapter should not be zero");
        int middlePosition = Integer.MAX_VALUE / 2;
        while (middlePosition % realCount != 0)
        {
            middlePosition--;
        }
        return middlePosition;
    }

    @Override
    public final Object getItem(int position)
    {
        return queryDataHolder(position);
    }

    @Override
    public final long getItemId(int position)
    {
        if(position >= getCount()) return -1; // Resolve Android Bug
        return queryDataHolder(position).getId();
    }

    @Override
    public final int getItemViewType(int position)
    {
        return queryDataHolder(position).getType();
    }

    @Override
    public final int getViewTypeCount()
    {
        return mViewTypeCount;
    }

    @Override
    public final boolean hasStableIds() {
        return !mIsLoopView;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent)
    {
        DataHolder holder = queryDataHolder(position);
        View returnVal;
        if (convertView == null)
        {
            returnVal = holder.onCreateView(mContext, position, holder.getData());
        } else
        {
            returnVal = convertView;
            holder.onUpdateView(mContext, position, convertView, holder.getData());
        }
        return returnVal;
    }

}
