package cn.emagsoftware.ui.adapterview;

import android.content.Context;

import java.util.List;

import cn.emagsoftware.ui.BaseTaskLoader;
import cn.emagsoftware.ui.LoaderResult;

/**
 * Created by Wendell on 14-8-22.
 */
public abstract class DataHolderLoader extends BaseTaskLoader<List<DataHolder>> {

    public DataHolderLoader(Context context,LoaderResult<List<DataHolder>> oldResult) {
        super(context,oldResult);
    }

    public DataHolderLoader(Context context,LoaderResult<List<DataHolder>> oldResult,Object param) {
        super(context,oldResult,param);
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
