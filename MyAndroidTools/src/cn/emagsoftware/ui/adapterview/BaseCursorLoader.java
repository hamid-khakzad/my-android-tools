package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.database.Cursor;

import cn.emagsoftware.ui.BaseTaskLoader;

/**
 * Created by Wendell on 14-8-24.
 */
public abstract class BaseCursorLoader extends BaseTaskLoader<Cursor> {

    public BaseCursorLoader(Context context) {
        super(context);
    }

    @Override
    protected void onReleaseData(Cursor data) {
        if(!data.isClosed()) {
            data.close();
        }
    }

}
