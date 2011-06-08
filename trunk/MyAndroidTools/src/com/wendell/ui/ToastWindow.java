package com.wendell.ui;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ToastWindow extends PopupWindow{
	
	private TextView text = null;
	
	public ToastWindow(Context context){
		super(context);
		text = new TextView(context);
		setContentView(text);
		setBackgroundDrawable(context.getResources().getDrawable(context.getResources().getIdentifier("bitmap_generic_toast_bg", "drawable", context.getPackageName())));
		setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
	}
	
	public ToastWindow(Context context,int textResId){
		this(context);
		text.setText(textResId);
	}
	
	public ToastWindow(Context context,CharSequence textStr) {
		this(context);
		text.setText(textStr);
	}
	
	public void setText(int textResId){
		text.setText(textResId);
	}
	
	public void setText(CharSequence textStr){
		text.setText(textStr);
	}
	
	public void show(Window window,int distanceToBottom){
		showAtLocation(window.getDecorView(), Gravity.BOTTOM, 0, distanceToBottom);
	}
	
	public void showCenter(Window window){
		showAtLocation(window.getDecorView(),Gravity.CENTER,0,0);
	}
	
	public void dismissDelayed(int delayMillis){
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					ToastWindow.this.dismiss();
				}catch(RuntimeException e){   //如果依附的window已关闭，则调用dismiss会导致异常，这里原则上不应该向外抛出
					Log.e("ToastWindow", "dismiss ToastWindow failed.", e);
				}
			}
		}, delayMillis);
	}
	
	public void dismissDelayed(){
		dismissDelayed(2800);
	}
	
}
