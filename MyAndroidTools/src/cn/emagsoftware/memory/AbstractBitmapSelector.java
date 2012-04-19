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
     * ���ڽ�Լ��Դ�Ŀ��ǣ�View.setBackgroundResource�ȷ����ڲ����漰��Bitmap����ʹ��ͬһ���� ͨ������ò�������ѡ��Bitmapʱ�ų�����Bitmap����Ϊ���ܻ��ѡ���Bitmapִ��recycle�� ��recycle������Bitmap����ʹ����Bitmap��Զ������ͨ��View.setBackgroundResource�ȷ���
     * �ٴ�ʹ�ã��Ѿ�ʹ��������Bitmap��View���ػ�ʱҲ���׳��쳣��
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
