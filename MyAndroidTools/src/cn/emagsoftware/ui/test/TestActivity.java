package cn.emagsoftware.ui.test;

import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.ToastManager;
import cn.emagsoftware.ui.adapterview.AsyncDataExecutor;
import cn.emagsoftware.ui.adapterview.AsyncDataScheduler;
import cn.emagsoftware.ui.adapterview.BaseLazyLoadingAdapter;
import cn.emagsoftware.ui.adapterview.DataHolder;
import cn.emagsoftware.ui.adapterview.ViewHolder;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TestActivity extends Activity {
	
	protected AsyncDataScheduler mAsyncDataScheduler = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		ListView g = new ListView(this);
		final TextView tv = new TextView(this);
		g.addFooterView(tv);
		BaseLazyLoadingAdapter blla = new BaseLazyLoadingAdapter(this,5) {
			@Override
			public void onBeginLoad(Context context, Object condition) {
				// TODO Auto-generated method stub
				tv.setText("正在加载...");
			}
			@Override
			public void onAfterLoad(Context context, Object condition, Exception exception) {
				// TODO Auto-generated method stub
				if(exception == null){
					tv.setText("加载成功");
				}else{
					tv.setText("加载失败");
				}
			}
			@Override
			public List<DataHolder> onLoad(Context context, Object condition, int start, int limit) throws Exception {
				// TODO Auto-generated method stub
				Thread.sleep(3000);
				List<DataHolder> d = new ArrayList<DataHolder>();
				for(int i = 0;i < 5;i++){
					d.add(new DataHolder(i) {
						@Override
						public View onCreateView(final Context context, final int position, Object data) {
							// TODO Auto-generated method stub
							LinearLayout ll = new LinearLayout(context);
							Button b = new Button(TestActivity.this);
							b.setFocusable(false);
							b.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									ToastManager.showShort(context, String.valueOf(position));
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
						public void onUpdateView(final Context context, final int position, View view, Object data) {
							// TODO Auto-generated method stub
							ViewHolder vh = (ViewHolder)view.getTag();
							Button b = (Button)vh.getParams()[0];
							b.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									ToastManager.showShort(context, String.valueOf(position));
								}
							});
							if(isAsyncDataCompleted()) b.setText("complete");
							else b.setText("prepare");
						}
					});
				}
				return d;
			}
		};
		g.setAdapter(blla);
		blla.bindLazyLoading(g);
		blla.load(null);
		mAsyncDataScheduler = new AsyncDataScheduler(g, 5, new AsyncDataExecutor(1) {
			@Override
			public void onExecute(List<Integer> positions, List<DataHolder> holders) throws Exception {
				// TODO Auto-generated method stub
				Thread.sleep(5*1000);
			}
		});
		setContentView(g);
	}
	
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	mAsyncDataScheduler.start();
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	mAsyncDataScheduler.stop();
    }
    
}
