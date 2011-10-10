package cn.emagsoftware.memory;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public final class MemoryManager {
	
	private MemoryManager(){}
	
	public static void recycleBitmaps(Drawable drawable){
		if(drawable instanceof BitmapDrawable){
			Bitmap b = ((BitmapDrawable)drawable).getBitmap();
			if(b != null && !b.isRecycled()) b.recycle();
		}
	}
	
	public static void recycleBitmaps(View view,boolean recursive){
		if(view instanceof ImageView){
			Drawable d = ((ImageView)view).getDrawable();
			if(d != null) recycleBitmaps(d);
		}
		Drawable d = view.getBackground();
		if(d != null) recycleBitmaps(d);
		if(recursive && view instanceof ViewGroup){
			ViewGroup viewGroup = (ViewGroup)view;
			for(int i = 0;i < viewGroup.getChildCount();i++){
				View child = viewGroup.getChildAt(i);
				recycleBitmaps(child,recursive);
			}
		}
	}
	
}
