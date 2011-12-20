package cn.emagsoftware.ui;

import cn.emagsoftware.ui.theme.ThemeActivity;

import android.content.res.Configuration;
import android.view.View;

public abstract class ConfigChangeHandlingActivity extends ThemeActivity {
	
	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if(configurationChangedCallback(newConfig)) resetContentView();
	}
	
	@Override
	protected final void onSetContentView(View prevContentView) {
		// TODO Auto-generated method stub
		int orientation = getResources().getConfiguration().orientation;
		if(orientation == Configuration.ORIENTATION_PORTRAIT){
			onSetContentViewWhenPortrait(prevContentView);
		}else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
			onSetContentViewWhenLandscape(prevContentView);
		}
	}
	
	protected abstract boolean configurationChangedCallback(Configuration newConfig);
	
	protected abstract void onSetContentViewWhenPortrait(View prevContentView);
	
	protected abstract void onSetContentViewWhenLandscape(View prevContentView);
	
}
