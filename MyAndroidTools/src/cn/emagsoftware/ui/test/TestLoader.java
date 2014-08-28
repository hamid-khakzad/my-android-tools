package cn.emagsoftware.ui.test;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.adapterview.BaseLoader;
import cn.emagsoftware.ui.adapterview.DataHolder;

/**
 * Created by Wendell on 14-8-22.
 */
public class TestLoader extends BaseLoader {

    public TestLoader(Context context,List<DataHolder> oldData) {
        super(context,oldData);
    }

    @Override
    public List<DataHolder> loadInBackgroundImpl() throws Exception {
        Thread.sleep(8000);
        int count = 20;
        List<DataHolder> holders = new ArrayList<DataHolder>(count);
        for(int i = 0;i < count;i++) {
            holders.add(new DataHolder(i) {
                @Override
                public View onCreateView(Context context, int position, Object data) {
                    TextView text = new TextView(context);
                    text.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,140));
                    text.setGravity(Gravity.CENTER_VERTICAL);
                    text.setText("POSITION:" + position);
                    return text;
                }
                @Override
                public void onUpdateView(Context context, int position, View view, Object data) {
                    ((TextView)view).setText("POSITION:" + position);
                }
            });
        }
        return holders;
    }

}
