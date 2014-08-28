package cn.emagsoftware.ui.adapterview;

import android.content.Context;
import android.database.Cursor;

import cn.emagsoftware.ui.LoaderResult;

/**
 * Created by Wendell on 14-8-27.
 */
public abstract class BaseCursorPageLoader extends BaseCursorLoader implements PageInterface<Cursor> {

    private int mPageSize;
    private int mStart = 0;
    private int mPage = 1;
    private int mPageCount = -1;
    private int mPageDataSize = -1;
    private int mDataSize;

    public BaseCursorPageLoader(Context context, Cursor oldData, int pageSize) {
        super(context,oldData);
        if(pageSize <= 0) throw new IllegalArgumentException("pageSize <= 0");
        mPageSize = pageSize;
        if(oldData != null) {
            mStart = oldData.getCount();
        }
        mPage = mStart / pageSize;
        mPage = mStart%pageSize==0?mPage:mPage+1;
        mDataSize = mStart;
    }

    @Override
    public final Cursor loadInBackgroundImpl() throws Exception {
        return loadPageInBackground(mStart,mPage + 1);
    }

    @Override
    public void deliverResult(LoaderResult<Cursor> data) {
        if(data != null && data.getException() == null) {
            Cursor curData = data.getData();
            mDataSize = curData==null?0:curData.getCount();
            mPageDataSize = mDataSize - mStart;
            if(mPageDataSize < 0) {
                mPageDataSize = 0;
            }
        }
        super.deliverResult(data);
    }

    @Override
    public void setPageCount(int pageCount) {
        if(pageCount < 0) throw new IllegalArgumentException("pageCount < 0");
        this.mPageCount = pageCount;
    }

    @Override
    public boolean isLoadedAll() {
        if(mResult != null) {
            if(mPageCount == -1) {
                if(mPageDataSize != -1) {
                    return mPageDataSize < mPageSize;
                }
            }else {
                int curPageCount = mDataSize/mPageSize;
                curPageCount = mDataSize%mPageSize==0?curPageCount:curPageCount+1;
                return curPageCount >= mPageCount;
            }
        }
        return false;
    }

    @Override
    public boolean isException() {
        if(mResult != null) {
            return mResult.getException() != null;
        }
        return false;
    }

}
