package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Wendell on 14-8-15.
 */
public class GenericCursorAdapter extends CursorAdapter {

    private int               mViewTypeCount = 1;
    private DataHolderCreator mCreator = null;
    private DataHolder mHolder = null;
    private boolean mIsNewView = false;

    public GenericCursorAdapter(Context context, Cursor c) {
        super(context,c,FLAG_REGISTER_CONTENT_OBSERVER);
    }

    public GenericCursorAdapter(Context context, Cursor c, int viewTypeCount) {
        super(context,c,FLAG_REGISTER_CONTENT_OBSERVER);
        if (viewTypeCount <= 0)
            throw new IllegalArgumentException("viewTypeCount should great than zero.");
        this.mViewTypeCount = viewTypeCount;
    }

    public void bindDataHolderCreator(DataHolderCreator creator) {
        if(creator == null) throw new NullPointerException("creator == null");
        Cursor cursor = getCursor();
        if(mDataValid && cursor != null) {
            DataHolder holder = creator.createDataHolder(cursor);
            if(holder == null) throw new NullPointerException("the method 'createDataHolder' can not return null.");
            this.mHolder = holder;
        }else {
            this.mHolder = null;
        }
        this.mCreator = creator;
    }

    private void checkDataHolderCreator() {
        if(mCreator == null) throw new IllegalStateException("should call 'bindDataHolderCreator' first.");
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        checkDataHolderCreator();
        Cursor cursor = super.swapCursor(newCursor);
        if(mDataValid && newCursor != null) {
            DataHolder holder = mCreator.createDataHolder(newCursor);
            if(holder == null) throw new NullPointerException("the method 'createDataHolder' can not return null.");
            this.mHolder = holder;
        }else {
            this.mHolder = null;
        }
        return cursor;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        mIsNewView = false;
        return super.getView(position, convertView, parent);
    }

    @Override
    public final View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        checkDataHolderCreator();
        mIsNewView = true;
        return mHolder.onCreateView(context,cursor.getPosition(),mHolder.getData());
    }

    @Override
    public final void bindView(View view, Context context, Cursor cursor) {
        checkDataHolderCreator();
        if(mIsNewView) return;
        mHolder.onUpdateView(context,cursor.getPosition(),view,mHolder.getData());
    }

    @Override
    public final int getItemViewType(int position) {
        checkDataHolderCreator();
        if(!mDataValid) throw new IllegalStateException("this should only be called when the cursor is valid");
        if(!getCursor().moveToPosition(position)) throw new IllegalStateException("couldn't move cursor to position " + position);
        return mHolder.getType();
    }

    @Override
    public final int getViewTypeCount() {
        return mViewTypeCount;
    }

    public static interface DataHolderCreator {

        public DataHolder createDataHolder(Cursor cursor);

    }

}
