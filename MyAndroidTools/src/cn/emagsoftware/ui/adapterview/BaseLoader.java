package cn.emagsoftware.ui.adapterview;

import android.content.Context;

import java.util.List;

import cn.emagsoftware.ui.BaseTaskLoader;

/**
 * Created by Wendell on 14-8-22.
 */
public abstract class BaseLoader extends BaseTaskLoader<List<DataHolder>> {

    public BaseLoader(Context context, List<DataHolder> oldData) {
        super(context,oldData);
    }

    @Override
    public void registerContentObserver(List<DataHolder> data, ForceLoadContentObserver observer) {
    }

    /**
     * <p>使用final的原因在于List<DataHolder>不需要Release，且避免给子类的其他逻辑带来困惑</>
     * @param data
     */
    @Override
    public final void onReleaseData(List<DataHolder> data) {
    }

    /**
     * <p>使用final的原因在于onReleaseData没有任何操作且被标识为final，故不需要进行Clone</>
     * @param oldData
     * @return
     */
    @Override
    public final List<DataHolder> cloneInBackground(List<DataHolder> oldData) {
        return oldData;
    }

}
