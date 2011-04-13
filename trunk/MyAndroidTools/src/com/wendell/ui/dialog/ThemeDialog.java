package com.wendell.ui.dialog;

import java.util.HashMap;
import java.util.Map;

import com.wendell.ui.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ThemeDialog extends Dialog implements android.view.View.OnClickListener{
	
	private Context context = null;
	
	private ImageView icon = null;
	private TextView title = null;
	private LinearLayout content = null;
	private TextView message = null;
	private LinearLayout buttonsLayout = null;
	private Button buttonPositive = null;
	private Button buttonNeutral = null;
	private Button buttonNegative = null;
	
	private Map<Button,OnClickListener> buttonsMapping = new HashMap<Button, OnClickListener>();
	
	public ThemeDialog(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
		init(context);
	}
	
	public ThemeDialog(Context context,int theme){
		super(context,theme);
		init(context);
	}
	
	protected void init(Context context){
		this.context = context;
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));		
		setContentView(R.layout.themedialog);
		icon = (ImageView)findViewById(R.id.ImageView01);
		title = (TextView)findViewById(R.id.TextView01);
		content = (LinearLayout)findViewById(R.id.LinearLayout04);
		message = (TextView)findViewById(R.id.TextView02);
		buttonsLayout = (LinearLayout)findViewById(R.id.LinearLayout05);
		buttonPositive = (Button)findViewById(R.id.Button01);
		buttonNeutral = (Button)findViewById(R.id.Button02);
		buttonNegative = (Button)findViewById(R.id.Button03);
		buttonPositive.setOnClickListener(this);
		buttonNeutral.setOnClickListener(this);
		buttonNegative.setOnClickListener(this);
	}
	
	public void setIcon(int iconResourceId){
		icon.setImageResource(iconResourceId);
	}
	
	public void setIcon(Drawable drawable){
		icon.setImageDrawable(drawable);
	}
	
	@Override
	public void setTitle(CharSequence titleStr) {
		// TODO Auto-generated method stub
		title.setText(titleStr);
	}
	
	@Override
	public void setTitle(int titleId) {
		// TODO Auto-generated method stub
		title.setText(titleId);
	}
	
	public void setMessage(CharSequence messageStr) {
		message.setText(messageStr);
	}
	
	public void setMessage(int messageId) {
		message.setText(messageId);
	}
	
	public void setView(View view){
		content.removeAllViews();
		content.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT));
	}
	
	public void setButton(int whichButton,CharSequence textStr,OnClickListener listener){
		Button button = null;
		if(whichButton == DialogInterface.BUTTON_POSITIVE) button = buttonPositive;
		else if(whichButton == DialogInterface.BUTTON_NEUTRAL) button = buttonNeutral;
		else if(whichButton == DialogInterface.BUTTON_NEGATIVE) button = buttonNegative;
		else throw new IllegalArgumentException();
		
		button.setVisibility(View.VISIBLE);
		button.setText(textStr);
		
		buttonsMapping.put(button, listener);
	}
	
	public void setButton(int whichButton,int textResourceId,OnClickListener listener){
		setButton(whichButton,context.getString(textResourceId),listener);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		OnClickListener listener = buttonsMapping.get(v);
		if(listener == null){
			this.dismiss();
		}else{
			if(v == buttonPositive) listener.onClick(this, DialogInterface.BUTTON_POSITIVE);
			else if(v == buttonNeutral) listener.onClick(this, DialogInterface.BUTTON_NEUTRAL);
			else if(v == buttonNegative) listener.onClick(this, DialogInterface.BUTTON_NEGATIVE);
		}
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		int visibleCount = 0;
		if(buttonPositive.getVisibility() == View.VISIBLE) visibleCount = visibleCount + 1;
		if(buttonNeutral.getVisibility() == View.VISIBLE) visibleCount = visibleCount + 1;
		if(buttonNegative.getVisibility() == View.VISIBLE) visibleCount = visibleCount + 1;
		if(visibleCount == 0){
			buttonsLayout.setPadding(0,0,0,0);
		}else if(visibleCount == 1){
			buttonsLayout.setPadding(50, 0, 50, 4);
		}else{
			buttonsLayout.setPadding(0, 0, 0, 4);
		}
		super.show();
	}
	
}
