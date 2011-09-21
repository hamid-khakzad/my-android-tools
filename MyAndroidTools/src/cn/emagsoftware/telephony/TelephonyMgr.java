package cn.emagsoftware.telephony;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;

public final class TelephonyMgr {
	
	private TelephonyMgr(){}
	
	public static int getSimState(Context context){
		TelephonyManager tm = (TelephonyManager)context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
		return tm.getSimState();
	}
	
	public static boolean isSimCardValid(Context context){
		return getSimState(context) == TelephonyManager.SIM_STATE_READY;
	}
	
	public static String getSubscriberId(Context context){
		TelephonyManager tm = (TelephonyManager)context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId();
	}
	
	public static String getSdCardState(){
		return Environment.getExternalStorageState();
	}
	
	public static long getSdCardSize(){
		String path = Environment.getExternalStorageDirectory().getPath();
		File file = new File(path);
		StatFs stat = new StatFs(file.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return blockSize*totalBlocks;
	}
	
	public static long getSdCardAvailableSize(){
		String path = Environment.getExternalStorageDirectory().getPath();
		File file = new File(path);
		StatFs stat = new StatFs(file.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return blockSize*(availableBlocks-4);
	}
	
	public static boolean isSdCardValid(){
		return getSdCardState().equals(Environment.MEDIA_MOUNTED);
	}
	
}
