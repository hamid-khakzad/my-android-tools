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
    private int mPageCount = -1;
    private int mCurPageSize = -1;

    public BaseCursorPageLoader(Context context, int pageSize) {
        super(context);
        if(pageSize <= 0) throw new IllegalArgumentException("pageSize <= 0");
        mPageSize = pageSize;
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
        mStart = 0;
    }

    @Override
    public void forceRefresh() {
        super.forceRefresh();
        mStart = 0;
    }

    @Override
    public void forcePageLoad() {
        super.forceLoad();
        Cursor data = mResult==null?null:mResult.getData();
        mStart = data==null?0:data.getCount();
    }

    @Override
    protected Cursor loadInBackgroundImpl(boolean isRefresh) throws Exception {
        int start = mStart;
        if(isRefresh && start != 0) { //解决同步问题
            isRefresh = false;
        }
        int page = start / mPageSize;
        page = start%mPageSize==0?page+1:page+2;
        return loadPageInBackground(isRefresh,start,page);
    }

    @Override
    protected void deliverLoadedResult(LoaderResult<Cursor> data) {
        if(data != null && data.getException() == null) {
            Cursor curData = data.getData();
            int curSize = curData==null?0:curData.getCount();
            mCurPageSize = curSize - mStart; //mStart是同步的，否则当前方法将会被取消
            if(mCurPageSize < 0) {
                mCurPageSize = 0;
            }
        }
        super.deliverLoadedResult(data);
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
                if(mCurPageSize != -1) {
                    return mCurPageSize < mPageSize;
                }
            }else {
                Cursor data = mResult.getData();
                int curSize = data==null?0:data.getCount();
                int curPageCount = curSize/mPageSize;
                curPageCount = curSize%mPageSize==0?curPageCount:curPageCount+1;
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
