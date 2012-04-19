package cn.emagsoftware.memory;

import java.util.LinkedList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public abstract class AbstractBitmapSelector
{

    protected Resources res                  = null;
    /**
     * 出于节约资源的考虑，View.setBackgroundResource等方法内部若涉及到Bitmap，将使用同一个。 通过传入该参数可在选择Bitmap时排除这类Bitmap，因为可能会对选择的Bitmap执行recycle， 若recycle了这类Bitmap，将使这类Bitmap永远不能再通过View.setBackgroundResource等方法
     * 再次使用，已经使用了这类Bitmap的View在重绘时也将抛出异常。
     */
    protected int[]     drawableIdsToExclude = null;

    public AbstractBitmapSelector()
    {
    }

    public AbstractBitmapSelector(Resources res, int[] drawableIdsToExclude)
    {
        if (res == null || drawableIdsToExclude == null)
            throw new NullPointerException();
        this.res = res;
        this.drawableIdsToExclude = drawableIdsToExclude;
    }

    public static List<Bitmap> drawableToBitmaps(Drawable drawable, boolean includeRecycled)
    {
        List<Bitmap> bitmaps = new LinkedList<Bitmap>();
        if (drawable instanceof BitmapDrawable)
        {
            Bitmap b = ((BitmapDrawable) drawable).getBitmap();
            if (b != null)
            {
                if (includeRecycled)
                    bitmaps.add(b);
                else if (!b.isRecycled())
                    bitmaps.add(b);
            }
        }
        return bitmaps;
    }

    protected abstract List<Drawable> onSelect(View view);

    public List<Bitmap> select(View view, boolean includeRecycled)
    {
        List<Bitmap> bitmaps = new LinkedList<Bitmap>();
        List<Drawable> drawables = onSelect(view);
        if (drawables != null)
        {
            for (Drawable drawable : drawables)
            {
                List<Bitmap> curBitmaps = drawableToBitmaps(drawable, includeRecycled);
                if (drawableIdsToExclude == null || drawableIdsToExclude.length == 0)
                {
                    bitmaps.addAll(curBitmaps);
                } else
                {
                    for (Bitmap curBitmap : curBitmaps)
                    {
                        boolean shouldExclude = false;
                        for (int drawableId : drawableIdsToExclude)
                        {
                            Drawable drawableToExclude = res.getDrawable(drawableId);
                            if (drawableToExclude != null)
                            {
                                List<Bitmap> curBitmapsToExclude = drawableToBitmaps(drawableToExclude, includeRecycled);
                                for (Bitmap curBitmapToExclude : curBitmapsToExclude)
                                {
                                    if (curBitmap == curBitmapToExclude)
                                    {
                                        shouldExclude = true;
                                        break;
                                    }
                                }
                                if (shouldExclude)
                                    break;
                            }
                        }
                        if (!shouldExclude)
                            bitmaps.add(curBitmap);
                    }
                }
            }
        }
        return bitmaps;
    }

}
