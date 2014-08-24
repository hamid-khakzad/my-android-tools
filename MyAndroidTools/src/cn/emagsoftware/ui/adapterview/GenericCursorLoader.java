package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.database.Cursor;

import cn.emagsoftware.ui.BaseTaskLoader;

/**
 * Created by Wendell on 14-8-24.
 */
public abstract class GenericCursorLoader extends BaseTaskLoader<Cursor> {

    public GenericCursorLoader(Context context,Cursor oldData) {
        super(context,oldData);
    }

    @Override
    public void registerContentObserver(Cursor data, ForceLoadContentObserver observer) {
        data.getCount();
        data.registerContentObserver(observer);
    }

    @Override
    public void onReleaseData(Cursor data) {
        if(!data.isClosed()) {
            data.close();
        }
    }

}
