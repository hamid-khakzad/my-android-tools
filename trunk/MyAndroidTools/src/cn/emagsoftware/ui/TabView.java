package cn.emagsoftware.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class TabView extends LinearLayout implements OnClickListener,OnTouchListener {
	
	protected Context context = null;
	
	protected LinearLayout titleBox = null;
	protected LinearLayout titleShadowBox = null;
	protected LinearLayout contentBox = null;
	
	protected Button selectedTitle = null;   //当前处于选中状态的Tab标题，除非当前Tab容器没有任何Tab，否则时刻有一个Tab处于选中状态
	protected List<Button> titles = new ArrayList<Button>();
	protected Map<Button, View> contents = new HashMap<Button, View>();
	
	protected Float tabTextSize;
	protected Integer selectedTabTextColor;
	protected Integer unSelectedTabTextColor;
	protected Drawable selectedTabBg;
	protected Drawable unSelectedTabBg;
	protected Drawable tabSeparatorBg;
	
	protected GestureDetector gestureDetector = null;
	protected OnTabChangedListener onTabChangedListener = null;
	
	public TabView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		setOrientation(LinearLayout.VERTICAL);
		this.titleBox = new LinearLayout(context);
		addView(titleBox, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		this.titleShadowBox = new LinearLayout(context);
		addView(titleShadowBox, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		this.contentBox = new LinearLayout(context);
		ScrollView contentWrap = new ScrollView(context);
		contentWrap.addView(contentBox,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
		addView(contentWrap, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
		gestureDetector = new GestureDetector(new OnGestureBaseListener(){
			@Override
			public boolean onSwipeBottom(MotionEvent e1, MotionEvent e2,float velocityX, float velocityY) {
				// TODO Auto-generated method stub
				return false;
			}
			@Override
			public boolean onSwipeLeft(MotionEvent e1, MotionEvent e2,float velocityX, float velocityY) {
				// TODO Auto-generated method stub
				TabView tabView = TabView.this;
				int tabIndex = tabView.getSelectedTabIndex();
				if(tabIndex < tabView.getTabCount()-1) {
					tabView.setSelectedTab(tabIndex + 1);
					return true;
				}
				return false;
			}
			@Override
			public boolean onSwipeRight(MotionEvent e1, MotionEvent e2,float velocityX, float velocityY) {
				// TODO Auto-generated method stub
				TabView tabView = TabView.this;
				int tabIndex = tabView.getSelectedTabIndex();
				if(tabIndex > 0) {
					tabView.setSelectedTab(tabIndex - 1);
					return true;
				}
				return false;
			}
			@Override
			public boolean onSwipeTop(MotionEvent e1, MotionEvent e2,float velocityX, float velocityY) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		//默认UI设置
        setTabTextSize(16);
        setTabTextColor(Color.WHITE, Color.GRAY);
        setTabBg(context.getResources().getIdentifier("selector_generic_tab_sel","drawable",context.getPackageName()), context.getResources().getIdentifier("selector_generic_tab","drawable",context.getPackageName()));
        setTabSeparatorBg(context.getResources().getIdentifier("shape_generic_tab_separator","drawable",context.getPackageName()));
        setTabShadowBg(context.getResources().getIdentifier("shape_generic_tab_shadow","drawable",context.getPackageName()));
	}
	
	public void setTabTextSize(float size){
		for(int i = 0;i < titles.size();i++){
			Button title = titles.get(i);
			title.setTextSize(size);
		}
		this.tabTextSize = size;
	}
	
	public void setTabTextColor(int selectedColor,int unselectedColor){
		for(int i = 0;i < titles.size();i++){
			Button title = titles.get(i);
			if(title == selectedTitle) title.setTextColor(selectedColor);
			else title.setTextColor(unselectedColor);
		}
		this.selectedTabTextColor = selectedColor;
		this.unSelectedTabTextColor = unselectedColor;
	}
	
	public void setTabBg(Drawable selectedDrawable,Drawable unselectedDrawable){
		for(int i = 0;i < titles.size();i++){
			Button title = titles.get(i);
			if(title == selectedTitle) title.setBackgroundDrawable(selectedDrawable);
			else title.setBackgroundDrawable(unselectedDrawable);
		}
		this.selectedTabBg = selectedDrawable;
		this.unSelectedTabBg = unselectedDrawable;
	}
	
	public void setTabBg(int selectedResId,int unselectedResId){
		setTabBg(context.getResources().getDrawable(selectedResId),context.getResources().getDrawable(unselectedResId));
	}
	
	public void setTabSeparatorBg(Drawable drawable){
		int addCount = titles.size() - 1;
		int addIndex = 1;
		for(int i = 0;i < addCount;i++){
			Button separator = new Button(context);
			separator.setPadding(0, separator.getPaddingTop(), 0, separator.getPaddingBottom());
			separator.setBackgroundDrawable(drawable);
			titleBox.addView(separator, addIndex);
			addIndex = addIndex + 2;
		}
		this.tabSeparatorBg = drawable;
	}
	
	public void setTabSeparatorBg(int resId){
		setTabSeparatorBg(context.getResources().getDrawable(resId));
	}
	
	public void setTabShadowBg(Drawable drawable){
		titleShadowBox.setBackgroundDrawable(drawable);
	}
	
	public void setTabShadowBg(int resId){
		setTabShadowBg(context.getResources().getDrawable(resId));
	}
	
	public void addTab(String title,View content){
		//添加分隔符
		if(tabSeparatorBg != null && getTabCount() > 0){
			Button separator = new Button(context);
			separator.setPadding(0, separator.getPaddingTop(), 0, separator.getPaddingBottom());
			separator.setBackgroundDrawable(tabSeparatorBg);
			titleBox.addView(separator, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.FILL_PARENT));
		}
		
		//添加Tab
		Button titleBtn = new Button(context);
		titleBtn.setText(title);
		if(tabTextSize != null) titleBtn.setTextSize(tabTextSize);
		titleBtn.setOnClickListener(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
		lp.weight = 1;
		titleBox.addView(titleBtn, lp);
		contentBox.addView(content, lp);
		titles.add(titleBtn);
		contents.put(titleBtn, content);
		
		//设置Touch事件，这里主要是为屏幕滑动提供入口
		content.setOnTouchListener(this);
		
		if(selectedTitle == null) {   //当前容器没有选中的Tab，即当前容器没有Tab
			if(selectedTabTextColor != null) titleBtn.setTextColor(selectedTabTextColor);
			if(selectedTabBg != null) titleBtn.setBackgroundDrawable(selectedTabBg);
			selectedTitle = titleBtn;
		}else {
			if(unSelectedTabTextColor != null) titleBtn.setTextColor(unSelectedTabTextColor);
			if(unSelectedTabBg != null) titleBtn.setBackgroundDrawable(unSelectedTabBg);
			content.setVisibility(View.GONE);
		}
	}
	
	public int getSelectedTabIndex(){
		if(selectedTitle == null) return -1;
		return titles.indexOf(selectedTitle);
	}
	
	public View getTab(int index){
		Button title = titles.get(index);
		return contents.get(title);
	}
	
	public void removeTab(int index){
		Button title = titles.get(index);
		View content = contents.get(title);
		titleBox.removeView(title);
		if(tabSeparatorBg != null && index > 0) titleBox.removeViewAt(index-1);  //同时移除分隔符
		contentBox.removeView(content);
		titles.remove(index);
		contents.remove(title);
		if(selectedTitle == title){   //当前移除的Tab是处于选中状态的
			if(titles.size() == 0) selectedTitle = null;
			else setSelectedTab(0);
		}
	}
	
	public void setSelectedTab(int index){
		Button title = titles.get(index);
		onClick(title);
	}
	
	public int getTabCount(){
		return titles.size();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == selectedTitle) return;    //若点击的是已经选中的Tab
		
		if(selectedTitle != null){
			if(unSelectedTabTextColor != null) selectedTitle.setTextColor(unSelectedTabTextColor);
			if(unSelectedTabBg != null) selectedTitle.setBackgroundDrawable(unSelectedTabBg);
			View oldContent = contents.get(selectedTitle);
			oldContent.setVisibility(View.GONE);
		}
		
		Button newTitle = (Button)v;
		if(selectedTabTextColor != null) newTitle.setTextColor(selectedTabTextColor);
		if(selectedTabBg != null) newTitle.setBackgroundDrawable(selectedTabBg);
		View newContent = contents.get(newTitle);
		newContent.setVisibility(View.VISIBLE);
		selectedTitle = newTitle;
		
		if(onTabChangedListener != null) {
			int index = titles.indexOf(v);
			onTabChangedListener.onTabChanged(this, newContent, index);   //触发对外的事件
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return gestureDetector.onTouchEvent(event);
	}
	
	public void setOnTabChangedListener(OnTabChangedListener onTabChangedListener){
		this.onTabChangedListener = onTabChangedListener;
	}
	
	public interface OnTabChangedListener{
		public void onTabChanged(View main,View content,int index);
	}
	
}
