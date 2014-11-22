package cn.emagsoftware.ui.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Wendell on 14-11-11.
 */
class PullViewWrapper extends ViewGroup {

    private OnPullViewLayoutListener mListener = null;

    public PullViewWrapper(Context context) {
        super(context);
    }

    public PullViewWrapper(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public PullViewWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getChildCount() != 1) throw new IllegalStateException("should have only one child at any time.");

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        View child = getChildAt(0);
        LayoutParams childParams = child.getLayoutParams();
        int childWidth = childParams.width;
        int childHeight = childParams.height;
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        if(childWidth == LayoutParams.MATCH_PARENT || childWidth == LayoutParams.WRAP_CONTENT) {
            if(childWidth == LayoutParams.MATCH_PARENT && widthMode == MeasureSpec.EXACTLY) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize - getPaddingLeft() - getPaddingRight(),MeasureSpec.EXACTLY);
            }else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
            }
        }else {
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth,MeasureSpec.EXACTLY);
        }
        if(childHeight == LayoutParams.MATCH_PARENT || childHeight == LayoutParams.WRAP_CONTENT) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        }else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight,MeasureSpec.EXACTLY);
        }
        child.measure(childWidthMeasureSpec,childHeightMeasureSpec);

        if(widthMode != MeasureSpec.EXACTLY) {
            int width = child.getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
            width = Math.max(width,getSuggestedMinimumWidth());
            if(widthMode == MeasureSpec.AT_MOST && width > widthSize) width = widthSize;
            widthSize = width;
        }
        if(heightMode != MeasureSpec.EXACTLY) {
            int height = child.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
            height = Math.max(height,getSuggestedMinimumHeight());
            if(heightMode == MeasureSpec.AT_MOST && height > heightSize) height = heightSize;
            heightSize = height;
        }
        setMeasuredDimension(widthSize,heightSize);
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {
        View child = getChildAt(0);
        int childHeight = child.getMeasuredHeight();
        int left = getPaddingLeft();
        int top = getMeasuredHeight() - getPaddingBottom() - childHeight;
        child.layout(left,top,left + child.getMeasuredWidth(),top + childHeight);
        if(mListener != null) mListener.onPullViewLayout(child);
    }

    public void setOnPullViewLayoutListener(OnPullViewLayoutListener listener) {
        mListener = listener;
    }

    public static interface OnPullViewLayoutListener {
        public void onPullViewLayout(View pullView);
    }

}
