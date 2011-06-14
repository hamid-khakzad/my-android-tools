package com.wendell.ui.theme;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public abstract class ThemeActivity extends Activity {
	
	protected int contentViewResID = View.NO_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ThemeEngine.addThemeActivity(this);
		onInit();
	}
	
	@Override
	public void setContentView(int layoutResID) {
		// TODO Auto-generated method stub
		super.setContentView(layoutResID);
		contentViewResID = layoutResID;
	}
	
	@Override
	public void setContentView(View view) {
		// TODO Auto-generated method stub
		super.setContentView(view);
		contentViewResID = view.getId();
	}
	
	@Override
	public void setContentView(View view, LayoutParams params) {
		// TODO Auto-generated method stub
		super.setContentView(view, params);
		contentViewResID = view.getId();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		ThemeEngine.removeThemeActivity(this);
	}
	
	/**
	 * <p>改变主题
	 * @param packageName 需要应用的主题包名
	 * @param themeName 主题包styles.xml中主题样式的名字，如果没有通用的主题样式，可传null
	 */
	public void changeTheme(String packageName,String themeName){
		ThemeFactory tf = (ThemeFactory)getLayoutInflater().getFactory();
		if(tf == null){
			getLayoutInflater().setFactory(ThemeFactory.getInstance(this, packageName, themeName));
		}else{
			tf.update(packageName, themeName);
		}
		resetUI();
		onInit();
	}
	
	protected void resetUI(){
		if(contentViewResID != View.NO_ID){
			setContentView(contentViewResID);
		}
	}
	
	/**
	 * <p>对UI的初始化操作须统一在该方法中进行，因为改变主题将会重构界面，此时会自动回调该方法重新初始化
	 */
	protected abstract void onInit();
	
}
