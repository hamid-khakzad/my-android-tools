package com.wendell.ui.theme;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public abstract class ThemeActivity extends Activity {
	
	protected int currContentViewResID = View.NO_ID;
	protected View currContentView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ThemeEngine.addThemeActivity(this);
		if(ThemeEngine.CURR_PACKAGENAME != null){
			changeTheme(ThemeEngine.CURR_PACKAGENAME,ThemeEngine.CURR_THEMENAME);
		}
	}
	
	@Override
	public void setContentView(int layoutResID) {
		// TODO Auto-generated method stub
		View newView = getLayoutInflater().inflate(layoutResID, null);
		super.setContentView(newView);
		currContentViewResID = layoutResID;
		View prevContentView = currContentView;
		currContentView = newView;
		onInit(prevContentView);
	}
	
	@Override
	public void setContentView(View view) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("ThemeActivity can only use 'setContentView(int layoutResID)' to set content view.");
	}
	
	@Override
	public void setContentView(View view, LayoutParams params) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("ThemeActivity can only use 'setContentView(int layoutResID)' to set content view.");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
		View prevContentView = currContentView;
		if(resetUI()) onInit(prevContentView);
	}
	
	protected boolean resetUI(){
		if(currContentViewResID != View.NO_ID){
			View newContentView = getLayoutInflater().inflate(currContentViewResID, null);
			super.setContentView(newContentView);
			currContentView = newContentView;
			return true;
		}
		return false;
	}
	
	/**
	 * <p>对UI的初始化操作须统一在该方法中进行，因为改变主题将会重构界面，此时会自动回调该方法重新初始化
	 * @param prevContentView 上一个ContentView，第一次初始化时将传入null，传入该参数是为了方便状态保存
	 */
	protected abstract void onInit(View prevContentView);
	
}
