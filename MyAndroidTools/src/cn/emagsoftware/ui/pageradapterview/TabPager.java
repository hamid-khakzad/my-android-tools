package cn.emagsoftware.ui.pageradapterview;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cn.emagsoftware.util.LogManager;

public class TabPager extends ViewGroup {
    private static final boolean DEBUG = false;

    static class ItemInfo {
        Object object;
        int position;
    }

    private final ArrayList<ItemInfo> mItems = new ArrayList<ItemInfo>();

    private PagerAdapter mAdapter;
    private int mCurItem;   // Index of currently displayed page.
    private int mCurTempItem;
    private int mRestoredCurItem = -1;
    private Parcelable mRestoredAdapterState = null;
    private ClassLoader mRestoredClassLoader = null;
    private PagerObserver mObserver;

    private OnTabChangeListener mOnTabChangeListener;

    public interface OnTabChangeListener {

        public void onTabSelected(int position);

    }

    public TabPager(Context context) {
        super(context);
    }

    public TabPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set a PagerAdapter that will supply views for this pager as needed.
     *
     * @param adapter Adapter to use
     */
    public void setAdapter(PagerAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
            mAdapter.startUpdate(this);
            for (int i = 0; i < mItems.size(); i++) {
                final ItemInfo ii = mItems.get(i);
                mAdapter.destroyItem(this, ii.position, ii.object);
            }
            mAdapter.finishUpdate(this);
            mItems.clear();
            removeAllViews();
            mCurItem = 0;
            mCurTempItem = 0;
        }

        mAdapter = adapter;

        if (mAdapter != null) {
            if (mObserver == null) {
                mObserver = new PagerObserver();
            }
            mAdapter.registerDataSetObserver(mObserver);
            if (mRestoredCurItem >= 0) {
                mAdapter.restoreState(mRestoredAdapterState, mRestoredClassLoader);
                mCurTempItem = mRestoredCurItem;
                mRestoredCurItem = -1;
                mRestoredAdapterState = null;
                mRestoredClassLoader = null;
            }
            requestLayout();
        }
    }

    public PagerAdapter getAdapter() {
        return mAdapter;
    }

    public void setCurrentItem(int item) {
        if (mAdapter == null) {
            return;
        }
        if (item >= mAdapter.getCount()) {
            item = mAdapter.getCount() - 1;
        }
        if (item < 0) {
            item = 0;
        }
        if (mCurItem == item) {
            return;
        }
        mCurTempItem = item;
        requestLayout();
    }

    public int getCurrentItem() {
        return mCurItem;
    }

    public void setOnTabChangeListener(OnTabChangeListener listener) {
        mOnTabChangeListener = listener;
    }

    ItemInfo addNewItem(int position) {
        ItemInfo ii = new ItemInfo();
        ii.position = position;
        ii.object = mAdapter.instantiateItem(this, position);
        mItems.add(ii);
        return ii;
    }

    void dataSetChanged() {
        // This method only gets called if our observer is attached, so mAdapter is non-null.

        int newCurrItem = -1;

        boolean isUpdating = false;
        for (int i = 0; i < mItems.size(); i++) {
            final ItemInfo ii = mItems.get(i);
            final int newPos = mAdapter.getItemPosition(ii.object);

            if (newPos == PagerAdapter.POSITION_UNCHANGED) {
                continue;
            }

            if (newPos == PagerAdapter.POSITION_NONE) {
                mItems.remove(i);
                i--;

                if (!isUpdating) {
                    mAdapter.startUpdate(this);
                    isUpdating = true;
                }

                mAdapter.destroyItem(this, ii.position, ii.object);

                if (mCurItem == ii.position) {
                    // Keep the current item in the valid range
                    newCurrItem = Math.max(0, Math.min(mCurItem, mAdapter.getCount() - 1));
                }
                continue;
            }

            if (ii.position != newPos) {
                if (ii.position == mCurItem) {
                    // Our current item changed position. Follow it.
                    newCurrItem = newPos;
                }

                ii.position = newPos;
            }
        }

        if (isUpdating) {
            mAdapter.finishUpdate(this);
        }

        if (newCurrItem >= 0) {
            mCurTempItem = newCurrItem;
            requestLayout();
        }
    }

    ItemInfo populate() {
        mAdapter.startUpdate(this);

        ItemInfo curItem = null;
        for (int i=0; i<mItems.size(); i++) {
            ItemInfo ii = mItems.get(i);
            if (ii.position == mCurTempItem) {
                curItem = ii;
                break;
            }
        }

        if(curItem == null)
        {
            if(mCurTempItem < mAdapter.getCount())
                curItem = addNewItem(mCurTempItem);
            else
                mCurTempItem = 0;
        }

        mAdapter.setPrimaryItem(this, mCurTempItem, curItem != null ? curItem.object : null);

        mAdapter.finishUpdate(this);

        return curItem;
    }

    /**
     * This is the persistent state that is saved by ViewPager.  Only needed
     * if you are creating a sublass of ViewPager that must save its own
     * state, in which case it should implement a subclass of this which
     * contains that state.
     */
    public static class SavedState extends BaseSavedState {
        int position;
        Parcelable adapterState;
        ClassLoader loader;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(position);
            out.writeParcelable(adapterState, flags);
        }

        @Override
        public String toString() {
            return "FragmentPager.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " position=" + position + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }
            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });

        SavedState(Parcel in, ClassLoader loader) {
            super(in);
            if (loader == null) {
                loader = getClass().getClassLoader();
            }
            position = in.readInt();
            adapterState = in.readParcelable(loader);
            this.loader = loader;
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.position = mCurItem;
        if (mAdapter != null) {
            ss.adapterState = mAdapter.saveState();
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (mAdapter != null) {
            mAdapter.restoreState(ss.adapterState, ss.loader);
            mCurTempItem = ss.position;
            requestLayout();
        } else {
            mRestoredCurItem = ss.position;
            mRestoredAdapterState = ss.adapterState;
            mRestoredClassLoader = ss.loader;
        }
    }

    boolean infoForChild(View child, ItemInfo ii) {
        return mAdapter.isViewFromObject(child, ii.object);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // For simple implementation, or internal size is always 0.
        // We depend on the container to specify the layout size of
        // our view.  We can't really know what it is since we will be
        // adding and removing different arbitrary views and do not
        // want the layout to change as this happens.
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec),
                getDefaultSize(0, heightMeasureSpec));

        // Children are just made to fill our space.
        int childWidthSize = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int childHeightSize = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY);

        ItemInfo info = null;
        if(mAdapter != null) {
            // Make sure we have created all fragments that we need to have shown.
            info = populate();
        }

        // Page views next.
        int size = getChildCount();
        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
            if(info != null && infoForChild(child,info))
            {
                child.setVisibility(View.VISIBLE);
                if (DEBUG) LogManager.logV(TabPager.class, "Measuring #" + i + " " + child
                        + ": " + childWidthMeasureSpec);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }else
            {
                child.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int size = getChildCount();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        for (int i = 0; i < size; ++i) {
            final View child = getChildAt(i);
            if(child.getVisibility() != View.GONE)
            {
                child.layout(paddingLeft, paddingTop, paddingLeft + child.getMeasuredWidth(), paddingTop + child.getMeasuredHeight());
            }
        }
        if(mCurTempItem != mCurItem)
        {
            mCurItem = mCurTempItem;
            if(mOnTabChangeListener != null)
            {
                // 在当前layout过程中onTabSelected回调方法如果导致了requestLayout()将无法执行，故post处理
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mOnTabChangeListener.onTabSelected(mCurItem);
                    }
                });
            }
        }
    }

    private class PagerObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            dataSetChanged();
        }
        @Override
        public void onInvalidated() {
            dataSetChanged();
        }
    }

}
