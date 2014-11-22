package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.database.Cursor;

import cn.emagsoftware.ui.BaseTaskPageLoader;

/**
 * Created by Wendell on 14-8-27.
 */
public abstract class BaseCursorPageLoader extends BaseTaskPageLoader<Cursor> {

    public BaseCursorPageLoader(Context context, int pageSize) {
        super(context,pageSize);
    }

    @Override
    protected void onReleaseData(Cursor data) {
        if(!data.isClosed()) {
            data.close();
        }
    }

    @Override
    protected int getCount(Cursor data) {
        return data.getCount();
    }

    @Override
    protected Cursor merge(Cursor old, Cursor add) {
        return add;
    }

}
