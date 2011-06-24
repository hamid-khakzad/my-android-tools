package cn.emagsoftware.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 仿Launcher中的WorkSpace，可以左右滑动切换屏幕的类
 * 该类是在Yao.GUET提供的ScrollLayout类的基础上修改完成，Yao.GUET的blog地址为http://blog.csdn.net/Yao_GUET
 * @author Wendell
 * @version 1.3
 */
public class FlipLayout extends ViewGroup {

	private static final String TAG = "FlipLayout";
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	
	private boolean isRendered = false;
	private int mCurScreen = -1;
	private int mTempCurScreen = -1;
	
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	
	private static final int SNAP_VELOCITY = 600;
	
	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	private float mLastMotionX;
	//private float mLastMotionY;
	
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
		if(attrs != null){
			//FlipLayout支持以下定义属性
			String toScreenIndexStr = attrs.getAttributeValue(null, "toScreen");
			if(toScreenIndexStr != null) setToScreen(Integer.valueOf(toScreenIndexStr));
		}
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		if (changed) {
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
	}

    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	Log.e(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);   
  
        final int width = MeasureSpec.getSize(widthMeasureSpec);   
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
        
        // Log.e(TAG, "moving to screen "+mCurScreen);
        if(mTempCurScreen != -1){
        	mTempCurScreen = Math.max(0, Math.min(mTempCurScreen, count-1));
        	scrollTo(mTempCurScreen*width,0);
        	mCurScreen = mTempCurScreen;
        	mTempCurScreen = -1;
        	if(listener != null) listener.onFlingChanged(getChildAt(mCurScreen),mCurScreen);
        }else if(mCurScreen == -1){
        	scrollTo(0,0);
        	mCurScreen = 0;
        	if(listener != null) listener.onFlingChanged(getChildAt(mCurScreen),mCurScreen);
        }else{
        	scrollTo(mCurScreen*width,0);
        }
        isRendered = true;
    }
    
    /**
     * According to the position of current layout
     * scroll to the destination page.
     */
    public void snapToDestination() {
    	final int screenWidth = getWidth();
    	final int destScreen = (getScrollX()+ screenWidth/2)/screenWidth;
    	snapToScreen(destScreen);
    }
    
    public void snapToScreen(int whichScreen) {
    	// get the valid layout page
    	if(!isRendered){
    		this.mTempCurScreen = whichScreen;
    		return;
    	}
    	whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));
    	if (getScrollX() != (whichScreen*getWidth())) {
    		final int delta = whichScreen*getWidth()-getScrollX();
    		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta)*2);
    		invalidate();		// Redraw the layout
    		if(mCurScreen != whichScreen){
    			mCurScreen = whichScreen;
    			if(listener != null) listener.onFlingChanged(getChildAt(whichScreen),whichScreen);
    		}
    	}
    }
    
    public void setToScreen(int whichScreen) {
    	if(!isRendered){
    		this.mTempCurScreen = whichScreen;
    		return;
    	}
    	whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));
    	scrollTo(whichScreen*getWidth(), 0);
    	if(mCurScreen != whichScreen){
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
			Log.e(TAG, "event down!");
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
			Log.e(TAG, "event : up");   
            // if (mTouchState == TOUCH_STATE_SCROLLING) {   
            final VelocityTracker velocityTracker = mVelocityTracker;   
            velocityTracker.computeCurrentVelocity(1000);   
            int velocityX = (int) velocityTracker.getXVelocity();   

            Log.e(TAG, "velocityX:"+velocityX); 
            
            if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {   
                // Fling enough to move left   
            	Log.e(TAG, "snap left");
                snapToScreen(mCurScreen - 1);
            } else if (velocityX < -SNAP_VELOCITY   
                    && mCurScreen < getChildCount() - 1) {   
                // Fling enough to move right   
            	Log.e(TAG, "snap right");
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
		Log.e(TAG, "onInterceptTouchEvent-slop:"+mTouchSlop);
		
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE) && 
				(mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		
		final float x = ev.getX();
		//final float y = ev.getY();
		
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int)Math.abs(mLastMotionX-x);
			if (xDiff>mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
				
			}
			break;
			
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			//mLastMotionY = y;
			mTouchState = mScroller.isFinished()? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;
			
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		
		return mTouchState != TOUCH_STATE_REST;
	}
	
	public void setOnFlingChangedListener(OnFlingChangedListener listener){
		this.listener = listener;
	}
	
	public abstract static interface OnFlingChangedListener{
		public abstract void onFlingChanged(View whichView,int whichScreen);
	}
	
}
