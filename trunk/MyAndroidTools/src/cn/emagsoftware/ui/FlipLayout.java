package cn.emagsoftware.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 仿Launcher中的WorkSpace，可以左右滑动切换屏幕的类
 * @author Wendell
 * @version 2.1
 */
public class FlipLayout extends ViewGroup {

	private static final String TAG = "FlipLayout";
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	
	private boolean isRendered = false;
	private int mCurScreen = -1;
	
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	
	private static final int SNAP_VELOCITY = 600;
	
	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	private float mLastMotionX;
	//private float mLastMotionY;
	/**是否请求了进行水平的滑动*/
	private boolean mRequestHorizontalFlip = false;
	
	private OnFlingChangedListener listener;
	
	public FlipLayout(Context context){
		this(context,null,0);
	}
	
	public FlipLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FlipLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mScroller = new Scroller(context);
		//mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mTouchSlop = 60;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int childLeft = 0;
		final int childCount = getChildCount();
		
		for (int i=0; i<childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, 
						childLeft+childWidth, childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	//Log.i(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);   
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!"); 
        }
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);   
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");
        }
        
        // The children are given the same width and height as the flipLayout   
        final int count = getChildCount();   
        for (int i = 0; i < count; i++) {   
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);   
        }
        
        //渲染时的执行逻辑
        isRendered = true;
        if(mCurScreen == -1 && getChildCount() > 0){    //在没有选择Screen的情况下，将默认选择第一个
        	setToScreen(0);
        }else if(mCurScreen != -1){
        	if(mCurScreen < 0 || mCurScreen >= getChildCount()) throw new IllegalArgumentException("mCurScreen is out of range!");
        	int width = MeasureSpec.getSize(widthMeasureSpec);
        	scrollTo(mCurScreen*width,0);
        }
    }
    
    /**
     * According to the position of current layout
     * scroll to the destination page.
     */
    public void snapToDestination() {
    	final int screenWidth = getWidth();
    	int destScreen = (getScrollX()+ screenWidth/2)/screenWidth;
    	if(destScreen < 0) destScreen = 0;
    	else if(destScreen >= getChildCount()) destScreen = getChildCount() - 1;
    	snapToScreen(destScreen);
    }
    
    public void snapToScreen(int whichScreen) {
    	// get the valid layout page
    	if(whichScreen < 0 || whichScreen >= getChildCount()) throw new IllegalArgumentException("whichScreen is out of range!");
    	if(isRendered){
        	if (getScrollX() != (whichScreen*getWidth())) {
        		final int delta = whichScreen*getWidth()-getScrollX();
        		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta)*2);
        		invalidate();		// Redraw the layout
        	}
    	}
		if(whichScreen != mCurScreen){
			mCurScreen = whichScreen;
			if(listener != null) listener.onFlingChanged(getChildAt(whichScreen),whichScreen);
		}
    }
    
    public void setToScreen(int whichScreen) {
    	if(whichScreen < 0 || whichScreen >= getChildCount()) throw new IllegalArgumentException("whichScreen is out of range!");
    	if(isRendered) scrollTo(whichScreen*getWidth(), 0);
    	if(whichScreen != mCurScreen){
    		mCurScreen = whichScreen;
    		if(listener != null) listener.onFlingChanged(getChildAt(whichScreen),whichScreen);
    	}
    }
    
    public int getCurScreen() {
    	return mCurScreen;
    }
    
	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		
		final int action = event.getAction();
		final float x = event.getX();
		//final float y = event.getY();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "event down!");
			if (!mScroller.isFinished()){
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;
			
		case MotionEvent.ACTION_MOVE:
			int deltaX = (int)(mLastMotionX - x);
			mLastMotionX = x;
			
            scrollBy(deltaX, 0);
			break;
			
		case MotionEvent.ACTION_UP:
			Log.i(TAG, "event : up");   
            // if (mTouchState == TOUCH_STATE_SCROLLING) {   
            final VelocityTracker velocityTracker = mVelocityTracker;   
            velocityTracker.computeCurrentVelocity(1000);   
            int velocityX = (int) velocityTracker.getXVelocity();   

            Log.i(TAG, "velocityX:"+velocityX); 
            
            if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {   
                // Fling enough to move left   
            	Log.i(TAG, "snap left");
                snapToScreen(mCurScreen - 1);
            } else if (velocityX < -SNAP_VELOCITY   
                    && mCurScreen < getChildCount() - 1) {   
                // Fling enough to move right   
            	Log.i(TAG, "snap right");
                snapToScreen(mCurScreen + 1);
            } else {   
                snapToDestination();
            }   

            if (mVelocityTracker != null) {   
                mVelocityTracker.recycle();   
                mVelocityTracker = null;   
            }   
            // }   
            mTouchState = TOUCH_STATE_REST;   
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "onInterceptTouchEvent-slop:"+mTouchSlop);
		
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		
		final float x = ev.getX();
		//final float y = ev.getY();
		
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int)Math.abs(mLastMotionX-x);
			if (xDiff>mTouchSlop && !mRequestHorizontalFlip) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			//mLastMotionY = y;
			boolean isFinished = mScroller.isFinished();
			mTouchState = isFinished ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			mRequestHorizontalFlip = isFinished ? false : mRequestHorizontalFlip;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			mRequestHorizontalFlip = false;
			break;
		}
		
		return mTouchState != TOUCH_STATE_REST;
	}
	
	public void requestHorizontalFlip(){
		mRequestHorizontalFlip = true;
	}
	
	public void setOnFlingChangedListener(OnFlingChangedListener listener){
		this.listener = listener;
	}
	
	public abstract static interface OnFlingChangedListener{
		public abstract void onFlingChanged(View whichView,int whichScreen);
	}
	
}
