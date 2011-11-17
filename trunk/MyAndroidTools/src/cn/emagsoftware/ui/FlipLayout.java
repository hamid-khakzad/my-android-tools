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
 * ��Launcher�е�WorkSpace���������һ����л���Ļ����
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
	
	/**��ǰScreen��λ��*/
	private int mCurScreen = -1;
	/**�Ƿ��ѱ����ֹ�*/
	private boolean mIsLayout = false;
	/**��ǰNO-GONE��View���б�*/
	private List<View> mNoGoneChildren = new ArrayList<View>();
	/**�Ƿ������˽���ˮƽ�Ļ���*/
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
	 * <p>��layout�����ص���ָ���ڲ������Լ�֮����β�����View�����Է�ViewGroup��View����ʵ�ָ÷���
	 * <p>layout��������measure����������������С�������Լ�ʹ������С���ص�onLayout������View�����õ��������С��λ�����ϴ�һ�������ڻص�onLayoutʱ��changed��������false
	 * <p>measure�������ڵ���requestLayout����ʱ���ò��ݹ�����������View�Ĵ�С��Ȼ��Ż�ݹ�ִ��layout��������������layout��������һ����measure����
	 * <p>ϵͳ�����Զ�����requestLayout������View����ĳЩ���ܸı������С����������֮���ֶ�����requestLayout����
	 * <p>����֮�����Ҫ�Խ�������ˣ�Viewͨ������invalidate�������л��ƻ��ػ棬ViewGroup��invalidate����������ƻ��ػ��Լ����е���View���������߳��е��û��ƻ��ػ�ʱ��ִ��postInvalidate����
	 * <p>ϵͳ�����Զ�����invalidate������View����ĳЩ���ܸı������С����������֮���ֶ�����invalidate������invalidate����ʵ���ϵ�����View��onDraw����
	 * <p>���Ʋ�����ͬ����ʾ����Ļ��ϵͳ����UI-Thread��Looper���ڿ���ʱ����onDraw�����л������ڴ��������Ⱦ����Ļ
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		//������changed��������ΪFlipLayout��С�ǹ̶��ģ�����ɼ���View�ĸ������ܷ����˱仯������ÿ�ζ�Ҫ����layout
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
		}else{    //����ǵ�һ�β���
			if(mCurScreen == -1){    //��û��ѡ��Screen������£���Ĭ��ѡ���һ��
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
    	//��Сֻ֧�־�ȷģʽ
    	int widthMode = MeasureSpec.getMode(widthMeasureSpec);   
        if (widthMode != MeasureSpec.EXACTLY) throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);   
        if (heightMode != MeasureSpec.EXACTLY) throw new IllegalStateException("FlipLayout only can run at EXACTLY mode!");
        
        //������View��С
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);   
        }
        
        //���������С
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
    		invalidate();    //�ػ�
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
                //�ﵽ�������ƶ����ٶ�
            	Log.i(TAG, "snap left");
            	scrollToScreen(mCurScreen - 1);
            } else if (velocityX < -SNAP_VELOCITY && mCurScreen < mNoGoneChildren.size() - 1) {
                //�ﵽ�������ƶ����ٶ�
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
			return isFinished ? false : true;    //���ڹ���ʱ�������¼��������أ����Һ����¼�Ҳ��������
		case MotionEvent.ACTION_MOVE:
			if(mRequestHorizontalFlip) return false;
			else{
				final int xDistence = (int)Math.abs(x-mLastMotionX);
				if(xDistence > mTouchSlop) return true;    //����ָ�����뽫�����أ����Һ����¼�Ҳ��������
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
