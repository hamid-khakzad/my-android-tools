package cn.emagsoftware.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

/**
 * Tab形式的布局类
 *
 * @deprecated 该类已舍弃，请使用实现更好的TabPager代替
 * @author Wendell
 * @version 4.5
 */
public class TabLayout extends ViewGroup
{

    public static final String      HEAD_POSITION_TOP      = "top";
    public static final String      HEAD_POSITION_BOTTOM   = "bottom";
    public static final String      HEAD_POSITION_LEFT     = "left";
    public static final String      HEAD_POSITION_RIGHT    = "right";

    protected Class<?>              tabClass               = Button.class;
    protected String                headPosition           = HEAD_POSITION_TOP;
    protected int                   selectedTabIndex       = -1;
    protected int                   tempSelectedTabIndex   = -1;

    protected List<View>            tabs                   = new ArrayList<View>();
    protected Map<Integer, View>    items                  = new HashMap<Integer, View>();

    protected OnTabChangedListener  mOnTabChangedListener  = null;
    protected OnAddFragmentListener mOnAddFragmentListener = null;

    public TabLayout(Context context)
    {
        this(context, null, 0);
    }

    public TabLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        if (attrs != null)
        {
            // TabLayout支持以下定义属性
            String tabClassName = attrs.getAttributeValue(null, "tabClass");
            String headPosition = attrs.getAttributeValue(null, "headPosition");
            String selectedTab = attrs.getAttributeValue(null, "selectedTab");
            try
            {
                if (tabClassName != null)
                    setTabClass(Class.forName(tabClassName));
            } catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            if (headPosition != null)
                setHeadPosition(headPosition);
            if (selectedTab != null)
                setSelectedTab(Integer.parseInt(selectedTab));
        }
    }

    public void setTabClass(Class<?> tabClass)
    {
        if (tabClass == null)
            throw new NullPointerException();
        this.tabClass = tabClass;
    }

    public void setHeadPosition(String headPosition)
    {
        if (headPosition == null)
            throw new NullPointerException();
        if (!headPosition.equals(HEAD_POSITION_TOP) && !headPosition.equals(HEAD_POSITION_BOTTOM) && !headPosition.equals(HEAD_POSITION_LEFT) && !headPosition.equals(HEAD_POSITION_RIGHT))
            throw new IllegalArgumentException("headPosition is invalid!");
        this.headPosition = headPosition;
    }

    /**
     * <p>刷新布局
     */
    protected ViewGroup refreshLayout()
    {
        if (getChildCount() != 2)
            throw new IllegalStateException("TabLayout can only contains two children(head and content)!");
        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        if (!(child1 instanceof ViewGroup))
            throw new IllegalStateException("TabLayout’s all children should be a ViewGroup!");
        if (!(child2 instanceof ViewGroup))
            throw new IllegalStateException("TabLayout’s all children should be a ViewGroup!");
        ViewGroup head = null;
        ViewGroup content = null;
        if (headPosition.equals(HEAD_POSITION_TOP) || headPosition.equals(HEAD_POSITION_LEFT))
        {
            head = (ViewGroup) child1;
            content = (ViewGroup) child2;
        } else if (headPosition.equals(HEAD_POSITION_BOTTOM) || headPosition.equals(HEAD_POSITION_RIGHT))
        {
            head = (ViewGroup) child2;
            content = (ViewGroup) child1;
        }
        tabs.clear();
        refreshTabs(head);
        int tabSize = tabs.size();
        int contentSize = content.getChildCount();
        if (mOnAddFragmentListener == null)
        {
            while (tabSize > contentSize)
            {
                tabs.remove(tabSize - 1).setOnClickListener(null);
                tabSize = tabs.size();
            }
            items.clear();
            for (int i = 0; i < contentSize; i++)
            {
                items.put(i, content.getChildAt(i));
            }
        } else
        {
            Iterator<View> entrys = items.values().iterator();
            while (entrys.hasNext())
            {
                View entry = entrys.next();
                if (entry != null)
                    entrys.remove();
            }
            for (int i = 0; i < contentSize; i++)
            {
                View child = content.getChildAt(i);
                if (!(child instanceof ViewGroup))
                    throw new IllegalStateException("in fragment mode,content children can only be created by Fragment");
                ViewGroup childGroup = (ViewGroup) child;
                if (childGroup.getChildCount() != 1)
                    throw new IllegalStateException("in fragment mode,content children can only be created by Fragment");
                Object indexObj = childGroup.getChildAt(0).getTag();
                if (!(indexObj instanceof Integer))
                    throw new IllegalStateException(
                            "in fragment mode,you should use setTag(tag) to set tab index in Fragment’s onCreateView(inflater,container,savedInstanceState) when onAddFragment is called back");
                int index = (Integer) indexObj;
                if (index < 0 || index >= tabSize)
                    throw new IllegalStateException("the tab index setting by setTag(tag) is out of tab size");
                if (items.get(index) != null)
                    throw new IllegalStateException("the tab index setting by setTag(tag) already exists");
                items.put(index, child);
            }
        }
        return content;
    }

    /**
     * <p>采用递归方式刷新布局中包含的所有Tab，需要在外部先清除原来tabs中的所有内容
     * 
     * @param view
     */
    protected void refreshTabs(View view)
    {
        if (view.getClass().equals(tabClass))
        {
            tabs.add(view);
            final int index = tabs.size() - 1;
            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // TODO Auto-generated method stub
                    setSelectedTab(index);
                }
            });
        } else if (view instanceof ViewGroup)
        {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++)
            {
                refreshTabs(vg.getChildAt(i));
            }
        }
    }

    protected void changeTabWhenLayout(final int index, boolean isIndexChanged)
    {
        for (int i = 0; i < tabs.size(); i++)
        {
            View tabView = tabs.get(i);
            View contentView = items.get(i);
            if (index == i)
            {
                if (tabView instanceof CompoundButton)
                    ((CompoundButton) tabView).setChecked(true);
                contentView.setVisibility(View.VISIBLE);
            } else
            {
                if (tabView instanceof CompoundButton)
                    ((CompoundButton) tabView).setChecked(false);
                if (contentView != null)
                    contentView.setVisibility(View.GONE);
            }
        }
        if (isIndexChanged)
        {
            this.selectedTabIndex = index;
            if (mOnTabChangedListener != null)
            {
                // 在当前layout过程中onTabChanged回调方法如果导致了requestLayout()将无法执行，故post处理
                new Handler().post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        mOnTabChangedListener.onTabChanged(tabs.get(index), items.get(index), index);
                    }
                });
            }
        }
    }

    public void setSelectedTab(int index)
    {
        if (index < 0)
            throw new IllegalArgumentException("index should equals or great than zero.");
        if (index == selectedTabIndex)
            return;
        this.tempSelectedTabIndex = index;
        requestLayout();
    }

    public int getSelectedTabIndex()
    {
        return selectedTabIndex;
    }

    public List<View> getTabs()
    {
        List<View> returnCopy = new ArrayList<View>(tabs.size());
        for (View view : tabs)
        {
            returnCopy.add(view);
        }
        return returnCopy;
    }

    public int getTabCount()
    {
        return tabs.size();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (headPosition.equals(HEAD_POSITION_TOP) || headPosition.equals(HEAD_POSITION_BOTTOM))
        {
            int firstHeight = 0;
            if (child1.getVisibility() != View.GONE)
            {
                int child1Width = child1.getMeasuredWidth();
                int child1Height = child1.getMeasuredHeight();
                child1.layout(paddingLeft, paddingTop, paddingLeft + child1Width, paddingTop + child1Height);
                firstHeight = child1Height;
            }
            if (child2.getVisibility() != View.GONE)
            {
                child2.layout(paddingLeft, paddingTop + firstHeight, paddingLeft + child2.getMeasuredWidth(), paddingTop + firstHeight + child2.getMeasuredHeight());
            }
        } else if (headPosition.equals(HEAD_POSITION_LEFT) || headPosition.equals(HEAD_POSITION_RIGHT))
        {
            int firstWidth = 0;
            if (child1.getVisibility() != View.GONE)
            {
                int child1Width = child1.getMeasuredWidth();
                int child1Height = child1.getMeasuredHeight();
                child1.layout(paddingLeft, paddingTop, paddingLeft + child1Width, paddingTop + child1Height);
                firstWidth = child1Width;
            }
            if (child2.getVisibility() != View.GONE)
            {
                child2.layout(paddingLeft + firstWidth, paddingTop, paddingLeft + firstWidth + child2.getMeasuredWidth(), paddingTop + child2.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 非EXACTLY模式需要根据子容器来计算父容器的大小，对TabLayout而言比较复杂且实际应用中完全可以避免，故暂不支持
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException("TabLayout only can run at EXACTLY mode!");
        int wrapWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int wrapHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = wrapWidthSize - getPaddingLeft() - getPaddingRight();
        int heightSize = wrapHeightSize - getPaddingTop() - getPaddingBottom();

        // 刷新布局
        ViewGroup content = refreshLayout();

        // 调整Tab(View.setVisibility(visibility))操作可能会影响子View的measure方法，故在当前onMeasure方法而不是onLayout中执行
        int tabSize = tabs.size();
        if (tempSelectedTabIndex == -1)
        {
            if (tabSize > 0)
            {
                View view = items.get(0);
                if (view == null)
                {
                    if (!items.containsKey(0))
                    {
                        // 添加Fragment的操作在layout过程中执行同样能请求到新的layout，故无需post处理
                        mOnAddFragmentListener.onAddFragment(0, content);
                        items.put(0, null);
                    }
                } else
                {
                    tempSelectedTabIndex = 0;
                    changeTabWhenLayout(tempSelectedTabIndex, true);
                }
            }
        } else
        {
            if (tempSelectedTabIndex >= tabSize)
            {
                int tempSelectedTabIndexCopy = tempSelectedTabIndex;
                tempSelectedTabIndex = selectedTabIndex;
                throw new IllegalStateException("tab index is out of range:" + tempSelectedTabIndexCopy + "!");
            } else
            {
                View view = items.get(tempSelectedTabIndex);
                if (view == null)
                {
                    if (!items.containsKey(tempSelectedTabIndex))
                    {
                        // 添加Fragment的操作在layout过程中执行同样能请求到新的layout，故无需post处理
                        mOnAddFragmentListener.onAddFragment(tempSelectedTabIndex, content);
                        items.put(tempSelectedTabIndex, null);
                    }
                } else
                {
                    changeTabWhenLayout(tempSelectedTabIndex, tempSelectedTabIndex != selectedTabIndex);
                }
            }
        }

        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        if (headPosition.equals(HEAD_POSITION_TOP))
        {
            int remainHeight = heightSize;
            if (child1.getVisibility() != View.GONE)
            {
                LayoutParams lp = child1.getLayoutParams();
                if (lp.height == LayoutParams.FILL_PARENT || lp.height == LayoutParams.WRAP_CONTENT)
                {
                    child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
                } else
                {
                    child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
                }
                remainHeight = heightSize - child1.getMeasuredHeight();
                if (remainHeight < 0)
                    remainHeight = 0;
            }
            if (child2.getVisibility() != View.GONE)
            {
                child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(remainHeight, MeasureSpec.EXACTLY));
            }
        } else if (headPosition.equals(HEAD_POSITION_BOTTOM))
        {
            int remainHeight = heightSize;
            if (child2.getVisibility() != View.GONE)
            {
                LayoutParams lp = child2.getLayoutParams();
                if (lp.height == LayoutParams.FILL_PARENT || lp.height == LayoutParams.WRAP_CONTENT)
                {
                    child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
                } else
                {
                    child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
                }
                remainHeight = heightSize - child2.getMeasuredHeight();
                if (remainHeight < 0)
                    remainHeight = 0;
            }
            if (child1.getVisibility() != View.GONE)
            {
                child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(remainHeight, MeasureSpec.EXACTLY));
            }
        } else if (headPosition.equals(HEAD_POSITION_LEFT))
        {
            int remainWidth = widthSize;
            if (child1.getVisibility() != View.GONE)
            {
                LayoutParams lp = child1.getLayoutParams();
                if (lp.width == LayoutParams.FILL_PARENT || lp.width == LayoutParams.WRAP_CONTENT)
                {
                    child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                } else
                {
                    child1.measure(MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                }
                remainWidth = widthSize - child1.getMeasuredWidth();
                if (remainWidth < 0)
                    remainWidth = 0;
            }
            if (child2.getVisibility() != View.GONE)
            {
                child2.measure(MeasureSpec.makeMeasureSpec(remainWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
            }
        } else if (headPosition.equals(HEAD_POSITION_RIGHT))
        {
            int remainWidth = widthSize;
            if (child2.getVisibility() != View.GONE)
            {
                LayoutParams lp = child2.getLayoutParams();
                if (lp.width == LayoutParams.FILL_PARENT || lp.width == LayoutParams.WRAP_CONTENT)
                {
                    child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                } else
                {
                    child2.measure(MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
                }
                remainWidth = widthSize - child2.getMeasuredWidth();
                if (remainWidth < 0)
                    remainWidth = 0;
            }
            if (child1.getVisibility() != View.GONE)
            {
                child1.measure(MeasureSpec.makeMeasureSpec(remainWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
            }
        }
        setMeasuredDimension(wrapWidthSize, wrapHeightSize);
    }

    public void setOnTabChangedListener(OnTabChangedListener mOnTabChangedListener)
    {
        this.mOnTabChangedListener = mOnTabChangedListener;
    }

    public void setUseFragmentMode(OnAddFragmentListener mOnAddFragmentListener)
    {
        this.mOnAddFragmentListener = mOnAddFragmentListener;
    }

    public interface OnTabChangedListener
    {
        public void onTabChanged(View tab, View contentItem, int index);
    }

    public interface OnAddFragmentListener
    {
        /**
         * <p>在Fragment状态恢复或指定Tab Index的Fragment已添加的情况下，该方法将不再被调用 <p><b>一定要注意</b>，添加的Fragment其onCreateView方法返回的View需要通过setTag方法来设置当前的Tab Index
         * 
         * @param index
         * @param container
         */
        public void onAddFragment(int index, ViewGroup container);
    }

}
