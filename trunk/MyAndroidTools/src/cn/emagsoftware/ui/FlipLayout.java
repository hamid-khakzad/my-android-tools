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

    /** ��ǰScreen��λ�� */
    private int              mCurScreen                         = -1;
    /** �Ƿ��ѱ����ֹ� */
    private boolean          mIsLayout                          = false;
    /** ��ǰNO-GONE��View���б� */
    private List<View>       mNoGoneChildren                    = new ArrayList<View>();
    /** ��ָ�Ƿ������� */
    private boolean          mIsPressed                         = false;
    /** �Ƿ񵱴ε�OnFlingListener.onFlingOutOfRange�¼��ص����ж� */
    private boolean          mIsFlingOutOfRangeBreak            = false;
    /** �Ƿ���Ҫ����mIsFlingOutOfRangeBreak��ֵ */
    private boolean          mShouldResetIsFlingOutOfRangeBreak = false;
    /** �Ƿ�����ָ��������ʱ��������Ļ�ı� */
    private boolean          mIsFlingChangedWhenPressed         = false;
    /** �Ƿ������˽���ˮƽ�Ļ��� */
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
     * <p>��layout�����ص���ָ���ڲ������Լ�֮����β�����View�����Է�ViewGroup��View����ʵ�ָ÷��� <p>layout��������measure����������������С�������Լ�ʹ������С���ص�onLayout������View�����õ��������С��λ�����ϴ�һ�������ڻص�onLayoutʱ��changed��������false
     * <p>measure�������ڵ���requestLayout����ʱ���ò��ݹ�����������View�Ĵ�С��Ȼ��Ż�ݹ�ִ��layout��������������layout��������һ����measure���� <p>ϵͳ�����Զ�����requestLayout������View����ĳЩ���ܸı������С����������֮���ֶ�����requestLayout����
     * <p>����֮�����Ҫ�Խ�������ˣ�Viewͨ������invalidate�������л��ƻ��ػ棬ViewGroup��invalidate����������ƻ��ػ��Լ����е���View���������߳��е��û��ƻ��ػ�ʱ��ִ��postInvalidate����
     * <p>ϵͳ�����Զ�����invalidate������View����ĳЩ���ܸı������С����������֮���ֶ�����invalidate������invalidate����ʵ���ϵ�����View��onDraw���� <p>���Ʋ�����ͬ����ʾ����Ļ��ϵͳ����UI-Thread��Looper���ڿ���ʱ����onDraw�����л������ڴ��������Ⱦ����Ļ
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        // TODO Auto-generated method stub
        // ������changed��������ΪFlipLayout��С�ǹ̶��ģ�����ɼ���View�ĸ������ܷ����˱仯������ÿ�ζ�Ҫ����layout
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
                // setToScreen�������¼����ڷǵ�һ�β��ֵ�����¿��ܻ�Ӱ�쵱ǰ�ĵݹ鲼�֣�ʹ״̬��һ�£���POST����
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
                // �ְ�������ʱ���ܶ�λλ�ã���Ӱ�컬�����飬�ַſ�ʱ�����¼���λ�ã�������С�ı䣨��������л�����������ܱ�׼ȷ��λ
                if (mScroller.isFinished())
                {
                    // �Զ�����ʱ���ܶ�λλ�ã���Ӱ��������飬�����ڴ�С�ı䣨��������л���������¶�λ���ܳ���ƫ���ǰ�߸�����������ؿ���
                    // �����ϲ���ص��¼�������Ӱ�쵱ǰ�ĵݹ鲼�֣��ʲ���ҪPOST����
                    setToScreen(mCurScreen);
                }
            }
        } else
        { // ����ǵ�һ�β���
            mIsLayout = true;
            if (mCurScreen == -1)
            { // ��û��ѡ��Screen������£���Ĭ��ѡ���һ��
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
            invalidate(); // �ػ�
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
                { // �����ָ��������ʱ�Ѿ���������Ļ�ı䣬�򽫲������������Ļ�ı�
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
                return isFinished ? false : true; // ���ڹ���ʱ�������¼��������أ����Һ����¼�Ҳ��������
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
                            return true; // С��ָ���ǶȽ������أ����Һ����¼�Ҳ��������
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
