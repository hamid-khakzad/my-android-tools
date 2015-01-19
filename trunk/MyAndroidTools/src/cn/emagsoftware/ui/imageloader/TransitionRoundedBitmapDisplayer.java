package cn.emagsoftware.ui.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * Created by Wendell on 14-12-12.
 */
public class TransitionRoundedBitmapDisplayer extends TransitionBitmapDisplayer {

    private final int cornerRadius;
    private final int margin;

    public TransitionRoundedBitmapDisplayer(int defaultDrawableId,int durationMillis,int cornerRadiusPixels) {
        super(defaultDrawableId,durationMillis);
        this.cornerRadius = cornerRadiusPixels;
        this.margin = 0;
    }

    public TransitionRoundedBitmapDisplayer(int defaultDrawableId, int durationMillis, boolean animateFromNetwork, boolean animateFromDisc, boolean animateFromMemory,int cornerRadiusPixels, int marginPixels) {
        super(defaultDrawableId,durationMillis,animateFromNetwork,animateFromDisc,animateFromMemory);
        this.cornerRadius = cornerRadiusPixels;
        this.margin = marginPixels;
    }

    @Override
    protected Drawable convert(Bitmap bitmap) {
        return new RoundedBitmapDisplayer.RoundedDrawable(bitmap, cornerRadius, margin);
    }

}
