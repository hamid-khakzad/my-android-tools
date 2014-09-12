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
    protected void registerContentObserver(Cursor data, ForceLoadContentObserver observer) {
        data.getCount();
        data.registerContentObserver(observer);
    }

    @Override
    protected void extraCountCheckInBackground(int count) {
        super.extraCountCheckInBackground(count);
        if(count == -1) {
            throw new IllegalStateException("in BaseCursorPageLoader,loadCountInBackground()'value can not be -1");
        }
    }

    @Override
    protected int getCount(Cursor data) {
        return data.getCount();
    }

    @Override
    protected Cursor merge(Cursor old, Cursor add) {
        return null;
    }

}
