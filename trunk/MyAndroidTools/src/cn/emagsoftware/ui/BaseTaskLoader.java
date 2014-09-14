package cn.emagsoftware.ui;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by Wendell on 14-8-20.
 */
public abstract class BaseTaskLoader<D> extends AsyncTaskLoader<LoaderResult<D>> {

    private ForceLoadContentObserver mObserver = null;
    private boolean mIsRefresh = false;
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
    }

    public void forceRefresh() {
        mIsRefresh = true;
        super.forceLoad();
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        mIsLoading = true;
    }

    @Override
    public boolean cancelLoad() {
        boolean returnVal = super.cancelLoad();
        mIsLoading = false;
        return returnVal;
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
                data = new LoaderResult<D>(exception,mResult==null?null:mResult.getData());
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

    protected abstract D loadInBackgroundImpl(boolean isRefresh) throws Exception;
    protected abstract void onReleaseData(D data);
    protected abstract void registerContentObserver(D data,ForceLoadContentObserver observer);

}
