package cn.emagsoftware.memory;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public final class MemoryManager {
	
	private MemoryManager(){}
	
	public static void recycleBitmaps(Drawable drawable){
		List<Bitmap> bitmaps = ViewBitmapSelector.drawableToBitmaps(drawable, false);
		for(Bitmap b : bitmaps){
			b.recycle();
		}
	}
	
	public static void recycleBitmaps(View view,ViewBitmapSelector selector,boolean recursive){
		List<Bitmap> bitmaps = selector.select(view, false);
		for(Bitmap b : bitmaps){
			b.recycle();
		}
		if(recursive && view instanceof ViewGroup){
			ViewGroup viewGroup = (ViewGroup)view;
			for(int i = 0;i < viewGroup.getChildCount();i++){
				View child = viewGroup.getChildAt(i);
				recycleBitmaps(child,selector,recursive);
			}
		}
	}
	
	public static boolean isLowMemory(Context context){
        ActivityManager actMgr = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        actMgr.getMemoryInfo(memoryInfo);
        Log.i("MemoryManager", "Avail Mem=" + (memoryInfo.availMem >> 20) + "M");
        Log.i("MemoryManager", "Threshold=" + (memoryInfo.threshold >> 20) + "M");
        Log.i("MemoryManager", "Is Low Mem=" + memoryInfo.lowMemory);
        return memoryInfo.lowMemory;
	}
	
}
