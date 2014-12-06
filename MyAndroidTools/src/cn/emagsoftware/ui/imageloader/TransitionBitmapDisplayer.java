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
 * @deprecated 该类在ImageView的ScaleType发生改变时效果会不理想，可使用{@link com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer}代替，可使用View重叠解决FadeInBitmapDisplayer淡入显示时导致的空白问题
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
        if ((animateFromNetwork && loadedFrom == LoadedFrom.NETWORK) ||
                (animateFromDisc && loadedFrom == LoadedFrom.DISC_CACHE) ||
                (animateFromMemory && loadedFrom == LoadedFrom.MEMORY_CACHE)) {
            MyTransitionDrawable transDrawable = new MyTransitionDrawable(imageAware.getWrappedView().getResources().getDrawable(defaultDrawableId), new BitmapDrawable(bitmap));
            transDrawable.setCrossFadeEnabled(true);
            imageAware.setImageDrawable(transDrawable);
            transDrawable.startTransition(durationMillis);
        }else {
            imageAware.setImageBitmap(bitmap);
        }
    }

    private static class MyTransitionDrawable extends TransitionDrawable {

        private Drawable mainDrawable = null;

        public MyTransitionDrawable(Drawable defDrawable, Drawable mainDrawable) {
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
