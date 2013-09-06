package cn.emagsoftware.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * @deprecated 该类已舍弃，请使用实现更好的android.support.v4.view.ViewPager代替
 * @author Wendell
 * @version 4.9
 */
public class FlipLayout extends ViewGroup
{

    private static final int SNAP_VELOCITY                      = 600;

    private Scroller         mScroller;
    private VelocityTracker  mVelocityTracker;
    private int              mTouchSlop;
    private float            mLastMotionX;
    private float            mLastMotionY;
    private float            mInterceptedX;

    /** 当前Screen的位置 */
    private int              mCurScreen                         = -1;
    private int              mTempCurScreen                     = -1;
    private boolean          mIsRequestScroll                   = false;
    private boolean          mScrollWhenSameScreen              = false;
    /** 当前NO-GONE子View的列表 */
    private List<View>       mNoGoneChildren                    = new ArrayList<View>();
    /** 是否当次的OnFlingListener.onFlingOutOfRange事件回调已中断 */
    private boolean          mIsFlingOutOfRangeBreak            = false;
    /** 是否需要重置mIsFlingOutOfRangeBreak的值 */
    private boolean          mShouldResetIsFlingOutOfRangeBreak = true;
    /** 是否在手指按在上面时发生了屏幕改变 */
    private boolean          mIsFlingChangedWhenPressed         = false;
    private boolean          mIsIntercepted                     = false;

    private OnFlingListener  listener;

    private boolean mCheckIndexForFragment = false;

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
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
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
        // 重新layout子View是必须的，在changed为false的情况下也是如此，因为子View的状态可能发生了变化（如Child改变、GONE/VISIBLE切换等）
        int childLeft = 0;
        int childWidth = r - l;
        int childHeight = b - t;
        int noGoneChildCount = mNoGoneChildren.size();
        for (int i = 0; i < noGoneChildCount; i++)
        {
            View noGonechildView = mNoGoneChildren.get(i);
            noGonechildView.layout(childLeft, 0, childLeft + childWidth, childHeight);
            childLeft += childWidth;
        }

        boolean curScrollWhenSameScreen = mScrollWhenSameScreen;
        mScrollWhenSameScreen = false;
        if (mTempCurScreen == -1)
        {
            mTempCurScreen = 0;
            scrollTo(mTempCurScreen * getWidth(), 0);
            mCurScreen = mTempCurScreen;
            if (listener != null)
                // 在当前layout过程中onFlingChanged回调方法如果导致了requestLayout()将无法执行，故post处理
                new Handler().post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        listener.onFlingChanged(mNoGoneChildren.get(mTempCurScreen), mTempCurScreen);
                    }
                });
        } else
        {
            if (mTempCurScreen >= noGoneChildCount)
            {
                mScroller.forceFinished(true);
                int mTempCurScreenCopy = mTempCurScreen;
                mTempCurScreen = mCurScreen;
                throw new IllegalStateException("cur screen is out of range:" + mTempCurScreenCopy + "!");
            } else
            {
                if (mTempCurScreen == mCurScreen)
                {
                    if (changed)
                    {
                        mScroller.forceFinished(true);
                        scrollTo(mTempCurScreen * getWidth(), 0);
                    } else if (curScrollWhenSameScreen)
                    {
                        int scrollX = getScrollX();
                        int delta = mTempCurScreen * getWidth() - scrollX;
                        mScroller.startScroll(scrollX, 0, delta, 0, 500);
                        invalidate(); // 重绘
                    }
                } else
                {
                    if (mIsRequestScroll)
                    {
                        int scrollX = getScrollX();
                        int delta = mTempCurScreen * getWidth() - scrollX;
                        mScroller.startScroll(scrollX, 0, delta, 0, 500);
                        mTempCurScreen = mCurScreen;
                        invalidate(); // 重绘
                    } else
                    {
                        mScroller.forceFinished(true);
                        scrollTo(mTempCurScreen * getWidth(), 0);
                        mCurScreen = mTempCurScreen;
                        if (listener != null)
                            // 在当前layout过程中onFlingChanged回调方法如果导致了requestLayout()将无法执行，故post处理
                            new Handler().post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    listener.onFlingChanged(mNoGoneChildren.get(mTempCurScreen), mTempCurScreen);
                                }
                            });
                    }
                }
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
        if(mCheckIndexForFragment)
        {
            Comparator<View> com = new Comparator<View>() {
                @Override
                public int compare(View view, View view2) {
                    if(!(view instanceof ViewGroup) || !(view2 instanceof ViewGroup))
                        throw new IllegalStateException("if you call setCheckIndexForFragment(true),the child should only be added by Fragment");
                    ViewGroup viewWrap = (ViewGroup)view;
                    ViewGroup viewWrap2 = (ViewGroup)view2;
                    if(viewWrap.getChildCount() != 1 || viewWrap2.getChildCount() != 1)
                        throw new IllegalStateException("if you call setCheckIndexForFragment(true),the child should only be added by Fragment");
                    Object tag = viewWrap.getChildAt(0).getTag();
                    Object tag2 = viewWrap2.getChildAt(0).getTag();
                    if(!(tag instanceof Integer) || !(tag2 instanceof Integer))
                        throw new IllegalStateException("if you call setCheckIndexForFragment(true),should use setTag(tag) to set index in Fragment’s onCreateView(inflater,container,savedInstanceState)");
                    int index = (Integer)tag;
                    int index2 = (Integer)tag2;
                    if(index == index2)
                        return 0;
                    return index > index2 ? 1 : -1;
                }
            };
            Collections.sort(mNoGoneChildren,com);
        }
        // 计算自身大小
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setToScreen(int whichScreen)
    {
        if (whichScreen < 0)
            throw new IllegalArgumentException("whichScreen should equals or great than zero.");
        if (whichScreen == mCurScreen)
            return;
        this.mTempCurScreen = whichScreen;
        this.mIsRequestScroll = false;
        requestLayout();
    }

    public void scrollToScreen(int whichScreen)
    {
        if (whichScreen < 0)
            throw new IllegalArgumentException("whichScreen should equals or great than zero.");
        if (whichScreen == mCurScreen)
            return;
        this.mTempCurScreen = whichScreen;
        this.mIsRequestScroll = true;
        requestLayout();
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
                    mCurScreen = mTempCurScreen = destScreen;
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
        if (!mIsIntercepted)
        {
            onInterceptTouchEventImpl(event);
            if (!mIsIntercepted)
                return true;
        }

        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);

        int action = event.getAction();
        float x = event.getX();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                LogManager.logI(FlipLayout.class, "event down!");
                mScroller.forceFinished(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                mScroller.forceFinished(true);
                if (mShouldResetIsFlingOutOfRangeBreak)
                {
                    mIsFlingOutOfRangeBreak = false;
                    mShouldResetIsFlingOutOfRangeBreak = false;
                }
                int deltaX = (int) (mInterceptedX - x);
                mInterceptedX = x;
                scrollBy(deltaX, 0);
                if (checkFlingWhenScroll())
                    mIsFlingChangedWhenPressed = true;
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                LogManager.logI(FlipLayout.class, "event up/cancel!");
                mIsIntercepted = false;
                mShouldResetIsFlingOutOfRangeBreak = true;
                if (mIsFlingChangedWhenPressed)
                { // 如果手指按在上面时已经发生了屏幕改变，则将不会继续触发屏幕改变
                    mIsFlingChangedWhenPressed = false;
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    mScrollWhenSameScreen = true;
                    requestLayout();
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
                        mScrollWhenSameScreen = true;
                        requestLayout();
                    }
                }
                return true;
            default:
                return true;
        }
    }

    /**
     * <p>MotionEvent事件可分为两个部分：
     *    1.当为ACTION_DOWN时：会对范围内的View从上而下（ViewGroup->View）递归执行onInterceptTouchEvent，直到没有子View或onInterceptTouchEvent返回true时将停止递归，此时，若当前View是onInterceptTouchEvent返回true的，
     *      则当前View会被标识为一个target，否则将从当前View开始向上（View->ViewGroup）执行OnTouchListener和onTouchEvent，直到返回true或到达顶层View时停止，这种情况下的target便是此时的View。
     *    2.当为后续MotionEvent时：后续MotionEvent只会发送给target进行执行。但事件仍会先通过target以上（不包括target）View的onInterceptTouchEvent进行拦截，若上层View此时在onInterceptTouchEvent返回true，则该上层View将会成为新的
     *      target，所有的后续事件都会发送到新的target，原来的target只会再收到一个ACTION_CANCEL事件。
     * <p>调用requestDisallowInterceptTouchEvent(true)会导致当前View及其所有父View不再执行onInterceptTouchEvent进行拦截，requestDisallowInterceptTouchEvent可重复调用，并且在下一个整MotionEvent事件开始时会恢复为允许拦截状态
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // TODO Auto-generated method stub
        return onInterceptTouchEventImpl(ev);
    }

    private boolean onInterceptTouchEventImpl(MotionEvent ev)
    {
        int action = ev.getAction();
        float x = ev.getX();
        float y = ev.getY();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN: // 尽管onInterceptTouchEvent可能会被requestDisallowInterceptTouchEvent(true)禁止，但其通常在onInterceptTouchEvent中调用，所以ACTION_DOWN时的onInterceptTouchEvent一般都会执行到
                mLastMotionX = x;
                mLastMotionY = y;
                boolean isFinished = mScroller.isFinished();
                if (isFinished)
                {
                    return false;
                } else
                // 正在滚动时将被拦截
                {
                    mInterceptedX = x;
                    mIsIntercepted = true;
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                final float xDistence = Math.abs(x - mLastMotionX);
                final float yDistence = Math.abs(y - mLastMotionY);
                if (xDistence > mTouchSlop)
                {
                    double angle = Math.toDegrees(Math.atan(yDistence / xDistence));
                    if (angle <= 45) // 小于指定角度将被拦截
                    {
                        mInterceptedX = x;
                        mIsIntercepted = true;
                        return true;
                    }
                }
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return false;
            default:
                return false;
        }
    }

    public void setOnFlingListener(OnFlingListener listener)
    {
        this.listener = listener;
    }

    /**
     * <p>增加对通过Fragment添加child的额外支持，在该方法设置为true的情况下，可以通过View的setTag方法来指定添加进父容器的位置</>
     * @param checkIndexForFragment
     */
    public void setCheckIndexForFragment(boolean checkIndexForFragment)
    {
        this.mCheckIndexForFragment = checkIndexForFragment;
    }

    public abstract static interface OnFlingListener
    {
        public abstract void onFlingChanged(View whichView, int whichScreen);

        public abstract boolean onFlingOutOfRange(boolean toRight, int distance);
    }

}
