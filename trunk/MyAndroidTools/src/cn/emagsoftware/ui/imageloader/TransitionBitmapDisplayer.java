package cn.emagsoftware.ui.imageloader;

import android.R;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

/**
 * Created by Wendell on 14-3-18.
 */
public class TransitionBitmapDisplayer implements BitmapDisplayer {

    private final int durationMillis;

    private final boolean animateFromNetwork;
    private final boolean animateFromDisc;
    private final boolean animateFromMemory;

    public TransitionBitmapDisplayer(int durationMillis) {
        this(durationMillis, true, true, true);
    }

    public TransitionBitmapDisplayer(int durationMillis, boolean animateFromNetwork, boolean animateFromDisc, boolean animateFromMemory) {
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
            MyTransitionDrawable transDrawable = new MyTransitionDrawable(drawable==null?new BitmapDrawable(bitmap):drawable);
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

        public MyTransitionDrawable(Drawable mainDrawable) {
            super(new Drawable[]{new ColorDrawable(R.color.transparent), mainDrawable});
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
