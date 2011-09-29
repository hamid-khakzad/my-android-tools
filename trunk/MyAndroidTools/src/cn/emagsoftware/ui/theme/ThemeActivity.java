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
		onSetContentView(prevContentView);
	}
	
	@Override
	public void setContentView(View view) {
		// TODO Auto-generated method stub
		super.setContentView(view);
		curContentViewResID = View.NO_ID;
		View prevContentView = curContentView;
		curContentView = view;
		onSetContentView(prevContentView);
	}
	
	@Override
	public void setContentView(View view, LayoutParams params) {
		// TODO Auto-generated method stub
		super.setContentView(view, params);
		curContentViewResID = View.NO_ID;
		View prevContentView = curContentView;
		curContentView = view;
		onSetContentView(prevContentView);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ThemeEngine.removeThemeActivity(this);
	}
	
	protected boolean resetContentView(){
		if(curContentViewResID != View.NO_ID){
			View newContentView = getLayoutInflater().inflate(curContentViewResID, null);
			super.setContentView(newContentView);
			View prevContentView = curContentView;
			curContentView = newContentView;
			onSetContentView(prevContentView);
			return true;
		}
		return false;
	}
	
	/**
	 * <p>setContentView之后的初始化操作须统一在该方法中进行，因为改变主题将重新setContentView，此时会回调该方法重新初始化
	 * @param prevContentView 上一个ContentView，第一次初始化时将传入null，传入该参数是为了方便状态保存
	 */
	protected abstract void onSetContentView(View prevContentView);
	
}
