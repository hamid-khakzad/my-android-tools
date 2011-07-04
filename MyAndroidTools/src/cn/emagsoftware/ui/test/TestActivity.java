package cn.emagsoftware.ui.test;

import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.R;
import cn.emagsoftware.ui.ToastManager;
import cn.emagsoftware.ui.adapterview.AsyncDataExecutor;
import cn.emagsoftware.ui.adapterview.AsyncDataScheduler;
import cn.emagsoftware.ui.adapterview.DataHolder;
import cn.emagsoftware.ui.adapterview.SimpleAdapter;
import cn.emagsoftware.ui.adapterview.ViewHolder;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

public class TestActivity extends Activity {
	
	protected AsyncDataScheduler mAsyncDataScheduler = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		List<DataHolder> d = new ArrayList<DataHolder>();
		for(int i = 0;i < 100;i++){
			d.add(new DataHolder(i) {
				@Override
				public View onCreateView(final Context context, int position, final Object data) {
					// TODO Auto-generated method stub
					LinearLayout ll = new LinearLayout(context);
					Button b = new Button(TestActivity.this);
					b.setFocusable(false);
					b.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							ToastManager.showShort(context, String.valueOf(data));
						}
					});
					ll.addView(b);
					ll.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, 60));
					if(isAsyncDataCompleted()) b.setText("complete");
					else b.setText("prepare");
					ViewHolder vh = new ViewHolder(b);
					ll.setTag(vh);
					return ll;
				}
				@Override
				public void onUpdateView(final Context context, int position, View view, final Object data) {
					// TODO Auto-generated method stub
					ViewHolder vh = (ViewHolder)view.getTag();
					Button b = (Button)vh.getParams()[0];
					b.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							ToastManager.showShort(context, String.valueOf(data));
						}
					});
					if(isAsyncDataCompleted()) b.setText("complete");
					else b.setText("prepare");
				}
			});
		}
		ListView g = new ListView(this);
		g.setAdapter(new SimpleAdapter(this, d));
		mAsyncDataScheduler = new AsyncDataScheduler(g, 5, new AsyncDataExecutor(1) {
			@Override
			public void onExecute(List<Integer> positions, List<DataHolder> holders) throws Exception {
				// TODO Auto-generated method stub
				System.out.println("execute async data in positoin:"+positions.get(0));
				Thread.sleep(5*1000);
			}
		});
		mAsyncDataScheduler.start();
		
		setContentView(g);
		
	}
	
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	if(mAsyncDataScheduler != null){
    		mAsyncDataScheduler.cancelMe();
    		mAsyncDataScheduler.cancelThreads();
    	}
    }
    
}
