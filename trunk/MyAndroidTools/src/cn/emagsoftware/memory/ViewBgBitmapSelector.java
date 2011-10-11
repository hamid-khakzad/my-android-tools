package cn.emagsoftware.memory;

import java.util.LinkedList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ViewBgBitmapSelector extends ViewBitmapSelector {
	
	public ViewBgBitmapSelector(){
		super();
	}
	
	public ViewBgBitmapSelector(Resources res,int[] drawableIdsToExclude){
		super(res, drawableIdsToExclude);
	}
	
	@Override
	protected List<Drawable> onSelect(View view) {
		// TODO Auto-generated method stub
		List<Drawable> drawables = new LinkedList<Drawable>();
		Drawable d = view.getBackground();
		if(d != null) drawables.add(d);
		return drawables;
	}
	
}
