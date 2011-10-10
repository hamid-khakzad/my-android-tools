package cn.emagsoftware.memory;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public abstract class ViewBitmapSelector {
	
	public static List<Bitmap> drawableToBitmaps(Drawable drawable,boolean includeRecycled){
		List<Bitmap> bitmaps = new LinkedList<Bitmap>();
		if(drawable instanceof BitmapDrawable){
			Bitmap b = ((BitmapDrawable)drawable).getBitmap();
			if(b != null){
				if(includeRecycled) bitmaps.add(b);
				else if(!b.isRecycled()) bitmaps.add(b);
			}
		}
		return bitmaps;
	}
	
	public abstract List<Drawable> onSelect(View view);
	
	public List<Bitmap> select(View view,boolean includeRecycled){
		List<Bitmap> bitmaps = new LinkedList<Bitmap>();
		List<Drawable> drawables = onSelect(view);
		for(Drawable d : drawables){
			bitmaps.addAll(drawableToBitmaps(d,includeRecycled));
		}
		return bitmaps;
	}
	
}
