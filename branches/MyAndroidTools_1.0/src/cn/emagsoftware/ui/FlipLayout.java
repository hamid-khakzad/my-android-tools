package cn.emagsoftware.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;
import cn.emagsoftware.util.LogManager;

/**
 * 仿Launcher中的WorkSpace，可以左右滑动切换屏幕的类
 * 
 * @author Wendell
 * @version 4.6
 */
public class FlipLayout extends ViewGroup
{

    private static final int SNAP_VELOCITY                      = 600;

    private Scroller         mScroller;
    private VelocityTracker  mVelocityTracker;
    private int              mTouchSlop;
    private float            mLastMotionX;
    private float            mLastMotionY;

    /** 当前Screen的位置 */
    private int              mCurScreen                         = -1;
    /** 是否已被布局过 */
    private boolean          mIsLayout                          = false;
    /** 当前NO-GONE子View的列表 */
    private List<View>       mNoGoneChildren                    = new ArrayList<View>();
    /** 手指是否按在上面 */
    private boolean          mIsPressed                         = false;
    /** 是否当次的OnFlingListener.onFlingOutOfRange事件回调已中断 */
    private boolean          mIsFlingOutOfRangeBreak            = false;
    /** 是否需要重置mIsFlingOutOfRangeBreak的值 */
    private boolean          mShouldResetIsFlingOutOfRangeBreak = false;
    /** 是否在手指按在上面时发生了屏幕改变 */
    private boolean          mIsFlingChangedWhenPressed         = false;
    /** 是否请求了进行水平的滑动 */
    private boolean          mRequestHorizontalFlip             = false;

    private OnFlingListener  listener;

    public FlipLayout(Context context)
    {
        this(context, null, 0);
    }

    public FlipLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public FlipLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    /**
     * <p>由layout方法回调，指定在布局完自己之后，如何布局子View。所以非ViewGroup的View无需实现该方法 <p>layout方法根据measure方法计算出的自身大小来布局自己使产生大小并回调onLayout布局子View。若得到的自身大小和位置与上次一样，将在回调onLayout时给changed参数传入false
     * <p>measure方法会在调用requestLayout方法时调用并递归计算出所有子View的大小，然后才会递归执行layout方法，而不是在layout方法中逐一调用measure方法 <p>系统不会自动调用requestLayout方法，View需在某些可能改变自身大小的属性设置之后手动调用requestLayout方法
     * <p>布局之后就需要对界面绘制了，View通过调用invalidate方法进行绘制或重绘，ViewGroup的invalidate方法将会绘制或重绘自己所有的子View，在其他线程中调用绘制或重绘时需执行postInvalidate方法
     * <p>系统不会自动调用invalidate方法，View需在某些可能改变自身大小的属性设置之后手动调用invalidate方法，invalidate方法实际上调用了View的onDraw方法 <p>绘制并不等同于显示到屏幕，系统会在UI-Thread的Looper处于空闲时，将onDraw方法中绘制在内存的内容渲染到屏幕
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        // 不考虑changed参数，因为FlipLayout大小是固定的，但其可见子View的个数可能发生了变化，所以每次都要重新layout
        int childLeft = 0;
        int childWidth = r - l;
        int childHeight = b - t;
        final int noGoneChildCount = mNoGoneChildren.size();
        for (int i = 0; i < noGoneChildCount; i++)
        {
            View noGonechildView = mNoGoneChildren.get(i);
            noGonechildView.layout(childLeft, 0, childLeft + childWidth, childHeight);
            childLeft += childWidth;
        }

        if (mIsLayout)
        {
            if (mCurScreen >= noGoneChildCount)
            {
                // setToScreen包含的事件，在非第一次布局的情况下可能会影响当前的递归布局，使状态不一致，故POST处理
                new Handler().post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        mScroller.forceFinished(true);
                        setToScreen(noGoneChildCount - 1);
                    }
                });
            } else if (!mIsPressed)
            {
                // 手按在上面时不能定位位置，会影响滑动体验，手放开时会重新计算位置，包括大小改变（如横竖屏切换）的情况都能被准确定位
                if (mScroller.isFinished())
                {
                    // 自动滚动时不能定位位置，会影响滚动体验，尽管在大小改变（如横竖屏切换）的情况下定位可能出现偏差，但前者更常见更需侧重考虑
                    // 理论上不会回调事件，不会影响当前的递归布局，故不需要POST处理
                    setToScreen(mCurScreen);
                }
            }
        } else
        { // 如果是第一次布局
            mIsLayout = true;
            if (mCurScreen == -1)
            { // 在没有选择Screen的情况下，将默认选择第一个
                setToScreen(0);
            } else
            {
                if (mCurScreen < 0 || mCurScreen >= noGoneChildCount)
                    throw new IllegalStateException("mCurScreen is out of range:" + mCurScreen + "!");
                scrollTo(mCurScreen * childWidth, 0);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // 大小只支持精确模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");

        // 计算子View大小
        mNoGoneChildren.clear();
        int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE)
            {
                childView.measure(widthMeasureSpec, heightMeasureSpec);
                mNoGoneChildren.add(childView);
            }
        }
        if (mNoGoneChildren.size() == 0)
            throw new IllegalStateException("FlipLayout must have one NO-GONE child at least!");

        // 计算自身大小
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setToScreen(int whichScreen)
    {
        if (mIsLayout)
        {
            if (whichScreen < 0 || whichScreen >= mNoGoneChildren.size())
                throw new IllegalArgumentException("whichScreen is out of range:" + whichScreen + "!");
            scrollTo(whichScreen * getWidth(), 0);
            if (whichScreen != mCurScreen)
            {
                mCurScreen = whichScreen;
                if (listener != null)
                    listener.onFlingChanged(mNoGoneChildren.get(whichScreen), whichScreen);
            }
        } else
        {
            int noGoneChildIndex = -1;
            View whichView = null;
            int count = getChildCount();
            for (int i = 0; i < count; i++)
            {
                View childView = getChildAt(i);
                if (childView.getVisibility() != View.GONE)
                {
                    if (++noGoneChildIndex == whichScreen)
                    {
                        whichView = childView;
                        break;
                    }
                }
            }
            if (noGoneChildIndex == -1)
                throw new IllegalStateException("FlipLayout must have one NO-GONE child at least!");
            if (whichView == null)
                throw new IllegalArgumentException("whichScreen is out of range:" + whichScreen + "!");
            if (whichScreen != mCurScreen)
            {
                mCurScreen = whichScreen;
                if (listener != null)
                    listener.onFlingChanged(whichView, whichScreen);
            }
        }
    }

    public void scrollToScreen(int whichScreen)
    {
        if (mIsLayout)
        {
            if (whichScreen < 0 || whichScreen >= mNoGoneChildren.size())
                throw new IllegalArgumentException("whichScreen is out of range:" + whichScreen + "!");
            int scrollX = getScrollX();
            int delta = whichScreen * getWidth() - scrollX;
            mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 2);
            invalidate(); // 重绘
        } else
        {
            int noGoneChildIndex = -1;
            View whichView = null;
            int count = getChildCount();
            for (int i = 0; i < count; i++)
            {
                View childView = getChildAt(i);
                if (childView.getVisibility() != View.GONE)
                {
                    if (++noGoneChildIndex == whichScreen)
                    {
                        whichView = childView;
                        break;
                    }
                }
            }
            if (noGoneChildIndex == -1)
                throw new IllegalStateException("FlipLayout must have one NO-GONE child at least!");
            if (whichView == null)
                throw new IllegalArgumentException("whichScreen is out of range:" + whichScreen + "!");
            if (whichScreen != mCurScreen)
            {
                mCurScreen = whichScreen;
                if (listener != null)
                    listener.onFlingChanged(whichView, whichScreen);
            }
        }
    }

    public int getCurScreen()
    {
        return mCurScreen;
    }

    protected boolean checkFlingWhenScroll()
    {
        int scrollX = getScrollX();
        if (scrollX < 0)
        {
            if (listener != null && !mIsFlingOutOfRangeBreak)
                mIsFlingOutOfRangeBreak = listener.onFlingOutOfRange(false, -scrollX);
            return false;
        } else
        {
            int noGoneChildCount = mNoGoneChildren.size();
            int screenWidth = getWidth();
            int maxScrollX = (noGoneChildCount - 1) * screenWidth;
            if (scrollX > maxScrollX)
            {
                if (listener != null && !mIsFlingOutOfRangeBreak)
                    mIsFlingOutOfRangeBreak = listener.onFlingOutOfRange(true, scrollX - maxScrollX);
                return false;
            } else
            {
                int destScreen = (scrollX + screenWidth / 2) / screenWidth;
                if (destScreen != mCurScreen)
                {
                    mCurScreen = destScreen;
                    if (listener != null)
                        listener.onFlingChanged(mNoGoneChildren.get(destScreen), destScreen);
                    return true;
                }
                return false;
            }
        }
    }

    @Override
    public void computeScroll()
    {
        // TODO Auto-generated method stub
        if (mScroller.computeScrollOffset())
        {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            checkFlingWhenScroll();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // TODO Auto-generated method stub
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);

        int action = event.getAction();
        float x = event.getX();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                LogManager.logI(FlipLayout.class, "event down!");
                if (!mScroller.isFinished())
                {
                    mScroller.forceFinished(true);
                }
                mLastMotionX = x;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!mIsPressed)
                    mIsPressed = true;
                if (mShouldResetIsFlingOutOfRangeBreak)
                {
                    mIsFlingOutOfRangeBreak = false;
                    mShouldResetIsFlingOutOfRangeBreak = false;
                }
                int deltaX = (int) (mLastMotionX - x);
                mLastMotionX = x;
                scrollBy(deltaX, 0);
                if (checkFlingWhenScroll())
                    mIsFlingChangedWhenPressed = true;
                return true;
            case MotionEvent.ACTION_UP:
                LogManager.logI(FlipLayout.class, "event up!");
                mIsPressed = false;
                mShouldResetIsFlingOutOfRangeBreak = true;
                if (mIsFlingChangedWhenPressed)
                { // 如果手指按在上面时已经发生了屏幕改变，则将不会继续触发屏幕改变
                    mIsFlingChangedWhenPressed = false;
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    scrollToScreen(mCurScreen);
                } else
                {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int velocityX = (int) mVelocityTracker.getXVelocity();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    LogManager.logI(FlipLayout.class, "velocityX:" + velocityX);
                    if (velocityX > SNAP_VELOCITY && mCurScreen > 0)
                    {
                        // 达到了向左移动的速度
                        LogManager.logI(FlipLayout.class, "snap left");
                        scrollToScreen(mCurScreen - 1);
                    } else if (velocityX < -SNAP_VELOCITY && mCurScreen < mNoGoneChildren.size() - 1)
                    {
                        // 达到了向右移动的速度
                        LogManager.logI(FlipLayout.class, "snap right");
                        scrollToScreen(mCurScreen + 1);
                    } else
                    {
                        scrollToScreen(mCurScreen);
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // TODO Auto-generated method stub
        int action = ev.getAction();
        float x = ev.getX();
        float y = ev.getY();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                boolean isFinished = mScroller.isFinished();
                mRequestHorizontalFlip = false;
                return isFinished ? false : true; // 正在滚动时发生的事件将被拦截，并且后续事件也将被拦截
            case MotionEvent.ACTION_MOVE:
                if (mRequestHorizontalFlip)
                    return false;
                else
                {
                    final float xDistence = Math.abs(x - mLastMotionX);
                    final float yDistence = Math.abs(y - mLastMotionY);
                    if (xDistence > mTouchSlop)
                    {
                        double angle = Math.toDegrees(Math.atan(yDistence / xDistence));
                        if (angle <= 45)
                            return true; // 小于指定角度将被拦截，并且后续事件也将被拦截
                    }
                    return false;
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return false;
            default:
                return false;
        }
    }

    public void requestHorizontalFlip()
    {
        mRequestHorizontalFlip = true;
    }

    public void setOnFlingListener(OnFlingListener listener)
    {
        this.listener = listener;
    }

    public abstract static interface OnFlingListener
    {
        public abstract void onFlingChanged(View whichView, int whichScreen);

        public abstract boolean onFlingOutOfRange(boolean toRight, int distance);
    }

}
