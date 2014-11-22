package cn.emagsoftware.ui;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;

import cn.emagsoftware.database.sqlite.GenericSQLiteOpenHelper;

/**
 * Created by Wendell on 14-8-20.
 */
public abstract class BaseTaskLoader<D> extends AsyncTaskLoader<LoaderResult<D>> {

    private ForceLoadContentObserver mObserver = null;
    private boolean mIsRefresh = false;
    private boolean mIsRefreshing = false;
    private boolean mIsLoading = false;
    private LoaderResult<D> mLoadedResult = null;
    LoaderResult<D> mResult = null;
    int mResumeType = 0;

    public BaseTaskLoader(Context context) {
        super(context);
        mObserver = new ForceLoadContentObserver();
    }

    @Override
    public void forceLoad() {
        mIsRefresh = false;
        super.forceLoad();
        mIsRefreshing = false;
        mIsLoading = true;
    }

    public void forceRefresh() {
        mIsRefresh = true;
        super.forceLoad();
        mIsRefreshing = true;
        mIsLoading = true;
    }

    @Override
    public boolean cancelLoad() {
        boolean returnVal = super.cancelLoad();
        mIsRefreshing = false;
        mIsLoading = false;
        return returnVal;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    @Override
    public final LoaderResult<D> loadInBackground() {
        D data = null;
        boolean isRefresh = mIsRefresh;
        try {
            data = loadInBackgroundImpl(isRefresh);
        }catch (Exception e) {
            mLoadedResult = new LoaderResult<D>(e,null);
            mLoadedResult.mIsRefresh = isRefresh;
            return mLoadedResult;
        }
        mLoadedResult = new LoaderResult<D>(null,data);
        mLoadedResult.mIsRefresh = isRefresh;
        return mLoadedResult;
    }

    @Override
    public final void deliverResult(LoaderResult<D> data) {
        if(mLoadedResult != null && mLoadedResult == data) {
            Exception exception = data.getException();
            if(exception != null) {
                boolean isRefresh = data.mIsRefresh;
                data = new LoaderResult<D>(exception,mResult==null?null:mResult.getData());
                data.mIsRefresh = isRefresh;
            }
            mLoadedResult = null;
            deliverLoadedResult(data);
            return;
        }
        D curData = data==null?null:data.getData();
        if(isReset()) {
            if(curData != null) {
                onReleaseData(curData);
            }
            return;
        }
        LoaderResult<D> oldResult = mResult;
        mResult = data;
        if(oldResult == null || oldResult.getData() != curData) {
            if(curData != null) {
                try {
                    registerContentObserver(curData, mObserver);
                }catch (RuntimeException e) {
                    onReleaseData(curData);
                    throw e;
                }
            }
        }
        if(isStarted()) {
            super.deliverResult(data);
        }
        if(oldResult != null) {
            D oldData = oldResult.getData();
            if(oldData != curData) {
                if(oldData != null) {
                    onReleaseData(oldData);
                }
            }
        }
    }

    protected void deliverLoadedResult(LoaderResult<D> data) {
        mIsRefreshing = false;
        mIsLoading = false;
        deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        if(mResult != null) {
            deliverResult(mResult);
        }
        int resumeType = mResumeType;
        mResumeType = 0;
        boolean takeContentChanged = takeContentChanged();
        if(resumeType == 1) {
            forceRefresh();
        }else if(resumeType == 2 || takeContentChanged || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        if(mIsLoading) {
            mResumeType = mIsRefresh?1:2;
        }
        cancelLoad();
    }

    @Override
    public void onCanceled(LoaderResult<D> data) {
        if(data != null) {
            D curData = data.getData();
            if(curData != null) {
                onReleaseData(curData);
            }
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        if(mResult != null) {
            D curData = mResult.getData();
            if(curData != null) {
                onReleaseData(curData);
            }
        }
        mResult = null;
    }

    public void addCacheInTransaction(SQLiteDatabase db,String[] deleteSqls,String[] addSqls) {
        if(deleteSqls == null || deleteSqls.length == 0) throw new IllegalArgumentException("delete sqls is necessary.");
        db.beginTransaction();
        try {
            for(String sql:deleteSqls) {
                GenericSQLiteOpenHelper.execSQL(db,sql);
            }
            if(addSqls != null) {
                for(String sql:addSqls) {
                    GenericSQLiteOpenHelper.execSQL(db,sql);
                }
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
    }

    /**
     * <p>注册Observer可能会使LoaderManager.LoaderCallbacks的实现复杂化或出现逻辑问题，故该方法不强制子类实现</>
     * @param data
     * @param observer
     */
    protected void registerContentObserver(D data,ForceLoadContentObserver observer) {
    }

    protected abstract D loadInBackgroundImpl(boolean isRefresh) throws Exception;
    protected abstract void onReleaseData(D data);

}
