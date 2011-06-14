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
	 * <p>�ı�����
	 * @param packageName ��ҪӦ�õ��������
	 * @param themeName �����styles.xml��������ʽ�����֣����û��ͨ�õ�������ʽ���ɴ�null
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
	 * <p>��UI�ĳ�ʼ��������ͳһ�ڸ÷����н��У���Ϊ�ı����⽫���ع����棬��ʱ���Զ��ص��÷������³�ʼ��
	 */
	protected abstract void onInit();
	
}
