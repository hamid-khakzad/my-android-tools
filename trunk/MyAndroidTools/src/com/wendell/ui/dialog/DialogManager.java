package com.wendell.ui.dialog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.TextView;

public abstract class DialogManager {
	
	public static AlertDialog showMessageDialog(Context context,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		if(title != null) ab.setTitle(title);
		if(msg != null) ab.setMessage(msg);
		if(buttons != null){
			if(buttons.length >= 1) ab.setPositiveButton(buttons[0], onClickListener);
			if(buttons.length >= 2) ab.setNegativeButton(buttons[1], onClickListener);
			if(buttons.length >= 3) ab.setNeutralButton(buttons[2], onClickListener);
		}
		ab.setCancelable(cancelable);
		return ab.show();
	}
	
	public static AlertDialog showCustomDialog(Context context,String title,View view,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		if(title != null) ab.setTitle(title);
		if(view != null) ab.setView(view);
		if(buttons != null){
			if(buttons.length >= 1) ab.setPositiveButton(buttons[0], onClickListener);
			if(buttons.length >= 2) ab.setNegativeButton(buttons[1], onClickListener);
			if(buttons.length >= 3) ab.setNeutralButton(buttons[2], onClickListener);
		}
		ab.setCancelable(cancelable);
		return ab.show();
	}
	
	public static ProgressDialog showMessageThemeDialog(Context context,int theme,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		TextView tv = null;
		if(msg != null){
			tv = new TextView(context);
			tv.setText(msg);
		}
		return showCustomThemeDialog(context,theme,title,tv,buttons,onClickListener,cancelable);
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
	
	public static ProgressDialog showCustomThemeDialog(Context context,int theme,String title,View view,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		ProgressDialog pd = null;
		if(theme == -1) pd = new ProgressDialog(context);
		else pd = new ProgressDialog(context, theme);
		if(title != null) pd.setTitle(title);
		if(view != null) pd.setView(view);
		if(buttons != null){
			if(buttons.length >= 1) pd.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
			if(buttons.length >= 2) pd.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[1], onClickListener);
			if(buttons.length >= 3) pd.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[2], onClickListener);
		}
		pd.setCancelable(cancelable);
		pd.show();
		return pd;
	}
	
}
