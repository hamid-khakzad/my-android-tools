package cn.emagsoftware.ui.adapterview;

import android.content.Context;

import java.util.List;

import cn.emagsoftware.ui.BaseTaskLoader;

/**
 * Created by Wendell on 14-8-22.
 */
public abstract class DataHolderLoader extends BaseTaskLoader<List<DataHolder>> {

    public DataHolderLoader(Context context,List<DataHolder> oldData) {
        super(context,oldData);
    }

    @Override
    public void registerContentObserver(List<DataHolder> data, ForceLoadContentObserver observer) {
    }

    @Override
    public void onReleaseData(List<DataHolder> data) {
    }

    @Override
    public List<DataHolder> cloneInBackground(List<DataHolder> oldData) {
        return oldData;
    }

}
