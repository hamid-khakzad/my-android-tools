package cn.emagsoftware.ui;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
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
 * @version 3.0
 */
public class FlipLayout extends ViewGroup {

	private static final String TAG = "FlipLayout";
	private Scroller mScroller;
	/**监控mScroller是否停止滚动的定时器*/
	private Timer mScrollerTimer = new Timer();
	/**监控mScroller是否停止滚动的任务*/
	private TimerTask mScrollerTask = null;
	private VelocityTracker mVelocityTracker;
	
	private boolean isRendered = false;
	private int mCurScreen = -1;
	/**在onTouchEvent事件中使用到的临时当前Screen序号*/
	private int mTempCurScreen = -1;
	
	private static final int SNAP_VELOCITY = 600;
	
	private int mTouchSlop;
	private float mLastMotionX;
	/**一连串ACTION_MOVE事件中的第一个是否已经过去*/
	private boolean isFirstMoveActionPassed = false;
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
        int childCount = getChildCount();
        if(childCount == 0){
        	throw new IllegalArgumentException("FlipLayout must have one child at least!");
        }else{
        	if(mCurScreen == -1){    //在没有选择Screen的情况下，将默认选择第一个
        		setToScreen(0);
        	}else{
            	if(mCurScreen < 0 || mCurScreen >= childCount) throw new IllegalArgumentException("mCurScreen is out of range:"+mCurScreen+"!");
            	int width = MeasureSpec.getSize(widthMeasureSpec);
            	scrollTo(mCurScreen*width,0);
        	}
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
    
    public void snapToScreen(final int whichScreen) {
    	// get the valid layout page
    	if(whichScreen < 0 || whichScreen >= getChildCount()) throw new IllegalArgumentException("whichScreen is out of range:"+whichScreen+"!");
    	if(isRendered){
    		if(mScrollerTask != null) mScrollerTask.cancel();
    		final int toWidth = whichScreen*getWidth();
			final int scrollX = getScrollX();
			final int delta = toWidth-scrollX;
    		mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta)*2);
    		invalidate();		// Redraw the layout
    		if(whichScreen != mCurScreen){
    			final Handler handler = new Handler();
    			mScrollerTask = new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(mScroller.isFinished()){
							this.cancel();
							if(getScrollX() != toWidth) return;    //被中断将不回调
							handler.post(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									mCurScreen = whichScreen;
									if(listener != null) listener.onFlingChanged(getChildAt(whichScreen),whichScreen);
								}
							});
						}
					}
				};
				mScrollerTimer.purge();
				mScrollerTimer.schedule(mScrollerTask,0,100);
    		}
    	}else if(whichScreen != mCurScreen){
			mCurScreen = whichScreen;
			if(listener != null) listener.onFlingChanged(getChildAt(whichScreen),whichScreen);
		}
    }
    
    public void setToScreen(int whichScreen) {
    	if(whichScreen < 0 || whichScreen >= getChildCount()) throw new IllegalArgumentException("whichScreen is out of range:"+whichScreen+"!");
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
		if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(event);
		
		final int action = event.getAction();
		final float x = event.getX();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "event down!");
			if (!mScroller.isFinished()){
				mScroller.forceFinished(true);
			}
			mLastMotionX = x;
			return true;
		case MotionEvent.ACTION_MOVE:
			if(!isFirstMoveActionPassed){
		    	final int screenWidth = getWidth();
		    	mTempCurScreen = (getScrollX()+ screenWidth/2)/screenWidth;
		    	if(mTempCurScreen < 0) mTempCurScreen = 0;
		    	else if(mTempCurScreen >= getChildCount()) mTempCurScreen = getChildCount() - 1;
				isFirstMoveActionPassed = true;
			}
			
			int deltaX = (int)(mLastMotionX - x);
			mLastMotionX = x;
            scrollBy(deltaX, 0);
            
			return true;
		case MotionEvent.ACTION_UP:
			Log.i(TAG, "event up!");
			isFirstMoveActionPassed = false;
			mVelocityTracker.computeCurrentVelocity(1000);
            int velocityX = (int) mVelocityTracker.getXVelocity();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
            Log.i(TAG, "velocityX:"+velocityX);
            
            if (velocityX > SNAP_VELOCITY && mTempCurScreen > 0) {
                // Fling enough to move left   
            	Log.i(TAG, "snap left");
                snapToScreen(mTempCurScreen - 1);
            } else if (velocityX < -SNAP_VELOCITY && mTempCurScreen < getChildCount() - 1) {
                // Fling enough to move right   
            	Log.i(TAG, "snap right");
                snapToScreen(mTempCurScreen + 1);
            } else {
                snapToDestination();
            }
            
            return true;
		case MotionEvent.ACTION_CANCEL:
			return true;
		default:
			return true;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		final int action = ev.getAction();
		final float x = ev.getX();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			boolean isFinished = mScroller.isFinished();
			mRequestHorizontalFlip = false;
			return isFinished ? false : true;    //正在滚动时发生的事件将被拦截，并且后续事件也将被拦截
		case MotionEvent.ACTION_MOVE:
			if(mRequestHorizontalFlip) return false;
			else{
				final int xDistence = (int)Math.abs(x-mLastMotionX);
				if(xDistence > mTouchSlop) return true;    //超过指定距离将被拦截，并且后续事件也将被拦截
				else return false;
			}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			return false;
		default:
			return false;
		}
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
