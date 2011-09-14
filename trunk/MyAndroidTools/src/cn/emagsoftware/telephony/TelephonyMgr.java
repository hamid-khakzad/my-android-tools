package cn.emagsoftware.telephony;

import android.content.Context;
import android.os.Environment;
import android.telephony.TelephonyManager;

public final class TelephonyMgr {
	
	private TelephonyMgr(){}
	
	public static int getSimState(Context context){
		TelephonyManager tm = (TelephonyManager)context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
		return tm.getSimState();
	}
	
	public static boolean checkSimCard(Context context){
		return getSimState(context) == TelephonyManager.SIM_STATE_READY;
	}
	
	public static String getSubscriberId(Context context){
		TelephonyManager tm = (TelephonyManager)context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId();
	}
	
	public static String getSdCardState(){
		return Environment.getExternalStorageState();
	}
	
	public static boolean checkSdCard(){
		return getSdCardState().equals(Environment.MEDIA_MOUNTED);
	}
	
}
