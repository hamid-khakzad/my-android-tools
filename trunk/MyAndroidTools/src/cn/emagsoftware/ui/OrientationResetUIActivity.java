package cn.emagsoftware.ui;

import cn.emagsoftware.ui.theme.ThemeActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;

public abstract class OrientationResetUIActivity extends ThemeActivity {
	
	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		View prevContentView = super.curContentView;
		if(resetUI()){
			if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
				onInitWhenPortrait(prevContentView);
			}else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
				onInitWhenLandscape(prevContentView);
			}
		}
	}
	
	@Override
	protected void onInit(View prevContentView) {
		// TODO Auto-generated method stub
		int orientation = getRequestedOrientation();
		if(orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
			onInitWhenLandscape(prevContentView);
		}else if(orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
			onInitWhenPortrait(prevContentView);
		}
	}
	
	protected abstract void onInitWhenPortrait(View prevContentView);
	
	protected abstract void onInitWhenLandscape(View prevContentView);
	
}
