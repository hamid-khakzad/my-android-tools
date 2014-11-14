package cn.emagsoftware.ui.textstyle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ImageSpan;

import java.lang.ref.WeakReference;

/**
 * Created by Wendell on 14-11-14.
 */
public class CenteredImageSpan extends ImageSpan {

    private WeakReference<Drawable> mDrawableRef;

    public CenteredImageSpan(Bitmap b) {
        super(b);
    }

    public CenteredImageSpan(Context context,Bitmap b) {
        super(context,b);
    }

    public CenteredImageSpan(Drawable d) {
        super(d);
    }

    public CenteredImageSpan(Drawable d,String source) {
        super(d,source);
    }

    public CenteredImageSpan(Context context,Uri uri) {
        super(context,uri);
    }

    public CenteredImageSpan(Context context,int resourceId) {
        super(context,resourceId);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();
        int transY = 0;
        //��ý�Ҫ��ʾ���ı��߶�-ͼƬ�߶ȳ�2�Ⱦ���λ��+top(�������)
        transY = ((bottom-top) - b.getBounds().bottom)/2+top;
        //ƫ�ƻ�����ʼ����
        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Drawable d = getCachedDrawable();
        Rect rect = d.getBounds();
        if (fm != null) {
            Paint.FontMetricsInt fmPaint=paint.getFontMetricsInt();
            //������֡�ͼƬ�߶�
            int fontHeight = fmPaint.bottom - fmPaint.top;
            int drHeight=rect.bottom-rect.top;
            //��������㷨LZ��ʾҲ���⣬�����߼�Ӧ��ͬdraw�еļ���һ��������ʾ�Ľ�������У��������ε���֮��ŷ�����ô��Ż����
            int top= drHeight/2 - fontHeight/4;
            int bottom=drHeight/2 + fontHeight/4;
            fm.ascent=-bottom;
            fm.top=-bottom;
            fm.bottom=top;
            fm.descent=top;
        }
        return rect.right;
    }

    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;
        if (wr != null) d = wr.get();
        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<Drawable>(d);
        }
        return d;
    }

}
