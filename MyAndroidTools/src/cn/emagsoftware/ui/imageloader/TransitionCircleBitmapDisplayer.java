package cn.emagsoftware.ui.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by Wendell on 14-12-12.
 */
public class TransitionCircleBitmapDisplayer extends TransitionBitmapDisplayer {

    public TransitionCircleBitmapDisplayer(int defaultDrawableId, int durationMillis) {
        super(defaultDrawableId,durationMillis);
    }

    public TransitionCircleBitmapDisplayer(int defaultDrawableId, int durationMillis, boolean animateFromNetwork, boolean animateFromDisc, boolean animateFromMemory) {
        super(defaultDrawableId,durationMillis,animateFromNetwork,animateFromDisc,animateFromMemory);
    }

    @Override
    protected Drawable convert(Bitmap bitmap) {
        return new CircleBitmapDisplayer.CircleDrawable(bitmap);
    }

}
