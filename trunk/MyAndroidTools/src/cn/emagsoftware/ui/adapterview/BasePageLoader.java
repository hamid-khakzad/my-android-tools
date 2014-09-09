package cn.emagsoftware.ui.adapterview;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.LoaderResult;

/**
 * Created by Wendell on 14-8-24.
 */
public abstract class BasePageLoader extends BaseLoader implements PageInterface<List<DataHolder>> {

    private int mPageSize;
    private int mStart = 0;
    private int mStartSign;
    private int mPageCount = -1;
    private int mCurPageSize = -1;

    public BasePageLoader(Context context, int pageSize) {
        super(context);
        if(pageSize <= 0) throw new IllegalArgumentException("pageSize <= 0");
        mPageSize = pageSize;
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
        mStart = -1;
    }

    @Override
    public void forceRefresh() {
        super.forceRefresh();
        mStart = -1;
    }

    @Override
    public void forcePageLoad() {
        super.forceLoad();
        List<DataHolder> data = mResult==null?null:mResult.getData();
        mStart = data==null?0:data.size();
    }

    @Override
    protected final List<DataHolder> loadInBackgroundImpl(boolean isRefresh) throws Exception {
        int start = mStart;
        if(start == -1) {
            start = 0;
            mStartSign = start;
        }else {
            int calcStart = loadCountInBackground();
            if(calcStart != -1 && calcStart < 0) {
                throw new IllegalStateException("loadCountInBackground()'value should (==-1 or >=0)");
            }
            if(calcStart == -1) {
                mStartSign = -1;
            }else {
                start = calcStart;
                mStartSign = start;
            }
            if(isRefresh) { //解决同步问题
                isRefresh = false;
            }
        }
        int page = start / mPageSize;
        page = start%mPageSize==0?page+1:page+2;
        return loadPageInBackground(isRefresh,start,page);
    }

    @Override
    protected void deliverLoadedResult(LoaderResult<List<DataHolder>> data) {
        if(data != null && data.getException() == null) {
            List<DataHolder> pageData = data.getData();
            if(mStartSign == -1) {
                List<DataHolder> oldData = mResult==null?null:mResult.getData();
                mCurPageSize = pageData==null?0:pageData.size();
                if(oldData != null) {
                    if(pageData == null) {
                        data = new LoaderResult<List<DataHolder>>(null,oldData);
                    }else {
                        List<DataHolder> all = new ArrayList<DataHolder>(oldData.size() + pageData.size());
                        all.addAll(oldData);
                        all.addAll(pageData);
                        data = new LoaderResult<List<DataHolder>>(null,all);
                    }
                }
            }else {
                int allSize = pageData==null?0:pageData.size();
                mCurPageSize = allSize - mStartSign;
                if(mCurPageSize < 0) {
                    mCurPageSize = 0;
                }
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
                List<DataHolder> data = mResult.getData();
                int curSize = data==null?0:data.size();
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
