package cn.emagsoftware.memory;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class BitmapSelector {
	
	public BitmapSelector(){}
	
	public List<Bitmap> fromDrawable(Drawable drawable,boolean includeRecycled){
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
	
	public List<Bitmap> fromView(View view,boolean includeRecycled){
		List<Bitmap> bitmaps = new LinkedList<Bitmap>();
		if(view instanceof ImageView){
			Drawable d = ((ImageView)view).getDrawable();
			if(d != null) bitmaps.addAll(fromDrawable(d,includeRecycled));
		}
		Drawable d = view.getBackground();
		if(d != null) bitmaps.addAll(fromDrawable(d,includeRecycled));
		return bitmaps;
	}
	
	public List<Bitmap> fromViewRecursively(View view,boolean includeRecycled){
		List<Bitmap> bitmaps = fromView(view,includeRecycled);
		if(view instanceof ViewGroup){
			ViewGroup viewGroup = (ViewGroup)view;
			for(int i = 0;i < viewGroup.getChildCount();i++){
				View child = viewGroup.getChildAt(i);
				bitmaps.addAll(fromViewRecursively(child,includeRecycled));
			}
		}
		return bitmaps;
	}
	
}
