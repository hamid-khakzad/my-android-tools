package cn.emagsoftware.ui.adapterview;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.BaseTaskPageLoader;

/**
 * Created by Wendell on 14-8-24.
 */
public abstract class BasePageLoader extends BaseTaskPageLoader<List<DataHolder>> {

    public BasePageLoader(Context context, int pageSize) {
        super(context,pageSize);
    }

    @Override
    protected void onReleaseData(List<DataHolder> data) {
    }

    @Override
    protected void registerContentObserver(List<DataHolder> data, ForceLoadContentObserver observer) {
    }

    @Override
    protected int getCount(List<DataHolder> data) {
        return data.size();
    }

    @Override
    protected List<DataHolder> merge(List<DataHolder> old, List<DataHolder> add) {
        List<DataHolder> all = new ArrayList<DataHolder>(old.size() + add.size());
        all.addAll(old);
        all.addAll(add);
        return all;
    }

}
