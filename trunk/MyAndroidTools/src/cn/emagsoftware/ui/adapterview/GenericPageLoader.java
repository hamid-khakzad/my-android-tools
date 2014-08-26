package cn.emagsoftware.ui.adapterview;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.emagsoftware.ui.LoaderResult;

/**
 * Created by Wendell on 14-8-24.
 */
public abstract class GenericPageLoader extends GenericLoader implements PageInterface<List<DataHolder>> {

    private int mPageSize;
    private int mStart = 0;
    private int mPage = 1;
    private int mPageCount = -1;
    private List<DataHolder> mPrePageData;
    private List<DataHolder> mPageData;

    public GenericPageLoader(Context context, List<DataHolder> oldData, int pageSize) {
        super(context,oldData);
        if(pageSize <= 0) throw new IllegalArgumentException("pageSize <= 0");
        mPageSize = pageSize;
        if(oldData != null) {
            mStart = oldData.size();
        }
        mPage = mStart / pageSize;
        mPage = mStart%pageSize==0?mPage:mPage+1;
    }

    @Override
    public final List<DataHolder> loadInBackgroundImpl() throws Exception {
        mPageData = loadPageInBackground(mStart,mPage + 1);
        List<DataHolder> preData = null;
        if(mResult == null) {
            preData = getOldData();
        }
        return preData;
    }

    @Override
    public void deliverResult(LoaderResult<List<DataHolder>> data) {
        if(data != null && data.getException() == null) {
            List<DataHolder> preData = data.getData();
            if(mResult != null) {
                List<DataHolder> preAllData = mResult.getData();
                if(preAllData != null) {
                    preData = new ArrayList<DataHolder>(preAllData);
                    if(mPrePageData != null) {
                        preData.removeAll(mPrePageData);
                    }
                }
            }
            mPrePageData = mPageData;
            mPageData = null;
            if(preData == null) {
                data = new LoaderResult<List<DataHolder>>(null,mPrePageData);
            }else if(mPrePageData == null) {
                data = new LoaderResult<List<DataHolder>>(null,preData);
            }else {
                List<DataHolder> all = new ArrayList<DataHolder>(preData.size() + mPrePageData.size());
                all.addAll(preData);
                all.addAll(mPrePageData);
                data = new LoaderResult<List<DataHolder>>(null,all);
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
                if(mResult.getException() == null) {
                    int curPageSize = mPrePageData==null?0:mPrePageData.size();
                    return curPageSize < mPageSize;
                }
            }else {
                List<DataHolder> data = mResult.getData();
                int size = data==null?0:data.size();
                int curPageCount = size/mPageSize;
                curPageCount = size%mPageSize==0?curPageCount:curPageCount+1;
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
