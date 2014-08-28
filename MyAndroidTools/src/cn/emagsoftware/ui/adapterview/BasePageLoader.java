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
    private int mPage = 1;
    private int mPageCount = -1;
    private List<DataHolder> mPageData;
    private int mPageDataSize = -1;
    private int mDataSize;

    public BasePageLoader(Context context, List<DataHolder> oldData, int pageSize) {
        super(context,oldData);
        if(pageSize <= 0) throw new IllegalArgumentException("pageSize <= 0");
        mPageSize = pageSize;
        if(oldData != null) {
            mStart = oldData.size();
        }
        mPage = mStart / pageSize;
        mPage = mStart%pageSize==0?mPage:mPage+1;
        mDataSize = mStart;
    }

    @Override
    public final List<DataHolder> loadInBackgroundImpl() throws Exception {
        return loadPageInBackground(mStart,mPage + 1);
    }

    @Override
    public void deliverResult(LoaderResult<List<DataHolder>> data) {
        if(data != null && data.getException() == null) {
            List<DataHolder> preData = null;
            if(mResult == null) {
                preData = getOldData();
            }else {
                List<DataHolder> preAllData = mResult.getData();
                if(preAllData != null) {
                    preData = new ArrayList<DataHolder>(preAllData);
                    if(mPageData != null) {
                        preData.removeAll(mPageData);
                    }
                }
            }
            mPageData = data.getData();
            mPageDataSize = mPageData==null?0:mPageData.size();
            if(preData == null) {
                data = new LoaderResult<List<DataHolder>>(null,mPageData);
            }else if(mPageData == null) {
                data = new LoaderResult<List<DataHolder>>(null,preData);
            }else {
                List<DataHolder> all = new ArrayList<DataHolder>(preData.size() + mPageData.size());
                all.addAll(preData);
                all.addAll(mPageData);
                data = new LoaderResult<List<DataHolder>>(null,all);
            }
            List<DataHolder> curData = data.getData();
            mDataSize = curData==null?0:curData.size();
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
