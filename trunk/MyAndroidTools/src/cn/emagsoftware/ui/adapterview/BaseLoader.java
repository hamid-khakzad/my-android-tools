package cn.emagsoftware.ui.adapterview;

import android.content.Context;

import java.util.List;

import cn.emagsoftware.ui.BaseTaskLoader;

/**
 * Created by Wendell on 14-8-22.
 */
public abstract class BaseLoader extends BaseTaskLoader<List<DataHolder>> {

    public BaseLoader(Context context) {
        super(context);
    }

    /**
     * <p>使用final的原因在于List<DataHolder>不需要Release，且避免给子类的其他逻辑带来困惑</>
     * @param data
     */
    @Override
    protected final void onReleaseData(List<DataHolder> data) {
    }

    @Override
    protected void registerContentObserver(List<DataHolder> data, ForceLoadContentObserver observer) {
    }

}
