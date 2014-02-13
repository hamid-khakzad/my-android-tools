package cn.emagsoftware.ui.imageloader;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

/**
 * Created by Wendell on 14-2-13.
 */
public class RoundedFadeInBitmapDisplayer extends RoundedBitmapDisplayer {

    private final int durationMillis;

    private final boolean animateFromNetwork;
    private final boolean animateFromDisc;
    private final boolean animateFromMemory;

    public RoundedFadeInBitmapDisplayer(int cornerRadiusPixels,int durationMillis) {
        super(cornerRadiusPixels);
        this.durationMillis = durationMillis;
        this.animateFromNetwork = true;
        this.animateFromDisc = true;
        this.animateFromMemory = true;
    }

    public RoundedFadeInBitmapDisplayer(int cornerRadiusPixels,int durationMillis, boolean animateFromNetwork, boolean animateFromDisc, boolean animateFromMemory) {
        super(cornerRadiusPixels);
        this.durationMillis = durationMillis;
        this.animateFromNetwork = animateFromNetwork;
        this.animateFromDisc = animateFromDisc;
        this.animateFromMemory = animateFromMemory;
    }

    public RoundedFadeInBitmapDisplayer(int cornerRadiusPixels, int marginPixels,int durationMillis) {
        super(cornerRadiusPixels,marginPixels);
        this.durationMillis = durationMillis;
        this.animateFromNetwork = true;
        this.animateFromDisc = true;
        this.animateFromMemory = true;
    }

    public RoundedFadeInBitmapDisplayer(int cornerRadiusPixels, int marginPixels,int durationMillis, boolean animateFromNetwork, boolean animateFromDisc, boolean animateFromMemory) {
        super(cornerRadiusPixels,marginPixels);
        this.durationMillis = durationMillis;
        this.animateFromNetwork = animateFromNetwork;
        this.animateFromDisc = animateFromDisc;
        this.animateFromMemory = animateFromMemory;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        super.display(bitmap, imageAware, loadedFrom);
        if ((animateFromNetwork && loadedFrom == LoadedFrom.NETWORK) ||
                (animateFromDisc && loadedFrom == LoadedFrom.DISC_CACHE) ||
                (animateFromMemory && loadedFrom == LoadedFrom.MEMORY_CACHE)) {
            FadeInBitmapDisplayer.animate(imageAware.getWrappedView(), durationMillis);
        }
    }

}
