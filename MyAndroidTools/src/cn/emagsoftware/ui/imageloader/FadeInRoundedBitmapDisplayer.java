package cn.emagsoftware.ui.imageloader;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

/**
 * Created by Wendell on 14-12-11.
 */
public class FadeInRoundedBitmapDisplayer extends AllAwareRoundedBitmapDisplayer {

    private final int durationMillis;

    private final boolean animateFromNetwork;
    private final boolean animateFromDisk;
    private final boolean animateFromMemory;

    public FadeInRoundedBitmapDisplayer(int cornerRadiusPixels,int durationMillis) {
        super(cornerRadiusPixels);
        this.durationMillis = durationMillis;
        this.animateFromNetwork = true;
        this.animateFromDisk = true;
        this.animateFromMemory = true;
    }

    public FadeInRoundedBitmapDisplayer(int cornerRadiusPixels, int marginPixels,int durationMillis,boolean animateFromNetwork,boolean animateFromDisk,boolean animateFromMemory) {
        super(cornerRadiusPixels,marginPixels);
        this.durationMillis = durationMillis;
        this.animateFromNetwork = animateFromNetwork;
        this.animateFromDisk = animateFromDisk;
        this.animateFromMemory = animateFromMemory;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        super.display(bitmap, imageAware, loadedFrom);
        if ((animateFromNetwork && loadedFrom == LoadedFrom.NETWORK) ||
                (animateFromDisk && loadedFrom == LoadedFrom.DISC_CACHE) ||
                (animateFromMemory && loadedFrom == LoadedFrom.MEMORY_CACHE)) {
            FadeInBitmapDisplayer.animate(imageAware.getWrappedView(), durationMillis);
        }
    }

}
