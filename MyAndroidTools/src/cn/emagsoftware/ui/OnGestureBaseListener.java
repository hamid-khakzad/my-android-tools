package cn.emagsoftware.ui;

import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;

/**
 * <p>implements OnGestureListener
 * @author Wendell
 * @version 1.1
 */
public abstract class OnGestureBaseListener implements OnGestureListener {
	
	protected int swipeMinDistance = 120;    //��������̾���
	protected int swipeMinVelocity = 200;    //��������С�ٶ�
	
	@Override
	public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY){
		// �������ͣ�
		// e1����1��ACTION_DOWN MotionEvent
		// e2�����һ��ACTION_MOVE MotionEvent
		// velocityX��X���ϵ��ƶ��ٶȣ�����/��
		// velocityY��Y���ϵ��ƶ��ٶȣ�����/��
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
	 * <p>���󻬶�
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return
	 */
	public abstract boolean onSwipeLeft(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY);
	
	/**
	 * <p>���һ���
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return
	 */
	public abstract boolean onSwipeRight(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY);
	
	/**
	 * <p>���ϻ���
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return
	 */
	public abstract boolean onSwipeTop(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY);
	
	/**
	 * <p>���»���
	 * @param e1
	 * @param e2
	 * @param velocityX
	 * @param velocityY
	 * @return
	 */
	public abstract boolean onSwipeBottom(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY);
	
}
