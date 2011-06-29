package cn.emagsoftware.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;

public class ThemeAlertDialog extends AlertDialog {
	
	public ThemeAlertDialog(Context context){
		super(context);
	}
	
	public ThemeAlertDialog(Context context,int theme) {
		super(context,theme);
	}
	
	public ThemeAlertDialog(Context context,boolean cancelable,OnCancelListener cancelListener){
		super(context,cancelable,cancelListener);
	}
	
}
