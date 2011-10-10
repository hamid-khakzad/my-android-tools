package cn.emagsoftware.memory;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

public final class MemoryManager {
	
	private MemoryManager(){}
	
	public static void recycleBitmaps(Drawable drawable){
		List<Bitmap> bitmaps = new BitmapSelector().fromDrawable(drawable, false);
		for(Bitmap b : bitmaps){
			b.recycle();
		}
	}
	
	public static void recycleBitmaps(View view){
		List<Bitmap> bitmaps = new BitmapSelector().fromView(view, false);
		for(Bitmap b : bitmaps){
			b.recycle();
		}
	}
	
	public static void recycleBitmapsRecursively(View view){
		List<Bitmap> bitmaps = new BitmapSelector().fromViewRecursively(view, false);
		for(Bitmap b : bitmaps){
			b.recycle();
		}
	}
	
}
