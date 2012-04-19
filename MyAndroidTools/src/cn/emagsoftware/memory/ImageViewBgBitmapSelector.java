package cn.emagsoftware.memory;

import java.util.LinkedList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class ImageViewBgBitmapSelector extends AbstractBitmapSelector
{

    public ImageViewBgBitmapSelector()
    {
        super();
    }

    public ImageViewBgBitmapSelector(Resources res, int[] drawableIdsToExclude)
    {
        super(res, drawableIdsToExclude);
    }

    @Override
    protected List<Drawable> onSelect(View view)
    {
        // TODO Auto-generated method stub
        List<Drawable> drawables = new LinkedList<Drawable>();
        if (view instanceof ImageView)
        {
            Drawable d = view.getBackground();
            if (d != null)
                drawables.add(d);
        }
        return drawables;
    }

}
