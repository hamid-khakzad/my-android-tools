package com.wendell.ui.test;

import com.wendell.ui.FlipLayout;
import com.wendell.ui.MenuWindow;
import com.wendell.ui.ToastManager;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class TestActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		FlipLayout fl = new FlipLayout(this);
		setContentView(fl);
		TextView tv = new TextView(this);
		tv.setText("step1");
		tv.setBackgroundColor(Color.RED);
		fl.addView(tv);
		tv = new TextView(this);
		tv.setText("step2");
		fl.addView(tv);
		tv = new TextView(this);
		tv.setText("step3");
		fl.addView(tv);
		fl.setToScreen(1);
		fl.setOnFlingChangedListener(new FlipLayout.OnFlingChangedListener() {
			@Override
			public void onFlingChanged(int whichScreen) {
				// TODO Auto-generated method stub
				ToastManager.showShort(TestActivity.this, String.valueOf(whichScreen));
			}
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuWindow mw = new MenuWindow(this);
		TextView tv = new TextView(this);
		tv.setText("≤‚ ‘");
		mw.setContentView(tv);
		mw.show(this.getWindow(), 20);
		//new ToastWindow(this, "≤‚ ‘").showCenterForMillis(this.getWindow(), 10000);
		return false;
	}
	
}
