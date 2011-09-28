package cn.emagsoftware.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;

/**
 * <p>可应用指定主题的对话框类。该类通过继承AlertDialog并暴露其受保护的构造函数来实现
 * <p>尽管ProgressDialog类也存在传入主题样式的构造函数，但其setView无效，故而创建当前类是必要的
 * @author Wendell
 * @version 1.0
 *
 */
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
