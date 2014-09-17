package cn.emagsoftware.ui.test;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.adapterview.BasePageLoader;
import cn.emagsoftware.ui.adapterview.DataHolder;
import cn.emagsoftware.util.MathUtilities;

/**
 * Created by Wendell on 14-8-22.
 */
public class TestLoader extends BasePageLoader {

    public TestLoader(Context context) {
        super(context,20);
    }

    @Override
    protected int loadCountInBackground() throws Exception {
        return -1;
    }

    @Override
    protected List<DataHolder> loadPageInBackground(boolean isRefresh, int start, int page) throws Exception {
        Thread.sleep(3000);
        if(MathUtilities.Random(3) == 2) {
            throw new Exception("test");
        }
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
