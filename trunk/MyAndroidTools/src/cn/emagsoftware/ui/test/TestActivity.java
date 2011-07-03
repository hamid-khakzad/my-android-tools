package cn.emagsoftware.ui.test;

import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.R;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		List<DataHolder> d = new ArrayList<DataHolder>();
		for(int i = 0;i < 100;i++){
			d.add(new DataHolder(i) {
				@Override
				public void onUpdateView(int position, View view, Object data) {
					// TODO Auto-generated method stub
					ViewHolder vh = (ViewHolder)view.getTag();
					TextView tv = (TextView)vh.getParams()[0];
					tv.setText("text:" + data);
				}
				@Override
				public View onCreateView(int position, Object data) {
					// TODO Auto-generated method stub
					TextView t = new TextView(TestActivity.this);
					t.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, 50));
					t.setText("text:" + data);
					ViewHolder vh = new ViewHolder(t);
					t.setTag(vh);
					return t;
				}
				@Override
				public boolean isAsyncDataCompleted() {
					// TODO Auto-generated method stub
					return true;
				}
			});
		}
		ListView g = new ListView(this);
		
		g.setAdapter(new SimpleAdapter(this, d));
		setContentView(g);
		
		
	}
	
}
