package cn.emagsoftware.ui;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.emagsoftware.util.LogManager;

/**
 * Created by Wendell on 14-2-20.
 */
public class BugFixedSlidingPaneLayout extends SlidingPaneLayout {

    public BugFixedSlidingPaneLayout(Context context) {
        super(context);
    }

    public BugFixedSlidingPaneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BugFixedSlidingPaneLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        }catch (ArrayIndexOutOfBoundsException e) {
            LogManager.logE(BugFixedSlidingPaneLayout.class, "internal bug in SlidingPaneLayout,this bug can be ignored in most cases.", e);
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        }catch (ArrayIndexOutOfBoundsException e) {
            LogManager.logE(BugFixedSlidingPaneLayout.class, "internal bug in SlidingPaneLayout,this bug can be ignored in most cases.", e);
            return false;
        }catch (IllegalArgumentException e) {
            LogManager.logE(BugFixedSlidingPaneLayout.class, "internal bug in SlidingPaneLayout,this bug can be ignored in most cases.", e);
            return false;
        }
    }

}
