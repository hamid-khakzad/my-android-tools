package cn.emagsoftware.memory;

import java.util.LinkedList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class ViewAllBitmapSelector extends ViewBitmapSelector {
	
	@Override
	public List<Drawable> onSelect(View view) {
		// TODO Auto-generated method stub
		List<Drawable> drawables = new LinkedList<Drawable>();
		if(view instanceof ImageView){
			Drawable d = ((ImageView)view).getDrawable();
			if(d != null) drawables.add(d);
		}
		Drawable d = view.getBackground();
		if(d != null) drawables.add(d);
		return drawables;
	}
	
}
