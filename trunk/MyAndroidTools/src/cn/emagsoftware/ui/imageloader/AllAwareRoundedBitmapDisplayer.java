package cn.emagsoftware.ui.imageloader;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

/**
 * Created by Wendell on 14-12-6.
 */
public class AllAwareRoundedBitmapDisplayer extends RoundedBitmapDisplayer {

    public AllAwareRoundedBitmapDisplayer(int cornerRadiusPixels) {
        super(cornerRadiusPixels);
    }

    public AllAwareRoundedBitmapDisplayer(int cornerRadiusPixels, int marginPixels) {
        super(cornerRadiusPixels,marginPixels);
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        imageAware.setImageDrawable(new RoundedDrawable(bitmap, cornerRadius, margin));
    }

}
