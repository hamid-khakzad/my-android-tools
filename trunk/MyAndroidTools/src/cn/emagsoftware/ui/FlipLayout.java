package cn.emagsoftware.ui;

import java.util.ArrayList;
import java.util.List;

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
 * @version 4.0
 */
public class FlipLayout extends ViewGroup {
	
	private static final String TAG = "FlipLayout";
	private static final int SNAP_VELOCITY = 600;
	
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mTouchSlop;
	private float mLastMotionX;
	
	/**当前Screen的位置*/
	private int mCurScreen = -1;
	/**是否已被布局过*/
	private boolean mIsLayout = false;
	/**当前NO-GONE子View的列表*/
	private List<View> mNoGoneChildren = new ArrayList<View>();
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
	
	/**
	 * <p>由layout方法回调，指定在布局完自己之后，如何布局子View。所以非ViewGroup的View无需实现该方法
	 * <p>layout方法根据measure方法计算出的自身大小来布局自己使产生大小并回调onLayout布局子View。若得到的自身大小和位置与上次一样，将在回调onLayout时给changed参数传入false
	 * <p>measure方法会在调用requestLayout方法时调用并递归计算出所有子View的大小，然后才会递归执行layout方法，而不是在layout方法中逐一调用measure方法
	 * <p>系统不会自动调用requestLayout方法，View需在某些可能改变自身大小的属性设置之后手动调用requestLayout方法
	 * <p>布局之后就需要对界面绘制了，View通过调用invalidate方法进行绘制或重绘，ViewGroup的invalidate方法将会绘制或重绘自己所有的子View，在其他线程中调用绘制或重绘时需执行postInvalidate方法
	 * <p>系统不会自动调用invalidate方法，View需在某些可能改变自身大小的属性设置之后手动调用invalidate方法，invalidate方法实际上调用了View的onDraw方法
	 * <p>绘制并不等同于显示到屏幕，系统会在UI-Thread的Looper处于空闲时，将onDraw方法中绘制在内存的内容渲染到屏幕
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		//不考虑changed参数，因为FlipLayout大小是固定的，但其可见子View的个数可能发生了变化，所以每次都要重新layout
		mNoGoneChildren.clear();
		int childLeft = 0;
		int childWidth = r - l;
		int childHeight = b - t;
		int childCount = getChildCount();
		for (int i = 0;i < childCount;i++) {
			View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				mNoGoneChildren.add(childView);
				childView.layout(childLeft, 0, childLeft+childWidth, childHeight);
				childLeft += childWidth;
			}
		}
		
		int noGoneChildCount = mNoGoneChildren.size();
		if(noGoneChildCount == 0) throw new IllegalStateException("FlipLayout must have one NO-GONE child at least!");
		
		if(mIsLayout){
			if(mCurScreen >= noGoneChildCount) {
				mScroller.forceFinished(true);
				setToScreen(noGoneChildCount-1);
			}
		}else{    //如果是第一次布局
			if(mCurScreen == -1){    //在没有选择Screen的情况下，将默认选择第一个
        		setToScreen(0);
        	}else{
            	if(mCurScreen < 0 || mCurScreen >= noGoneChildCount) throw new IllegalArgumentException("mCurScreen is out of range:"+mCurScreen+"!");
            	scrollTo(mCurScreen*childWidth,0);
        	}
        }
		
		mIsLayout = true;
	}

    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	//大小只支持精确模式
    	int widthMode = MeasureSpec.getMode(widthMeasureSpec);   
        if (widthMode != MeasureSpec.EXACTLY) throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);   
        if (heightMode != MeasureSpec.EXACTLY) throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");
        
        //计算子View大小
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);   
        }
        
        //计算自身大小
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    public void setToScreen(int whichScreen) {
    	if(mIsLayout){
    		if(whichScreen < 0 || whichScreen >= mNoGoneChildren.size()) throw new IllegalArgumentException("whichScreen is out of range:"+whichScreen+"!");
    		scrollTo(whichScreen*getWidth(), 0);
    		if(whichScreen != mCurScreen){
        		mCurScreen = whichScreen;
        		if(listener != null) listener.onFlingChanged(mNoGoneChildren.get(whichScreen),whichScreen);
        	}
    	}else{
    		int noGoneChildIndex = -1;
        	View whichView = null;
        	int count = getChildCount();
            for (int i = 0; i < count; i++) {
            	View childView = getChildAt(i);
            	if (childView.getVisibility() != View.GONE) {
            		if(++noGoneChildIndex == whichScreen){
            			whichView = childView;
            			break;
            		}
            	}
            }
            if(whichView == null) throw new IllegalArgumentException("whichScreen is out of range:"+whichScreen+"!");
            if(whichScreen != mCurScreen){
        		mCurScreen = whichScreen;
        		if(listener != null) listener.onFlingChanged(whichView,whichScreen);
        	}
    	}
    }
    
    public void scrollToScreen(int whichScreen) {
    	if(mIsLayout){
    		if(whichScreen < 0 || whichScreen >= mNoGoneChildren.size()) throw new IllegalArgumentException("whichScreen is out of range:"+whichScreen+"!");
    		int scrollX = getScrollX();
    		int delta = whichScreen*getWidth() - scrollX;
    		mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta)*2);
    		invalidate();    //重绘
    	}else{
    		int noGoneChildIndex = -1;
        	View whichView = null;
        	int count = getChildCount();
            for (int i = 0; i < count; i++) {
            	View childView = getChildAt(i);
            	if (childView.getVisibility() != View.GONE) {
            		if(++noGoneChildIndex == whichScreen){
            			whichView = childView;
            			break;
            		}
            	}
            }
            if(whichView == null) throw new IllegalArgumentException("whichScreen is out of range:"+whichScreen+"!");
            if(whichScreen != mCurScreen){
            	mCurScreen = whichScreen;
    			if(listener != null) listener.onFlingChanged(whichView,whichScreen);
            }
		}
    }
    
    public int getCurScreen() {
    	return mCurScreen;
    }
    
    protected void checkFlingChangedWhenScroll(){
    	int screenWidth = getWidth();
    	int destScreen = (getScrollX()+ screenWidth/2)/screenWidth;
    	int noGoneChildCount = mNoGoneChildren.size();
    	if(destScreen >= noGoneChildCount) destScreen = noGoneChildCount - 1;
    	if(destScreen != mCurScreen){
    		mCurScreen = destScreen;
    		if(listener != null) listener.onFlingChanged(mNoGoneChildren.get(destScreen),destScreen);
    	}
    }
    
    @Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			invalidate();
			checkFlingChangedWhenScroll();
		}
	}
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(event);
		
		int action = event.getAction();
		float x = event.getX();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "event down!");
			if (!mScroller.isFinished()){
				mScroller.forceFinished(true);
			}
			mLastMotionX = x;
			return true;
		case MotionEvent.ACTION_MOVE:
			int deltaX = (int)(mLastMotionX - x);
			mLastMotionX = x;
            scrollBy(deltaX, 0);
            checkFlingChangedWhenScroll();
			return true;
		case MotionEvent.ACTION_UP:
			Log.i(TAG, "event up!");
			mVelocityTracker.computeCurrentVelocity(1000);
            int velocityX = (int) mVelocityTracker.getXVelocity();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
            Log.i(TAG, "velocityX:"+velocityX);
            
            if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {
                //达到了向左移动的速度
            	Log.i(TAG, "snap left");
            	scrollToScreen(mCurScreen - 1);
            } else if (velocityX < -SNAP_VELOCITY && mCurScreen < mNoGoneChildren.size() - 1) {
                //达到了向右移动的速度
            	Log.i(TAG, "snap right");
            	scrollToScreen(mCurScreen + 1);
            } else {
            	int screenWidth = getWidth();
            	int destScreen = (getScrollX()+ screenWidth/2)/screenWidth;
            	int noGoneChildCount = mNoGoneChildren.size();
            	if(destScreen >= noGoneChildCount) destScreen = noGoneChildCount - 1;
            	scrollToScreen(destScreen);
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
		int action = ev.getAction();
		float x = ev.getX();
		
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
