package cn.emagsoftware.ui.imageloader;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

/**
 * Created by Wendell on 14-3-18.
 */
public class TransitionBitmapDisplayer implements BitmapDisplayer {

    private final int defaultDrawableId;
    private final int durationMillis;

    private final boolean animateFromNetwork;
    private final boolean animateFromDisc;
    private final boolean animateFromMemory;

    public TransitionBitmapDisplayer(int defaultDrawableId, int durationMillis) {
        this(defaultDrawableId, durationMillis, true, true, true);
    }

    public TransitionBitmapDisplayer(int defaultDrawableId, int durationMillis, boolean animateFromNetwork, boolean animateFromDisc, boolean animateFromMemory) {
        this.defaultDrawableId = defaultDrawableId;
        this.durationMillis = durationMillis;
        this.animateFromNetwork = animateFromNetwork;
        this.animateFromDisc = animateFromDisc;
        this.animateFromMemory = animateFromMemory;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        Drawable drawable = convert(bitmap);
        if ((animateFromNetwork && loadedFrom == LoadedFrom.NETWORK) ||
                (animateFromDisc && loadedFrom == LoadedFrom.DISC_CACHE) ||
                (animateFromMemory && loadedFrom == LoadedFrom.MEMORY_CACHE)) {
            MyTransitionDrawable transDrawable = new MyTransitionDrawable(imageAware.getWrappedView().getResources().getDrawable(defaultDrawableId),drawable==null?new BitmapDrawable(bitmap):drawable);
            transDrawable.setCrossFadeEnabled(true);
            imageAware.setImageDrawable(transDrawable);
            transDrawable.startTransition(durationMillis);
        }else {
            if(drawable == null) imageAware.setImageBitmap(bitmap);
            else imageAware.setImageDrawable(drawable);
        }
    }

    protected Drawable convert(Bitmap bitmap) {
        return null;
    }

    private static class MyTransitionDrawable extends TransitionDrawable {

        private Drawable mainDrawable = null;

        public MyTransitionDrawable(Drawable defDrawable,Drawable mainDrawable) {
            super(new Drawable[]{defDrawable, mainDrawable});
            this.mainDrawable = mainDrawable;
        }

        @Override
        public int getIntrinsicWidth() {
            return mainDrawable.getIntrinsicWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return mainDrawable.getIntrinsicHeight();
        }

    }

}
