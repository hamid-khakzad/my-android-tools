package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Gallery;

/**
 * Created by Wendell on 14-6-9.
 */
public class InterceptGallery extends Gallery {

    private int mTouchSlop;
    private MotionEvent mDownEvent;

    public InterceptGallery(Context context)
    {
        super(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public InterceptGallery(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public InterceptGallery(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        int action = ev.getAction();
        if(action == MotionEvent.ACTION_DOWN){
            mDownEvent = MotionEvent.obtain(ev);
            mDownEvent.recycle();
        }else if(mDownEvent != null && action == MotionEvent.ACTION_MOVE){
            float x = ev.getX();
            float y = ev.getY();
            float xFloat = x - mDownEvent.getX();
            float yFloat = y - mDownEvent.getY();
            final float xDistence = Math.abs(xFloat);
            final float yDistence = Math.abs(yFloat);
            if(xDistence > mTouchSlop || yDistence > mTouchSlop) //容错范围
            {
                boolean intercept = false;
                if(xDistence != 0){
                    double angle = Math.toDegrees(Math.atan(yDistence / xDistence));
                    if (angle <= 45) { // 小于指定角度将被拦截
                        onTouchEvent(mDownEvent);
                        intercept = true;
                    }
                }
                mDownEvent = null;
                if(intercept) {
                    return true;
                }
            }
        }
        return result;
    }

}
