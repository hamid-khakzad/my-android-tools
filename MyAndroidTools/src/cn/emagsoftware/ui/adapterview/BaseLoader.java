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
     * <p>ʹ��final��ԭ������List<DataHolder>����ҪRelease���ұ��������������߼���������</>
     * @param data
     */
    @Override
    public final void onReleaseData(List<DataHolder> data) {
    }

    /**
     * <p>ʹ��final��ԭ������onReleaseDataû���κβ����ұ���ʶΪfinal���ʲ���Ҫ����Clone</>
     * @param oldData
     * @return
     */
    @Override
    public final List<DataHolder> cloneInBackground(List<DataHolder> oldData) {
        return oldData;
    }

}
