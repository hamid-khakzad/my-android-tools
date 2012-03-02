package cn.emagsoftware.memory;

import java.util.LinkedList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class ImageViewFgBitmapSelector extends AbstractBitmapSelector {
	
	public ImageViewFgBitmapSelector(){
		super();
	}
	
	public ImageViewFgBitmapSelector(Resources res,int[] drawableIdsToExclude){
		super(res, drawableIdsToExclude);
	}
	
	@Override
	protected List<Drawable> onSelect(View view) {
		// TODO Auto-generated method stub
		List<Drawable> drawables = new LinkedList<Drawable>();
		if(view instanceof ImageView){
			Drawable d = ((ImageView)view).getDrawable();
			if(d != null) drawables.add(d);
		}
		return drawables;
	}
	
}
