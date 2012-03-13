package cn.emagsoftware.memory;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import cn.emagsoftware.util.LogManager;

public final class MemoryManager {
	
	private MemoryManager(){}
	
	/**
	 * <p>获取指定了尺寸限制的BitmapFactory.Options
	 * @param options 能获得outWidth和outHeight的BitmapFactory.Options，
	 *                通常的做法是decode参数inJustDecodeBounds为true的BitmapFactory.Options来获得此参数，此时只包含了Bitmap的尺寸信息，可节约内存
	 * @param minSideLength 最小的宽度或高度，忽略此项限制可传-1
	 * @param maxNumOfPixels 最大的宽度*高度，忽略此项限制可传-1
	 * @return
	 */
	public static BitmapFactory.Options getBitmapFactoryOptions(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels){
		BitmapFactory.Options returnOptions = new BitmapFactory.Options();
		returnOptions.inDither = false;
		returnOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		returnOptions.inSampleSize = computeSampleSize(options,minSideLength,maxNumOfPixels);
		return returnOptions;
	}
	
	/**
	 * <p>获取BitmapFactory.Options使用到的inSampleSize值
	 * @param options
	 * @param minSideLength 最小的宽度或高度，忽略此项限制可传-1
	 * @param maxNumOfPixels 最大的宽度*高度，忽略此项限制可传-1
	 * @return
	 */
	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}
	
	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
				.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
	
	public static void recycleBitmaps(Drawable drawable){
		List<Bitmap> bitmaps = AbstractBitmapSelector.drawableToBitmaps(drawable, false);
		for(Bitmap b : bitmaps){
			b.recycle();
		}
	}
	
	public static void recycleBitmaps(View view,AbstractBitmapSelector[] selectors,boolean recursive){
		for(AbstractBitmapSelector selector : selectors){
			List<Bitmap> bitmaps = selector.select(view, false);
			for(Bitmap b : bitmaps){
				b.recycle();
			}
		}
		if(recursive && view instanceof ViewGroup){
			ViewGroup viewGroup = (ViewGroup)view;
			for(int i = 0;i < viewGroup.getChildCount();i++){
				View child = viewGroup.getChildAt(i);
				recycleBitmaps(child,selectors,recursive);
			}
		}
	}
	
	public static boolean isLowMemory(Context context){
        ActivityManager actMgr = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        actMgr.getMemoryInfo(memoryInfo);
        LogManager.logI(MemoryManager.class, "Avail Mem=" + (memoryInfo.availMem >> 20) + "M");
        LogManager.logI(MemoryManager.class, "Threshold=" + (memoryInfo.threshold >> 20) + "M");
        LogManager.logI(MemoryManager.class, "Is Low Mem=" + memoryInfo.lowMemory);
        return memoryInfo.lowMemory;
	}
	
}
