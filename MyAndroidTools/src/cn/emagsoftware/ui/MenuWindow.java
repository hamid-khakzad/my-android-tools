package cn.emagsoftware.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class MenuWindow {
	
	private PopupWindow pw = null;
	
	public MenuWindow(Context context){
		pw = new PopupWindow(context);
		pw.setWidth(WindowManager.LayoutParams.FILL_PARENT);
		pw.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setAnimationStyle(context.getResources().getIdentifier("MenuWindow", "style", context.getPackageName()));
		setContentView(new LinearLayout(context));
		setBackgroundDrawable(context.getResources().getDrawable(context.getResources().getIdentifier("bitmap_menuwindow_bg", "drawable", context.getPackageName())));
	}
	
	public void setContentView(View contentView){
		pw.setContentView(contentView);
	}
	
	public void setBackgroundDrawable(Drawable background){
		pw.setBackgroundDrawable(background);
	}
	
	public void show(Window mainWindow,int distanceToBottom){
		pw.showAtLocation(mainWindow.getDecorView(), Gravity.BOTTOM, 0, distanceToBottom);
	}
	
	public void showAsDropDown(View anchor, int xoff, int yoff){
		pw.showAsDropDown(anchor, xoff, yoff);
	}
	
	public void setFocusable(boolean focusable){
		pw.setFocusable(focusable);
	}
	
	public void setAnimationStyle(int animationStyle){
		pw.setAnimationStyle(animationStyle);
	}
	
	public boolean isShowing(){
		return pw.isShowing();
	}
	
	public void dismiss(){
		pw.dismiss();
	}
	
	public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener){
		pw.setOnDismissListener(onDismissListener);
	}
	
}
