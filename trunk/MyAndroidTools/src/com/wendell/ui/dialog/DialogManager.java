package com.wendell.ui.dialog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

public abstract class DialogManager {
	
	public static AlertDialog.Builder createAlertDialogBuilder(Context context,String title,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		if(title != null) ab.setTitle(title);
		if(buttons != null){
			if(buttons.length >= 1) ab.setPositiveButton(buttons[0], onClickListener);
			if(buttons.length >= 2) ab.setNegativeButton(buttons[1], onClickListener);
			if(buttons.length >= 3) ab.setNeutralButton(buttons[2], onClickListener);
		}
		ab.setCancelable(cancelable);
		return ab;
	}
	
	public static AlertDialog showMessageDialog(Context context,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		AlertDialog.Builder ab = createAlertDialogBuilder(context,title,buttons,onClickListener,cancelable);
		if(msg != null) ab.setMessage(msg);
		return ab.show();
	}
	
	public static ProgressDialog showProgressDialog(Context context,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		return showProgressThemeDialog(context,-1,title,msg,buttons,onClickListener,cancelable);
	}
	
	public static AlertDialog showCustomDialog(Context context,String title,View view,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		AlertDialog.Builder ab = createAlertDialogBuilder(context,title,buttons,onClickListener,cancelable);
		if(view != null) ab.setView(view);
		return ab.show();
	}
	
	public static ThemeDialog showMessageThemeDialog(Context context,int theme,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		ThemeDialog td = null;
		if(theme == -1) td = new ThemeDialog(context);
		else td = new ThemeDialog(context, theme);
		if(title != null) td.setTitle(title);
		if(msg != null) td.setMessage(msg);
		if(buttons != null){
			if(buttons.length >= 1) td.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
			if(buttons.length >= 2) td.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[1], onClickListener);
			if(buttons.length >= 3) td.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[2], onClickListener);
		}
		td.setCancelable(cancelable);
		td.show();
		return td;
	}
	
	public static ProgressDialog showProgressThemeDialog(Context context,int theme,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		ProgressDialog pd = null;
		if(theme == -1) pd = new ProgressDialog(context);
		else pd = new ProgressDialog(context, theme);
		if(title != null) pd.setTitle(title);
		if(msg != null) pd.setMessage(msg);
		if(buttons != null){
			if(buttons.length >= 1) pd.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
			if(buttons.length >= 2) pd.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[1], onClickListener);
			if(buttons.length >= 3) pd.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[2], onClickListener);
		}
		pd.setCancelable(cancelable);
		pd.show();
		return pd;
	}
	
	public static ThemeDialog showCustomThemeDialog(Context context,int theme,String title,View view,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		ThemeDialog td = null;
		if(theme == -1) td = new ThemeDialog(context);
		else td = new ThemeDialog(context, theme);
		if(title != null) td.setTitle(title);
		if(view != null) td.setView(view);
		if(buttons != null){
			if(buttons.length >= 1) td.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
			if(buttons.length >= 2) td.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[1], onClickListener);
			if(buttons.length >= 3) td.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[2], onClickListener);
		}
		td.setCancelable(cancelable);
		td.show();
		return td;
	}
	
}
