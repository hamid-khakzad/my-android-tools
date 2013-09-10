package cn.emagsoftware.ui.pageradapterview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Wendell on 13-9-10.
 */
public class NoFlipViewPager extends ViewPager {

    public NoFlipViewPager(Context context) {
        super(context);
    }

    public NoFlipViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

}
