package com.wendell.ui.dialog;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.view.View;

/**
 * <p>dialog manager
 * <p>though the 'setView' method for thematic ProgressDialog has existed,but it dose not work.The ThemeDialog class is created by this reson.
 * @author Wendell
 * @version 1.2
 * 
 */
public abstract class DialogManager {
	
	public static AlertDialog.Builder createAlertDialogBuilder(Context context,String title,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		if(title != null) ab.setTitle(title);
		if(buttons != null){
			if(buttons.length >= 1) ab.setPositiveButton(buttons[0], onClickListener);
			if(buttons.length >= 2) ab.setNeutralButton(buttons[1], onClickListener);
			if(buttons.length >= 3) ab.setNegativeButton(buttons[2], onClickListener);
		}
		ab.setCancelable(cancelable);
		return ab;
	}
	
	public static AlertDialog.Builder createAlertDialogBuilder(Context context,int titleId,int[] buttonIds,OnClickListener onClickListener,boolean cancelable){
		String[] buttons = new String[buttonIds.length];
		for(int i = 0;i < buttons.length;i++){
			buttons[i] = context.getString(buttonIds[i]);
		}
		return createAlertDialogBuilder(context,context.getString(titleId),buttons,onClickListener,cancelable);
	}
	
	public static AlertDialog showAlertDialog(Context context,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		AlertDialog.Builder ab = createAlertDialogBuilder(context,title,buttons,onClickListener,cancelable);
		if(msg != null) ab.setMessage(msg);
		if(isNotAutoDismiss) return setNotAutoDismiss(ab.show());
		else return ab.show();
	}
	
	public static AlertDialog showAlertDialog(Context context,int titleId,int msgId,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String[] buttons = new String[buttonIds.length];
		for(int i = 0;i < buttons.length;i++){
			buttons[i] = context.getString(buttonIds[i]);
		}
		return showAlertDialog(context,context.getString(titleId),context.getString(msgId),buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static AlertDialog showAlertDialog(Context context,String title,View view,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		AlertDialog.Builder ab = createAlertDialogBuilder(context,title,buttons,onClickListener,cancelable);
		if(view != null) ab.setView(view);
		if(isNotAutoDismiss) return setNotAutoDismiss(ab.show());
		else return ab.show();
	}
	
	public static AlertDialog showAlertDialog(Context context,int titleId,View view,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String[] buttons = new String[buttonIds.length];
		for(int i = 0;i < buttons.length;i++){
			buttons[i] = context.getString(buttonIds[i]);
		}
		return showAlertDialog(context,context.getString(titleId),view,buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ProgressDialog showProgressDialog(Context context,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		return showProgressDialog(context,-1,title,msg,buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ProgressDialog showProgressDialog(Context context,int titleId,int msgId,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String[] buttons = new String[buttonIds.length];
		for(int i = 0;i < buttons.length;i++){
			buttons[i] = context.getString(buttonIds[i]);
		}
		return showProgressDialog(context,context.getString(titleId),context.getString(msgId),buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ProgressDialog showProgressDialog(Context context,int theme,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		ProgressDialog pd = null;
		if(theme == -1) pd = new ProgressDialog(context);
		else pd = new ProgressDialog(context, theme);
		if(title != null) pd.setTitle(title);
		if(msg != null) pd.setMessage(msg);
		if(buttons != null){
			if(buttons.length >= 1) pd.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
			if(buttons.length >= 2) pd.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[1], onClickListener);
			if(buttons.length >= 3) pd.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[2], onClickListener);
		}
		pd.setCancelable(cancelable);
		pd.show();
		if(isNotAutoDismiss) return (ProgressDialog)setNotAutoDismiss(pd);
		else return pd;
	}
	
	public static ProgressDialog showProgressDialog(Context context,int theme,int titleId,int msgId,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String[] buttons = new String[buttonIds.length];
		for(int i = 0;i < buttons.length;i++){
			buttons[i] = context.getString(buttonIds[i]);
		}
		return showProgressDialog(context,theme,context.getString(titleId),context.getString(msgId),buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ThemeDialog showThemeDialog(Context context,int theme,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		ThemeDialog td = null;
		if(theme == -1) td = new ThemeDialog(context);
		else td = new ThemeDialog(context, theme);
		if(title != null) td.setTitle(title);
		if(msg != null) td.setMessage(msg);
		if(buttons != null){
			if(buttons.length >= 1) td.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
			if(buttons.length >= 2) td.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[1], onClickListener);
			if(buttons.length >= 3) td.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[2], onClickListener);
		}
		td.setCancelable(cancelable);
		td.show();
		return td.setNotAutoDismiss(isNotAutoDismiss);
	}
	
	public static ThemeDialog showThemeDialog(Context context,int theme,int titleId,int msgId,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String[] buttons = new String[buttonIds.length];
		for(int i = 0;i < buttons.length;i++){
			buttons[i] = context.getString(buttonIds[i]);
		}
		return showThemeDialog(context,theme,context.getString(titleId),context.getString(msgId),buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ThemeDialog showThemeDialog(Context context,int theme,String title,View view,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		ThemeDialog td = null;
		if(theme == -1) td = new ThemeDialog(context);
		else td = new ThemeDialog(context, theme);
		if(title != null) td.setTitle(title);
		if(view != null) td.setView(view);
		if(buttons != null){
			if(buttons.length >= 1) td.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
			if(buttons.length >= 2) td.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[1], onClickListener);
			if(buttons.length >= 3) td.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[2], onClickListener);
		}
		td.setCancelable(cancelable);
		td.show();
		return td.setNotAutoDismiss(isNotAutoDismiss);
	}
	
	public static ThemeDialog showThemeDialog(Context context,int theme,int titleId,View view,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String[] buttons = new String[buttonIds.length];
		for(int i = 0;i < buttons.length;i++){
			buttons[i] = context.getString(buttonIds[i]);
		}
		return showThemeDialog(context,theme,context.getString(titleId),view,buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static AlertDialog setNotAutoDismiss(final AlertDialog dialog){
		try{
			Field field = dialog.getClass().getDeclaredField("mAlert"); 
	        field.setAccessible(true); 
	        //retrieve mAlert value 
	        Object obj = field.get(dialog); 
	        field = obj.getClass().getDeclaredField("mHandler");
	        field.setAccessible(true); 
	        //replace mHandler with our own handler 
	        field.set(obj, new Handler(){
	        	@Override
	        	public void handleMessage(Message msg) {
	        		// TODO Auto-generated method stub
	                switch (msg.what) {
	                    case DialogInterface.BUTTON_POSITIVE:
	                    case DialogInterface.BUTTON_NEUTRAL:
	                    case DialogInterface.BUTTON_NEGATIVE:
	                    	((DialogInterface.OnClickListener)msg.obj).onClick(dialog, msg.what);
	                    	break;
	                }
	            }
	        });
	        return dialog;
		}catch(NoSuchFieldException e){
			throw new RuntimeException(e);
		}catch(IllegalAccessException e){
			throw new RuntimeException(e);
		}
	}
	
}
