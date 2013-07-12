package cn.emagsoftware.ui.adapterview;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 13-6-17.
 */
public abstract class GroupDataHolder extends DataHolder
{

    private List<DataHolder>  mChildren       = null;
    private boolean mIsExpanded;

    public GroupDataHolder(Object data, int asyncDataCount)
    {
        super(data,asyncDataCount);
        mChildren = new ArrayList<DataHolder>();
    }

    public GroupDataHolder(Object data, int asyncDataCount, List<DataHolder> children)
    {
        super(data,asyncDataCount);
        if(children == null)
            throw new NullPointerException();
        mChildren = new ArrayList<DataHolder>(children);
    }

    public boolean isExpanded()
    {
        return mIsExpanded;
    }

    void setExpanded(boolean isExpanded)
    {
        mIsExpanded = isExpanded;
    }

    /**
     * <p>��Ҫ�ֶ�ˢ��</>
     * @param holder
     */
    public void addChild(DataHolder holder)
    {
        mChildren.add(holder);
    }

    /**
     * <p>��Ҫ�ֶ�ˢ��</>
     * @param location
     * @param holder
     */
    public void addChild(int location, DataHolder holder)
    {
        mChildren.add(location, holder);
    }

    /**
     * <p>��Ҫ�ֶ�ˢ��</>
     * @param holders
     */
    public void addChildren(List<DataHolder> holders)
    {
        mChildren.addAll(holders);
    }

    /**
     * <p>��Ҫ�ֶ�ˢ��</>
     * @param location
     * @param holders
     */
    public void addChildren(int location, List<DataHolder> holders)
    {
        mChildren.addAll(location, holders);
    }

    /**
     * <p>��Ҫ�ֶ�ˢ��</>
     * @param location
     */
    public void removeChild(int location)
    {
        mChildren.remove(location);
    }

    /**
     * <p>��Ҫ�ֶ�ˢ��</>
     * @param holder
     */
    public void removeChild(DataHolder holder)
    {
        mChildren.remove(holder);
    }

    public DataHolder queryChild(int location)
    {
        return mChildren.get(location);
    }

    public int queryChild(DataHolder holder)
    {
        return mChildren.indexOf(holder);
    }

    /**
     * <p>��Ҫ�ֶ�ˢ��</>
     */
    public void clearChildren()
    {
        mChildren.clear();
    }

    public int getChildrenCount()
    {
        return mChildren.size();
    }

}
