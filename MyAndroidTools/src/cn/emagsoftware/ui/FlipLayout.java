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
 * ��Launcher�е�WorkSpace���������һ����л���Ļ����
 * 
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

    /** ��ǰScreen��λ�� */
    private int              mCurScreen                         = -1;
    private int              mTempCurScreen                     = -1;
    private boolean          mIsRequestScroll                   = false;
    private boolean          mScrollWhenSameScreen              = false;
    /** ��ǰNO-GONE��View���б� */
    private List<View>       mNoGoneChildren                    = new ArrayList<View>();
    /** �Ƿ񵱴ε�OnFlingListener.onFlingOutOfRange�¼��ص����ж� */
    private boolean          mIsFlingOutOfRangeBreak            = false;
    /** �Ƿ���Ҫ����mIsFlingOutOfRangeBreak��ֵ */
    private boolean          mShouldResetIsFlingOutOfRangeBreak = true;
    /** �Ƿ�����ָ��������ʱ��������Ļ�ı� */
    private boolean          mIsFlingChangedWhenPressed         = false;
    private boolean          mIsIntercepted                     = false;

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
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * <p>��layout�����ص���ָ���ڲ������Լ�֮����β�����View�����Է�ViewGroup��View����ʵ�ָ÷��� <p>layout��������measure����������������С�������Լ�ʹ������С���ص�onLayout������View�����õ��������С��λ�����ϴ�һ�������ڻص�onLayoutʱ��changed��������false
     * <p>measure�������ڵ���requestLayout����ʱ���ò��ݹ�����������View�Ĵ�С��Ȼ��Ż�ݹ�ִ��layout��������������layout��������һ����measure���� <p>ϵͳ�����Զ�����requestLayout������View����ĳЩ���ܸı������С����������֮���ֶ�����requestLayout����
     * <p>����֮�����Ҫ�Խ�������ˣ�Viewͨ������invalidate�������л��ƻ��ػ棬ViewGroup��invalidate����������ƻ��ػ��Լ����е���View���������߳��е��û��ƻ��ػ�ʱ��ִ��postInvalidate����
     * <p>ϵͳ�����Զ�����invalidate������View����ĳЩ���ܸı������С����������֮���ֶ�����invalidate������invalidate����ʵ���ϵ�����View��onDraw���� <p>���Ʋ�����ͬ����ʾ����Ļ��ϵͳ����UI-Thread��Looper���ڿ���ʱ����onDraw�����л������ڴ��������Ⱦ����Ļ
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        // ����layout��View�Ǳ���ģ���changedΪfalse�������Ҳ����ˣ���Ϊ��View��״̬���ܷ����˱仯����Child�ı䡢GONE/VISIBLE�л��ȣ�
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
                // �ڵ�ǰlayout������onFlingChanged�ص��������������requestLayout()���޷�ִ�У���post����
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
                        mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 2);
                        invalidate(); // �ػ�
                    }
                } else
                {
                    if (mIsRequestScroll)
                    {
                        int scrollX = getScrollX();
                        int delta = mTempCurScreen * getWidth() - scrollX;
                        mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 2);
                        mTempCurScreen = mCurScreen;
                        invalidate(); // �ػ�
                    } else
                    {
                        mScroller.forceFinished(true);
                        scrollTo(mTempCurScreen * getWidth(), 0);
                        mCurScreen = mTempCurScreen;
                        if (listener != null)
                            // �ڵ�ǰlayout������onFlingChanged�ص��������������requestLayout()���޷�ִ�У���post����
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
        // ��Сֻ֧�־�ȷģʽ
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY)
            throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");

        // ������View��С
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

        // ���������С
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
                { // �����ָ��������ʱ�Ѿ���������Ļ�ı䣬�򽫲������������Ļ�ı�
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
                        // �ﵽ�������ƶ����ٶ�
                        LogManager.logI(FlipLayout.class, "snap left");
                        scrollToScreen(mCurScreen - 1);
                    } else if (velocityX < -SNAP_VELOCITY && mCurScreen < mNoGoneChildren.size() - 1)
                    {
                        // �ﵽ�������ƶ����ٶ�
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
     * <p>��MotionEventΪACTION_DOWNʱ������϶��£�ViewGroup->View���ݹ�ִ��onInterceptTouchEvent��ֱ��û����View��onInterceptTouchEvent����trueʱ��ֹͣ�ݹ飬
     * ��ʱ���ӵ�ǰView��ʼ���ϣ�View->ViewGroup��ִ��OnTouchListener��onTouchEvent��ֱ������true��ִֹͣ�У����ҵ�ǰ����true��View�����ǳ�һ��target����Ҫע����ǣ�
     * �����ʱ������requestDisallowInterceptTouchEvent(true)��������ִ��onInterceptTouchEvent������ֱ�Ӵ����·���View��ʼ���ϣ�View->ViewGroup��ִ��OnTouchListener ��onTouchEvent��Ѱ��target
     * <p>��MotionEventΪ��ACTION_DOWNʱ�����û��target������ִ�ж���View��OnTouchListener��onTouchEvent���������target����ֻ��ִ��target��OnTouchListener��onTouchEvent��
     * 1.��requestDisallowInterceptTouchEvent(false)�ҵݹ�ִ��onInterceptTouchEventʱ������true��targetִ�е��¼����ᱻ�޸�ΪACTION_CANCEL�� �˺�onInterceptTouchEvent����true��View�Ļ��Ϊ�µ�target���������ͬ��
     * 2.����target��ִ��ԭʼ�ķ�ACTION_DOWN�¼� <p>��MotionEventΪ��ACTION_DOWNʱ��OnTouchListener��onTouchEvent�ķ���ֵ�Ѿ�����޹ؽ�Ҫ
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
            case MotionEvent.ACTION_DOWN: // ����onInterceptTouchEvent���ܻᱻrequestDisallowInterceptTouchEvent(true)��ֹ������ͨ����onInterceptTouchEvent�е��ã�����ACTION_DOWNʱ��onInterceptTouchEventһ�㶼��ִ�е�
                mLastMotionX = x;
                mLastMotionY = y;
                boolean isFinished = mScroller.isFinished();
                if (isFinished)
                {
                    return false;
                } else
                // ���ڹ���ʱ��������
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
                    if (angle <= 45) // С��ָ���ǶȽ�������
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

    public abstract static interface OnFlingListener
    {
        public abstract void onFlingChanged(View whichView, int whichScreen);

        public abstract boolean onFlingOutOfRange(boolean toRight, int distance);
    }

}
