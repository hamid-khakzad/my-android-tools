package cn.emagsoftware.memory;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import cn.emagsoftware.util.LogManager;

public final class MemoryManager
{

    private MemoryManager()
    {
    }

    /**
     * <p>获取仅仅用来计算Bitmap大小的BitmapFactory.Options</>
     * @return
     */
    public static BitmapFactory.Options createJustDecodeBoundsOptions(){
        BitmapFactory.Options returnOptions = new BitmapFactory.Options();
        returnOptions.inJustDecodeBounds = true;
        return returnOptions;
    }

    /**
     * <p>获取根据maxWidth和maxHeight计算出inSampleSize的BitmapFactory.Options</>
     * @param justBoundsOptions
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static BitmapFactory.Options createSampleSizeOptions(BitmapFactory.Options justBoundsOptions, int maxWidth, int maxHeight)
    {
        int srcWidth = justBoundsOptions.outWidth;
        int srcHeight = justBoundsOptions.outHeight;
        int widthScale = (int) Math.ceil((float) srcWidth / maxWidth);
        int heightScale = (int) Math.ceil((float) srcHeight / maxHeight);
        int inSampleSize = Math.max(widthScale, heightScale);
        return createSampleSizeOptions(inSampleSize);
    }

    /**
     * <p>获取指定了具体缩放值的BitmapFactory.Options
     * 
     * @param inSampleSize 缩放成1/inSampleSize
     * @return
     */
    public static BitmapFactory.Options createSampleSizeOptions(int inSampleSize)
    {
        BitmapFactory.Options returnOptions = new BitmapFactory.Options();
        returnOptions.inDither = false;
        returnOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        returnOptions.inSampleSize = inSampleSize;
        return returnOptions;
    }

    public static boolean isLowMemory(Context context)
    {
        ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        actMgr.getMemoryInfo(memoryInfo);
        LogManager.logI(MemoryManager.class, "Avail Mem=" + (memoryInfo.availMem >> 20) + "M");
        LogManager.logI(MemoryManager.class, "Threshold=" + (memoryInfo.threshold >> 20) + "M");
        LogManager.logI(MemoryManager.class, "Is Low Mem=" + memoryInfo.lowMemory);
        return memoryInfo.lowMemory;
    }

}
