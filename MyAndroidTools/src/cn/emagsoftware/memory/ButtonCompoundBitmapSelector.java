package cn.emagsoftware.memory;

import java.util.LinkedList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;

public class ButtonCompoundBitmapSelector extends AbstractBitmapSelector {
	
	public ButtonCompoundBitmapSelector(){
		super();
	}
	
	public ButtonCompoundBitmapSelector(Resources res,int[] drawableIdsToExclude){
		super(res, drawableIdsToExclude);
	}
	
	@Override
	protected List<Drawable> onSelect(View view) {
		// TODO Auto-generated method stub
		List<Drawable> drawables = new LinkedList<Drawable>();
		if(view instanceof Button){
			Drawable[] dArr = ((Button)view).getCompoundDrawables();
			for(Drawable d : dArr){
				if(d != null) drawables.add(d);
			}
		}
		return drawables;
	}
	
}
