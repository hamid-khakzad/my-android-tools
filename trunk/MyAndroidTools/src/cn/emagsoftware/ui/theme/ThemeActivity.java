package cn.emagsoftware.ui.theme;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public abstract class ThemeActivity extends Activity {
	
	protected int curContentViewResID = View.NO_ID;
	protected View curContentView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ThemeEngine.addThemeActivity(this);
		getLayoutInflater().setFactory(ThemeFactory.createOrUpdateInstance(this, ThemeEngine.CUR_PACKAGENAME, ThemeEngine.CUR_THEMENAME));
	}
	
	@Override
	public void setContentView(int layoutResID) {
		// TODO Auto-generated method stub
		View newView = getLayoutInflater().inflate(layoutResID, null);
		super.setContentView(newView);
		curContentViewResID = layoutResID;
		View prevContentView = curContentView;
		curContentView = newView;
		onInit(prevContentView);
	}
	
	@Override
	public void setContentView(View view) {
		// TODO Auto-generated method stub
		super.setContentView(view);
		curContentViewResID = View.NO_ID;
		View prevContentView = curContentView;
		curContentView = view;
		onInit(prevContentView);
	}
	
	@Override
	public void setContentView(View view, LayoutParams params) {
		// TODO Auto-generated method stub
		super.setContentView(view, params);
		curContentViewResID = View.NO_ID;
		View prevContentView = curContentView;
		curContentView = view;
		onInit(prevContentView);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ThemeEngine.removeThemeActivity(this);
	}
	
	protected boolean resetUI(){
		if(curContentViewResID != View.NO_ID){
			View newContentView = getLayoutInflater().inflate(curContentViewResID, null);
			super.setContentView(newContentView);
			curContentView = newContentView;
			return true;
		}
		return false;
	}
	
	/**
	 * <p>��UI�ĳ�ʼ��������ͳһ�ڸ÷����н��У���Ϊ�ı����⽫���ع����棬��ʱ���Զ��ص��÷������³�ʼ��
	 * @param prevContentView ��һ��ContentView����һ�γ�ʼ��ʱ������null������ò�����Ϊ�˷���״̬����
	 */
	protected abstract void onInit(View prevContentView);
	
}
