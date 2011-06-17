package cn.emagsoftware.ui;

import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;

/**
 * <p>implements OnGestureListener
 * @author Wendell
 * @version 1.1
 */
public abstract class OnGestureBaseListener implements OnGestureListener {
	
	protected int swipeMinDistance = 120;    //滑动的最短距离
	protected int swipeMinVelocity = 200;    //滑动的最小速度
	
	@Override
	public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY){
		// 参数解释：
		// e1：第1个ACTION_DOWN MotionEvent
		// e2：最后一个ACTION_MOVE MotionEvent
		// velocityX：X轴上的移动速度，像素/秒
		// velocityY：Y轴上的移动速度，像素/秒
		float xDistance = e2.getX() - e1.getX();
		float yDistance = e2.getY() - e1.getY();
		if(Math.abs(xDistance) >= swipeMinDistance && Math.abs(velocityX) >= swipeMinVelocity){
			if(xDistance >= 0) return onSwipeRight(e1,e2,velocityX,velocityY);
			else return onSwipeLeft(e1,e2,velocityX,velocityY);
		}
		if(Math.abs(yDistance) >= swipeMinDistance && Math.abs(velocityY) >= swipeMinVelocity){
			if(yDistance >= 0) return onSwipeBottom(e1,e2,velocityX,velocityY);
			else return onSwipeTop(e1,e2,velocityX,velocityY);
		}
		return false;
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * <p>向左滑动
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return
	 */
	public abstract boolean onSwipeLeft(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY);
	
	/**
	 * <p>向右滑动
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return
	 */
	public abstract boolean onSwipeRight(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY);
	
	/**
	 * <p>向上滑动
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return
	 */
	public abstract boolean onSwipeTop(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY);
	
	/**
	 * <p>向下滑动
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return
	 */
	public abstract boolean onSwipeBottom(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY);
	
}
