package com.wendell.ui;

import com.wendell.ui.theme.ThemeActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public abstract class OrientationResetUIActivity extends ThemeActivity {
	
	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		super.resetUI();
		if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			onInitWhenPortrait();
		}else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			onInitWhenLandscape();
		}
	}
	
	@Override
	protected void onInit() {
		// TODO Auto-generated method stub
		int orientation = getRequestedOrientation();
		if(orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
			onInitWhenLandscape();
		}else if(orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
			onInitWhenPortrait();
		}
	}
	
	protected abstract void onInitWhenPortrait();
	
	protected abstract void onInitWhenLandscape();
	
}
