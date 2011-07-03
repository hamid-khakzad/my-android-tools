package cn.emagsoftware.ui.test;

import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.R;
import cn.emagsoftware.ui.adapterview.AsyncDataExecutor;
import cn.emagsoftware.ui.adapterview.AsyncDataScheduler;
import cn.emagsoftware.ui.adapterview.DataHolder;
import cn.emagsoftware.ui.adapterview.SimpleAdapter;
import cn.emagsoftware.ui.adapterview.ViewHolder;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

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
				public View onCreateView(int position, Object data) {
					// TODO Auto-generated method stub
					TextView t = new TextView(TestActivity.this);
					t.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, 50));
					if(isAsyncDataCompleted()) t.setText("text(OK):" + data);
					else t.setText("text:" + data);
					ViewHolder vh = new ViewHolder(t);
					t.setTag(vh);
					return t;
				}
				@Override
				public void onUpdateView(int position, View view, Object data) {
					// TODO Auto-generated method stub
					ViewHolder vh = (ViewHolder)view.getTag();
					TextView tv = (TextView)vh.getParams()[0];
					if(isAsyncDataCompleted()) tv.setText("text(OK):" + data);
					else tv.setText("text:" + data);
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
