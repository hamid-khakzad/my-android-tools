package cn.emagsoftware.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

/**
 * Tab形式的布局类
 * @author Wendell
 * @version 1.3
 */
public class TabLayout extends ViewGroup {
	
	public static final String HEAD_POSITION_TOP = "top";
	public static final String HEAD_POSITION_BOTTOM = "bottom";
	public static final String HEAD_POSITION_LEFT = "left";
	public static final String HEAD_POSITION_RIGHT = "right";
	
	protected boolean isRendered = false;
	protected Class<?> tabClass = Button.class;
	protected String headPosition = HEAD_POSITION_TOP;
	protected int selectedTabIndex = -1;
	protected int tempSelectedTabIndex = -1;
	
	protected ViewGroup head = null;
	protected ViewGroup content = null;
	protected List<View> tabs = new ArrayList<View>();
	
	protected OnTabChangedListener mOnTabChangedListener = null;
	
	public TabLayout(Context context){
		this(context,null,0);
	}
	
	public TabLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TabLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if(attrs != null){
			//TabLayout支持以下定义属性
			String tabClassName = attrs.getAttributeValue(null, "tabClass");
			String headPosition = attrs.getAttributeValue(null, "headPosition");
			String selectedTabIndexStr = attrs.getAttributeValue(null, "selectedTab");
			try{
				if(tabClassName != null) setTabClass(Class.forName(tabClassName));
			}catch(ClassNotFoundException e){
				throw new RuntimeException(e);
			}
			if(headPosition != null) setHeadPosition(headPosition);
			if(selectedTabIndexStr != null) setSelectedTab(Integer.valueOf(selectedTabIndexStr));			
		}
	}
	
	protected void initUI(){
		View child1 = getChildAt(0);
		View child2 = getChildAt(1);
		if(!(child1 instanceof ViewGroup) || !(child2 instanceof ViewGroup)) throw new IllegalStateException("TabLayout children should be ViewGroup but not View!");
		ViewGroup childGroup1 = (ViewGroup)child1;
		ViewGroup childGroup2 = (ViewGroup)child2;
		if(headPosition.equals(HEAD_POSITION_TOP) || headPosition.equals(HEAD_POSITION_LEFT)){
			head = childGroup1;
			content = childGroup2;
		}else if(headPosition.equals(HEAD_POSITION_BOTTOM) || headPosition.equals(HEAD_POSITION_RIGHT)){
			head = childGroup2;
			content = childGroup1;
		}
		tabs.clear();
		initTabs(head);
		if(tempSelectedTabIndex != -1){
			int tempSelectedTabIndexCopy = tempSelectedTabIndex;
			tempSelectedTabIndex = -1;
			setSelectedTab(tempSelectedTabIndexCopy);
		}else if(tabs.size() > 0 && selectedTabIndex == -1) {
			setSelectedTab(0);
		}
	}
	
	/**
	 * <p>使用递归方法初始化所有Tab
	 * @param view
	 */
	protected void initTabs(View view){
		if(view.getClass().equals(tabClass)){
			tabs.add(view);
			final int index = tabs.size()-1;
			view.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					setSelectedTab(index);
				}
			});
		}else if(view instanceof ViewGroup){
			ViewGroup vg = (ViewGroup)view;
			for(int i = 0;i < vg.getChildCount();i++){
				initTabs(vg.getChildAt(i));
			}
		}
	}
	
	public void setTabClass(Class<?> tabClass){
		if(tabClass == null) throw new NullPointerException();
		this.tabClass = tabClass;
	}
	
	public void setHeadPosition(String headPosition){
		if(headPosition == null) throw new NullPointerException();
		if(!headPosition.equals(HEAD_POSITION_TOP) && !headPosition.equals(HEAD_POSITION_BOTTOM) && !headPosition.equals(HEAD_POSITION_LEFT) && !headPosition.equals(HEAD_POSITION_RIGHT))
			throw new IllegalArgumentException("headPosition is invalid!");
		this.headPosition = headPosition;
	}
	
	public void setSelectedTab(int index){
		if(!isRendered){
			this.tempSelectedTabIndex = index;
			return;
		}
		if(index < 0 || index >= tabs.size()) throw new IllegalArgumentException("index is invalid!");
		if(index == selectedTabIndex) return;
		for(int i = 0;i < content.getChildCount();i++){
			View tabView = tabs.get(i);
			View contentView = content.getChildAt(i);
			if(index == i) {
				if(tabView instanceof CompoundButton) ((CompoundButton)tabView).setChecked(true);
				contentView.setVisibility(View.VISIBLE);
			}else {
				if(tabView instanceof CompoundButton) ((CompoundButton)tabView).setChecked(false);
				contentView.setVisibility(View.GONE);
			}
		}
		this.selectedTabIndex = index;
		if(mOnTabChangedListener != null){
			mOnTabChangedListener.onTabChanged(tabs.get(index), content.getChildAt(index), index);
		}
	}
	
	public int getSelectedTabIndex(){
		return selectedTabIndex;
	}
	
	public int getTabCount(){
		return tabs.size();
	}
	
	public void setOnTabChangedListener(OnTabChangedListener mOnTabChangedListener){
		this.mOnTabChangedListener = mOnTabChangedListener;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		View child1 = getChildAt(0);
		View child2 = getChildAt(1);
		if(headPosition.equals(HEAD_POSITION_TOP) || headPosition.equals(HEAD_POSITION_BOTTOM)){
			int top = 0;
			if(child1.getVisibility() != View.GONE){
				int child1Width = child1.getMeasuredWidth();
				int child1Height = child1.getMeasuredHeight();
				child1.layout(0, 0, child1Width, child1Height);
				top = child1Height;
			}
			if(child2.getVisibility() != View.GONE) {
				child2.layout(0, top, child2.getMeasuredWidth(), top + child2.getMeasuredHeight());
			}
		}else if(headPosition.equals(HEAD_POSITION_LEFT) || headPosition.equals(HEAD_POSITION_RIGHT)){
			int left = 0;
			if(child1.getVisibility() != View.GONE){
				int child1Width = child1.getMeasuredWidth();
				int child1Height = child1.getMeasuredHeight();
				child1.layout(0, 0, child1Width, child1Height);
				left = child1Width;
			}
			if(child2.getVisibility() != View.GONE) {
				child2.layout(left, 0, left + child2.getMeasuredWidth(), child2.getMeasuredHeight());
			}
		}
	}
	
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //非EXACTLY模式需要根据子容器来计算父容器的大小，对TabLayout而言比较复杂且实际应用中完全可以避免，故暂不支持
        if(widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) throw new IllegalStateException("TabLayout only can run at EXACTLY mode!");
        
        int count = getChildCount();
        if(count != 2) throw new IllegalStateException("TabLayout only can contains two children!");
        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        
		if(headPosition.equals(HEAD_POSITION_TOP)){
			int remainHeight = heightSize;
			if(child1.getVisibility() != View.GONE){
				LayoutParams lp = child1.getLayoutParams();
				if(lp.height == LayoutParams.FILL_PARENT || lp.height == LayoutParams.WRAP_CONTENT){
					child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
				}else{
					child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
				}
				remainHeight = heightSize - child1.getMeasuredHeight();
				if(remainHeight < 0) remainHeight = 0;
			}
			if(child2.getVisibility() != View.GONE){
				child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(remainHeight, MeasureSpec.EXACTLY));
			}
		}else if(headPosition.equals(HEAD_POSITION_BOTTOM)){
			int remainHeight = heightSize;
			if(child2.getVisibility() != View.GONE){
				LayoutParams lp = child2.getLayoutParams();
				if(lp.height == LayoutParams.FILL_PARENT || lp.height == LayoutParams.WRAP_CONTENT){
					child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST));
				}else{
					child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
				}
				remainHeight = heightSize - child2.getMeasuredHeight();
				if(remainHeight < 0) remainHeight = 0;
			}
			if(child1.getVisibility() != View.GONE){
				child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(remainHeight, MeasureSpec.EXACTLY));
			}
		}else if(headPosition.equals(HEAD_POSITION_LEFT)){
			int remainWidth = widthSize;
			if(child1.getVisibility() != View.GONE){
				LayoutParams lp = child1.getLayoutParams();
				if(lp.width == LayoutParams.FILL_PARENT || lp.width == LayoutParams.WRAP_CONTENT){
					child1.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
				}else{
					child1.measure(MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
				}
				remainWidth = widthSize - child1.getMeasuredWidth();
				if(remainWidth < 0) remainWidth = 0;
			}
			if(child2.getVisibility() != View.GONE){
				child2.measure(MeasureSpec.makeMeasureSpec(remainWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
			}
		}else if(headPosition.equals(HEAD_POSITION_RIGHT)){
			int remainWidth = widthSize;
			if(child2.getVisibility() != View.GONE){
				LayoutParams lp = child2.getLayoutParams();
				if(lp.width == LayoutParams.FILL_PARENT || lp.width == LayoutParams.WRAP_CONTENT){
					child2.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
				}else{
					child2.measure(MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
				}
				remainWidth = widthSize - child2.getMeasuredWidth();
				if(remainWidth < 0) remainWidth = 0;
			}
			if(child1.getVisibility() != View.GONE){
				child1.measure(MeasureSpec.makeMeasureSpec(remainWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
			}
		}
		setMeasuredDimension(widthSize, heightSize);
		isRendered = true;
		initUI();
    }
    
	public interface OnTabChangedListener{
		public void onTabChanged(View tab,View contentItem,int index);
	}
	
}
