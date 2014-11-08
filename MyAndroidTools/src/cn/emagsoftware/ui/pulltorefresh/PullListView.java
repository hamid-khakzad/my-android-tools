package cn.emagsoftware.ui.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;

import cn.emagsoftware.util.LogManager;

/**
 * Created by Wendell on 14-11-8.
 */
public class PullListView extends ListView {

    private static final float OFFSET_RADIO = 1.8f;

    private View mPullView = null;
    private Integer mPullViewHeight = null;
    private float mLastY = -1;
    private Scroller mScroller;

    public PullListView(Context context) {
        super(context);
        mScroller = new Scroller(context, new DecelerateInterpolator());
    }

    public PullListView(Context context, AttributeSet attrs) {
        super(context,attrs);
        mScroller = new Scroller(context, new DecelerateInterpolator());
    }

    public PullListView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        mScroller = new Scroller(context, new DecelerateInterpolator());
    }

    public void addPullView(View v) {
        if(getHeaderViewsCount() > 0) throw new IllegalStateException("addPullView can be called only once and before addHeaderView.");
        mPullView = v;
        mPullView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPullViewHeight = mPullView.getHeight();
                updatePullViewHeight(0);
                mPullView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        LinearLayout wrap = new LinearLayout(getContext());
        wrap.addView(mPullView);
        wrap.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
        addHeaderView(wrap,null,false);
    }

    private void updatePullViewHeight(int height) {
        if(height < 0) height = 0;
        ViewGroup.LayoutParams layoutParams = mPullView.getLayoutParams();
        if(layoutParams == null) layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,height);
        else layoutParams.height = height;
        mPullView.setLayoutParams(layoutParams);
    }

    @Override
    public boolean removeHeaderView(View v) {
        if(mPullView != null && mPullView.getParent() == v) throw new UnsupportedOperationException("current header view is pull view,could not be removed.");
        return super.removeHeaderView(v);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mLastY == -1) {
                    mLastY = ev.getRawY();
                }else {
                    final float deltaY = ev.getRawY() - mLastY;
                    mLastY = ev.getRawY();
                    if(mPullViewHeight != null) {
                        if(getFirstVisiblePosition() == 0 && (mPullView.getHeight() > 0 || deltaY > 0)) {
                            int curHeight = (int)(deltaY/OFFSET_RADIO + mPullView.getHeight());
                            updatePullViewHeight(curHeight);
                        }
                    }
                }
                break;
            default:
                mLastY = -1;
                if(mPullViewHeight != null) {
                    int curHeight = mPullView.getHeight();
                    if(curHeight > 0) {
                        if(getFirstVisiblePosition() == 0) {
                            int finalHeight = 0;
                            if(curHeight > mPullViewHeight) finalHeight = mPullViewHeight;
                            mScroller.startScroll(0, curHeight, 0, finalHeight - curHeight, 400);
                            invalidate();
                        }else {
                            updatePullViewHeight(0);
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            LogManager.logE(PullListView.class,"=========y:" + y);
            updatePullViewHeight(y);
            postInvalidate();
        }
        super.computeScroll();
    }

}
