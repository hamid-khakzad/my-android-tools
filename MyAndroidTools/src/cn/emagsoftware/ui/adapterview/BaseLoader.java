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
     * <p>ʹ��final��ԭ������List<DataHolder>����ҪRelease���ұ��������������߼���������</>
     * @param data
     */
    @Override
    protected final void onReleaseData(List<DataHolder> data) {
    }

    @Override
    protected void registerContentObserver(List<DataHolder> data, ForceLoadContentObserver observer) {
    }

}
