package cn.emagsoftware.ui;

import cn.emagsoftware.ui.theme.ThemeActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;

public abstract class OrientationActivity extends ThemeActivity {
	
	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		resetContentView();
	}
	
	@Override
	protected final void onSetContentView(View prevContentView) {
		// TODO Auto-generated method stub
		int orientation = getRequestedOrientation();
		if(orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
			onSetContentViewWhenPortrait(prevContentView);
		}else if(orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
			onSetContentViewWhenLandscape(prevContentView);
		}
	}
	
	protected abstract void onSetContentViewWhenPortrait(View prevContentView);
	
	protected abstract void onSetContentViewWhenLandscape(View prevContentView);
	
}
