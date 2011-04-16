package com.wendell.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public abstract class OrientationResetUIActivity extends Activity {
	
	protected int contentViewResID = View.NO_ID;
	
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
	public final void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if(contentViewResID != View.NO_ID){
			setContentView(contentViewResID);
			if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
				onUIResetWhenPortrait();
			}else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
				onUIResetWhenLandscape();
			}
		}
	}
	
	protected abstract void onUIResetWhenPortrait();
	
	protected abstract void onUIResetWhenLandscape();
	
}
