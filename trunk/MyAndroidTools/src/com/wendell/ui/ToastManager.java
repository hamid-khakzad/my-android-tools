package com.wendell.ui;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.widget.Toast;

public abstract class ToastManager {
	
	protected static Timer timerForEver = new Timer();
	protected static TimerTask taskForEver = null;
	protected static Toast toastForEver = null;
	
	public static Toast showShort(Context context,String text){
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
		return toast;
	}
	
	public static Toast showShort(Context context,int textResId){
		return showShort(context,context.getString(textResId));
	}
	
	public static Toast showLong(Context context,String text){
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		toast.show();
		return toast;
	}
	
	public static Toast showLong(Context context,int textResId){
		return showLong(context,context.getString(textResId));
	}
	
	public static void showForEver(Context context,String text){
		if(taskForEver != null) {
			toastForEver.setText(text);
			return;
		}
		toastForEver = Toast.makeText(context, text, Toast.LENGTH_LONG);		
		taskForEver = new TimerTask(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				toastForEver.show();
			}
		};
		timerForEver.schedule(taskForEver, new Date(), 2000);
	}
	
	public static void showForEver(Context context,int textResId){
		showForEver(context,context.getString(textResId));
	}
	
	public static void updateForEver(String text){
		if(toastForEver != null) toastForEver.setText(text);
	}
	
	public static void updateForEver(Context context,int textResId){
		updateForEver(context.getString(textResId));
	}
	
	public static void interruptForEver(){
		if(taskForEver != null){
			taskForEver.cancel();
			timerForEver.purge();
			taskForEver = null;
		}
	}
	
}
