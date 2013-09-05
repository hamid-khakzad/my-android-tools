package cn.emagsoftware.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class LeftSliderLayout extends ViewGroup {

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    /**
     * Constant value for touch state
     * TOUCH_STATE_REST : no touch
     * TOUCH_STATE_SCROLLING : scrolling
     */
    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_SCROLLING = 1;
    private int mTouchState = TOUCH_STATE_REST;

    /**
     * Distance in pixels a touch can wander before we think the user is scrolling
     */
    private int mTouchSlop;

    /**
     * Values for saving axis of the last touch event.
     */
    private float mLastMotionX;
    private float mLastMotionY;

    /**
     * Values for VelocityTracker to compute current velocity.
     * VELOCITY_UNITS in dp
     * mVelocityUnits in px
     */
    private static final int VELOCITY_UNITS = 1000;
    private int mVelocityUnits;

    /**
     * The minimum velocity for determining the direction.
     * MINOR_VELOCITY in dp
     * mMinorVelocity in px
     */
    private static final float MINOR_VELOCITY = 150.0f;
    private int mMinorVelocity;

    /**
     * The width of Sliding distance from left.
     * And it should be the same with the width of the View below SliderLayout in a FrameLayout.
     * DOCK_WIDTH in dp
     * mDockWidth in px
     */
    private static final float SLIDING_WIDTH = 180.0f;
    private int mSlidingWidth;

    /**
     * The default values of shadow.
     * VELOCITY_UNITS in dp
     * mVelocityUnits in px
     */
    private static final float DEF_SHADOW_WIDTH = 10.0f;
    private int mDefShadowWidth;

    /**
     * Value for checking a touch event is completed.
     */
    private boolean mIsTouchEventDone = false;

    /**
     * Value for checking slider is open.
     */
    private boolean mIsOpen = false;

    /**
     * Value for saving the last offset of scroller ’ x-axis.
     */
    private int mSaveScrollX = 0;

    /**
     * Value for checking slider is allowed to slide.
     */
    private boolean mEnableSlide = true;

    private View mMainChild = null;
    private OnLeftSliderLayoutStateListener mListener = null;

    private float fDensity;

    public LeftSliderLayout(Context context) {
        this(context, null, 0);
    }

    /**
     * Instantiates a new LeftSliderLayout.
     *
     * @param context the associated Context
     * @param attrs AttributeSet
     */
    public LeftSliderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new LeftSliderLayout.
     *
     * @param context the associated Context
     * @param attrs AttributeSet
     * @param defStyle Style
     */
    public LeftSliderLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        /**
         * Convert values in dp to values in px;
         */
        fDensity = getResources().getDisplayMetrics().density;
        mVelocityUnits = (int) (VELOCITY_UNITS * fDensity + 0.5f);
        mMinorVelocity = (int) (MINOR_VELOCITY * fDensity + 0.5f);
        mSlidingWidth = (int) (SLIDING_WIDTH * fDensity + 0.5f);
        mDefShadowWidth = (int) (DEF_SHADOW_WIDTH * fDensity + 0.5f);
    }

    public void setSlidingWidthDp(int slidingWidth)
    {
        if(slidingWidth < 0) throw new IllegalArgumentException("slidingWidth < 0");
        mSlidingWidth = (int) (slidingWidth * fDensity + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // check Measure Mode is Exactly.
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("LeftSliderLayout only canmCurScreen run at EXACTLY mode!");
        }
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("LeftSliderLayout only can run at EXACTLY mode!");
        }

        // measure child views
        int nCount = getChildCount();
        for (int i = 2; i < nCount; i++) {
            removeViewAt(i);
        }
        nCount = getChildCount();
        if (nCount > 0) {
            if (nCount > 1) {
                mMainChild = getChildAt(1);
                getChildAt(0).measure(widthMeasureSpec, heightMeasureSpec);
            } else {
                mMainChild = getChildAt(0);
            }
            mMainChild.measure(widthMeasureSpec, heightMeasureSpec);
        }

        // Set the scrolled position
        scrollTo(mSaveScrollX, 0);
    }

    /**
     * <p>由layout方法回调，指定在布局完自己之后，如何布局子View。所以非ViewGroup的View无需实现该方法 <p>layout方法根据measure方法计算出的自身大小来布局自己使产生大小并回调onLayout布局子View。若得到的自身大小和位置与上次一样，将在回调onLayout时给changed参数传入false
     * <p>measure方法会在调用requestLayout方法时调用并递归计算出所有子View的大小，然后才会递归执行layout方法，而不是在layout方法中逐一调用measure方法 <p>系统不会自动调用requestLayout方法，View需在某些可能改变自身大小的属性设置之后手动调用requestLayout方法
     * <p>布局之后就需要对界面绘制了，View通过调用invalidate方法进行绘制或重绘，ViewGroup的invalidate方法将会绘制或重绘自己所有的子View，在其他线程中调用绘制或重绘时需执行postInvalidate方法
     * <p>系统不会自动调用invalidate方法，View需在某些可能改变自身大小的属性设置之后手动调用invalidate方法，invalidate方法实际上调用了View的onDraw方法 <p>绘制并不等同于显示到屏幕，系统会在UI-Thread的Looper处于空闲时，将onDraw方法中绘制在内存的内容渲染到屏幕
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int nCount = getChildCount();
        if (nCount <= 0) {
            return;
        }

        // Set the size and position of Main Child
        if (mMainChild != null) {
            mMainChild.layout(
                    l,
                    t,
                    l + mMainChild.getMeasuredWidth(),
                    t + mMainChild.getMeasuredHeight());
        }

        // Set the size and position of Shadow Child
        if (nCount > 1) {
            int nLeftChildWidth = 0;
            View leftChild = getChildAt(0);
            ViewGroup.LayoutParams layoutParams = leftChild.getLayoutParams();
            if (layoutParams.width == ViewGroup.LayoutParams.FILL_PARENT
                    /**|| layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT*/) {
                nLeftChildWidth = mDefShadowWidth;
            } else {
                nLeftChildWidth = layoutParams.width;
            }
            leftChild.layout(
                    l - nLeftChildWidth,
                    t,
                    l,
                    t + leftChild.getMeasuredHeight());
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int nCurScrollX = getScrollX();

        // check touch point is in the rectangle of Main Child
        if (mMainChild != null
                && mTouchState != TOUCH_STATE_SCROLLING
                && mIsTouchEventDone) {
            Rect rect = new Rect();
            mMainChild.getHitRect(rect);
            if (!rect.contains((int)event.getX() + nCurScrollX, (int)event.getY())) {
                return false;
            }
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);

        final int action = event.getAction();
        final float x = event.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                mIsTouchEventDone = false;
                mLastMotionX = x;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // check slider is allowed to slide.
                if (!mEnableSlide) {
                    break;
                }

                // compute the x-axis offset from last point to current point
                int deltaX = (int) (mLastMotionX - x);
                if (nCurScrollX + deltaX < getMinScrollX()) {
                    deltaX = getMinScrollX() - nCurScrollX;
                    mLastMotionX = mLastMotionX - deltaX;
                } else if (nCurScrollX + deltaX > getMaxScrollX()) {
                    deltaX = getMaxScrollX() - nCurScrollX;
                    mLastMotionX = mLastMotionX - deltaX;
                } else {
                    mLastMotionX = x;
                }

                // Move view to the current point
                if (deltaX != 0) {
                    scrollBy(deltaX, 0);
                }

                // Save the scrolled position
                mSaveScrollX = getScrollX();
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {

                // check slider is allowed to slide.
                if (!mEnableSlide) {
                    break;
                }

                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(mVelocityUnits);

                // Set open or close state, when get ACTION_UP or ACTION_CANCEL event.
                if (nCurScrollX < 0) {
                    int velocityX = (int) velocityTracker.getXVelocity();
                    if (velocityX > mMinorVelocity) {
                        scrollByWithAnim(getMinScrollX() - nCurScrollX);
                        setState(true);
                    }
                    else if (velocityX < -mMinorVelocity) {
                        scrollByWithAnim(-nCurScrollX);
                        setState(false);
                    } else {
                        if (nCurScrollX >= getMinScrollX() / 2) {
                            scrollByWithAnim(- nCurScrollX);
                            setState(false);
                        } else {
                            scrollByWithAnim(getMinScrollX() - nCurScrollX);
                            setState(true);
                        }
                    }
                } else {
                    if (nCurScrollX > 0) {
                        scrollByWithAnim(-nCurScrollX);
                    }
                    setState(false);
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                mTouchState = TOUCH_STATE_REST;
                mIsTouchEventDone = true;
                break;
            }

        }
        return true;
    }

    /**
     * <p>MotionEvent事件按时间先后可以分成两个部分：
     *    1.寻找target：寻找target只会在MotionEvent为ACTION_DOWN执行，会对范围内的View从上而下（ViewGroup->View）递归执行onInterceptTouchEvent，直到没有子View或onInterceptTouchEvent返回true时将停止递归，
     *      此时将从当前View开始向上（View->ViewGroup）执行OnTouchListener和onTouchEvent，直到返回true或到达顶层View将停止执行，此时当前的View便会被标记成一个target。
     *    2.执行MotionEvent：后续MotionEvent事件只会发送给target进行执行。但事件仍会先通过target以上（不包括target）View的onInterceptTouchEvent进行拦截，若上层View此时在onInterceptTouchEvent返回true，则该上层View将会成为新的
     *      target，所有的后续事件都会发送到新的target，原来的target只会再收到一个ACTION_CANCEL事件。
     * <p>调用requestDisallowInterceptTouchEvent(true)会导致当前View及其所有父View不再执行onInterceptTouchEvent进行拦截，requestDisallowInterceptTouchEvent可重复调用，并且在下一个整MotionEvent事件开始时会恢复为允许拦截状态
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();

        /*if (mListener != null && !mListener.OnLeftSliderLayoutInterceptTouch(ev)) {
            return false;
        }*/

        if ((action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                if (xDiff > mTouchSlop) {
                    if (Math.abs(mLastMotionY - y) / Math.abs(mLastMotionX - x) < 1)
                        mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    /**
     * With the horizontal scroll of the animation
     *
     * @param nDx x-axis offset
     */
    void scrollByWithAnim(int nDx) {
        if (nDx == 0) {
            return;
        }

        mScroller.startScroll(getScrollX(), 0, nDx, 0,
                Math.abs(nDx));

        invalidate();
    }

    /**
     * Get distance of the maximum horizontal scroll
     *
     * @return distance in px
     */
    private int getMaxScrollX() {
        return 0;
    }

    /**
     * Get distance of the minimum horizontal scroll
     * @return distance in px
     */
    private int getMinScrollX() {
        return -mSlidingWidth;
    }


    /**
     * Open LeftSlideLayout
     */
    public void open() {
        if (mEnableSlide) {
            scrollByWithAnim(getMinScrollX() - getScrollX());
            setState(true);
        }
    }

    /**
     * Close LeftSlideLayout
     */
    public void close() {
        if (mEnableSlide) {
            scrollByWithAnim((-1) * getScrollX());
            setState(false);
        }
    }

    /**
     * Determine whether LeftSlideLayout is open
     *
     * @return true-open，false-close
     */
    public boolean isOpen() {
        return mIsOpen;
    }

    /**
     * Set state of LeftSliderLayout
     *
     * @param bIsOpen the new state
     */
    private void setState(boolean bIsOpen) {
        boolean bStateChanged = false;
        if (mIsOpen && !bIsOpen) {
            bStateChanged = true;
        } else if (!mIsOpen && bIsOpen) {
            bStateChanged = true;
        }

        mIsOpen = bIsOpen;

        if (bIsOpen) {
            mSaveScrollX = getMaxScrollX();
        } else {
            mSaveScrollX = 0;
        }

        if (bStateChanged && mListener != null) {
            mListener.OnLeftSliderLayoutStateChanged(bIsOpen);
        }
    }

    /**
     * enable slide action of LeftSliderLayout
     *
     * @param bEnable
     */
    public void enableSlide(boolean bEnable) {
        mEnableSlide = bEnable;
    }

    /**
     * Set listener to LeftSliderLayout
     */
    public void setOnLeftSliderLayoutListener(OnLeftSliderLayoutStateListener listener) {
        mListener = listener;
    }

    /**
     * LeftSliderLayout Listener
     *
     */
    public interface OnLeftSliderLayoutStateListener {

        /**
         * Called when LeftSliderLayout’s state has been changed.
         *
         * @param bIsOpen the new state
         */
        public void OnLeftSliderLayoutStateChanged(boolean bIsOpen);

        /**
         * Called when LeftSliderLayout has got onInterceptTouchEvent.
         *
         * @param ev Touch Event
         * @return true - LeftSliderLayout need to manage the InterceptTouchEvent.
         *         false - LeftSliderLayout don't need to manage the InterceptTouchEvent.
         */
        /**public boolean OnLeftSliderLayoutInterceptTouch(MotionEvent ev);*/
    }
}
