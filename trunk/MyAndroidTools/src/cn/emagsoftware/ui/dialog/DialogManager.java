package cn.emagsoftware.ui.dialog;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.View;

/**
 * <p>对话框管理类
 * @author Wendell
 * @version 1.5
 * 
 */
public abstract class DialogManager {
	
	private static boolean useSystemDialogTheme = false;
	
	/**
	 * <p>默认情况下，弹出的对话框除了标题和底部按钮，主体部分将会应用传入的Context中的主题。
	 *    如果要使弹出的对话框使用系统样式，可调用该方法并传入一个true值。
	 * <p>当前方法的设置只对通过当前类的方法弹出的对话框有效。
	 * <p>如果是通过传入一个View来显示对话框的话，则当前方法的设置将不起作用。若要显示系统样式，
	 *    可在创建View之前将Context通过convertToSystemDialogTheme方法进行转化后再传入View的构造函数。
	 *    事实上，当前类的内部也是通过convertToSystemDialogTheme方法实现的。
	 * <p>另：由于默认情况下对话框的标题和底部按钮不使用传入的Context中的主题，所以若要使对话框完全应用
	 *    Context中的主题，可使用ThemeAlertDialog，即当前类中的showThemeAlertDialog方法。
	 * @param use
	 */
	public static void setUseSystemDialogTheme(boolean use){
		useSystemDialogTheme = use;
	}
	
	public static Context convertToSystemDialogTheme(Context context){
		return new ContextThemeWrapper(context,android.R.style.Theme_Dialog);
	}
	
	public static AlertDialog.Builder createAlertDialogBuilder(Context context,String title,String[] buttons,OnClickListener onClickListener,boolean cancelable){
		if(useSystemDialogTheme) context = convertToSystemDialogTheme(context);
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
		String title = null;
		if(titleId != -1) title = context.getString(titleId);
		String[] buttons = null;
		if(buttonIds != null){
			buttons = new String[buttonIds.length];
			for(int i = 0;i < buttons.length;i++){
				buttons[i] = context.getString(buttonIds[i]);
			}
		}
		return createAlertDialogBuilder(context,title,buttons,onClickListener,cancelable);
	}
	
	public static AlertDialog showAlertDialog(Context context,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		AlertDialog.Builder ab = createAlertDialogBuilder(context,title,buttons,onClickListener,cancelable);
		if(msg != null) ab.setMessage(msg);
		if(isNotAutoDismiss) return setNotAutoDismiss(ab.show());
		else return ab.show();
	}
	
	public static AlertDialog showAlertDialog(Context context,int titleId,int msgId,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String title = null;
		if(titleId != -1) title = context.getString(titleId);
		String msg = null;
		if(msgId != -1) msg = context.getString(msgId);
		String[] buttons = null;
		if(buttonIds != null){
			buttons = new String[buttonIds.length];
			for(int i = 0;i < buttons.length;i++){
				buttons[i] = context.getString(buttonIds[i]);
			}
		}
		return showAlertDialog(context,title,msg,buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static AlertDialog showAlertDialog(Context context,String title,View view,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		AlertDialog.Builder ab = createAlertDialogBuilder(context,title,buttons,onClickListener,cancelable);
		if(view != null) ab.setView(view);
		if(isNotAutoDismiss) return setNotAutoDismiss(ab.show());
		else return ab.show();
	}
	
	public static AlertDialog showAlertDialog(Context context,int titleId,View view,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String title = null;
		if(titleId != -1) title = context.getString(titleId);
		String[] buttons = null;
		if(buttonIds != null){
			buttons = new String[buttonIds.length];
			for(int i = 0;i < buttons.length;i++){
				buttons[i] = context.getString(buttonIds[i]);
			}
		}
		return showAlertDialog(context,title,view,buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ProgressDialog showProgressDialog(Context context,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		return showProgressDialog(context,-1,title,msg,buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ProgressDialog showProgressDialog(Context context,int titleId,int msgId,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String title = null;
		if(titleId != -1) title = context.getString(titleId);
		String msg = null;
		if(msgId != -1) msg = context.getString(msgId);
		String[] buttons = null;
		if(buttonIds != null){
			buttons = new String[buttonIds.length];
			for(int i = 0;i < buttons.length;i++){
				buttons[i] = context.getString(buttonIds[i]);
			}
		}
		return showProgressDialog(context,title,msg,buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ProgressDialog showProgressDialog(Context context,int theme,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		if(useSystemDialogTheme) context = convertToSystemDialogTheme(context);
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
		String title = null;
		if(titleId != -1) title = context.getString(titleId);
		String msg = null;
		if(msgId != -1) msg = context.getString(msgId);
		String[] buttons = null;
		if(buttonIds != null){
			buttons = new String[buttonIds.length];
			for(int i = 0;i < buttons.length;i++){
				buttons[i] = context.getString(buttonIds[i]);
			}
		}
		return showProgressDialog(context,theme,title,msg,buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ThemeAlertDialog showThemeAlertDialog(Context context,int theme,String title,String msg,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		if(useSystemDialogTheme) context = convertToSystemDialogTheme(context);
		ThemeAlertDialog tad = null;
		if(theme == -1) tad = new ThemeAlertDialog(context);
		else tad = new ThemeAlertDialog(context, theme);
		if(title != null) tad.setTitle(title);
		if(msg != null) tad.setMessage(msg);
		if(buttons != null){
			if(buttons.length >= 1) tad.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
			if(buttons.length >= 2) tad.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[1], onClickListener);
			if(buttons.length >= 3) tad.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[2], onClickListener);
		}
		tad.setCancelable(cancelable);
		tad.show();
		if(isNotAutoDismiss) return (ThemeAlertDialog)setNotAutoDismiss(tad);
		else return tad;
	}
	
	public static ThemeAlertDialog showThemeAlertDialog(Context context,int theme,int titleId,int msgId,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String title = null;
		if(titleId != -1) title = context.getString(titleId);
		String msg = null;
		if(msgId != -1) msg = context.getString(msgId);
		String[] buttons = null;
		if(buttonIds != null){
			buttons = new String[buttonIds.length];
			for(int i = 0;i < buttons.length;i++){
				buttons[i] = context.getString(buttonIds[i]);
			}
		}
		return showThemeAlertDialog(context,theme,title,msg,buttons,onClickListener,cancelable,isNotAutoDismiss);
	}
	
	public static ThemeAlertDialog showThemeAlertDialog(Context context,int theme,String title,View view,String[] buttons,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		if(useSystemDialogTheme) context = convertToSystemDialogTheme(context);
		ThemeAlertDialog tad = null;
		if(theme == -1) tad = new ThemeAlertDialog(context);
		else tad = new ThemeAlertDialog(context, theme);
		if(title != null) tad.setTitle(title);
		if(view != null) tad.setView(view);
		if(buttons != null){
			if(buttons.length >= 1) tad.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
			if(buttons.length >= 2) tad.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[1], onClickListener);
			if(buttons.length >= 3) tad.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[2], onClickListener);
		}
		tad.setCancelable(cancelable);
		tad.show();
		if(isNotAutoDismiss) return (ThemeAlertDialog)setNotAutoDismiss(tad);
		else return tad;
	}
	
	public static ThemeAlertDialog showThemeAlertDialog(Context context,int theme,int titleId,View view,int[] buttonIds,OnClickListener onClickListener,boolean cancelable,boolean isNotAutoDismiss){
		String title = null;
		if(titleId != -1) title = context.getString(titleId);
		String[] buttons = null;
		if(buttonIds != null){
			buttons = new String[buttonIds.length];
			for(int i = 0;i < buttons.length;i++){
				buttons[i] = context.getString(buttonIds[i]);
			}
		}
		return showThemeAlertDialog(context,theme,title,view,buttons,onClickListener,cancelable,isNotAutoDismiss);
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
	        field.set(obj, new Handler(Looper.getMainLooper()){
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
