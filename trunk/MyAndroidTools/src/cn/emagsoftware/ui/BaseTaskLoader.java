package cn.emagsoftware.ui;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by Wendell on 14-8-20.
 */
public abstract class BaseTaskLoader<D> extends AsyncTaskLoader<LoaderResult<D>> {

    private ForceLoadContentObserver mObserver = null;
    private D mOldData = null;
    protected LoaderResult<D> mResult = null;

    public BaseTaskLoader(Context context,D oldData) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mOldData = oldData;
    }

    public D getOldData() {
        return mOldData;
    }

    @Override
    public final LoaderResult<D> loadInBackground() {
        D data = null;
        try {
            data = loadInBackgroundImpl();
        }catch (Exception e) {
            D eData;
            if(mResult == null) {
                eData = mOldData == null?null:cloneInBackground(mOldData);
            }else {
                eData = mResult.getData();
            }
            return new LoaderResult<D>(e,eData);
        }finally {
            mOldData = null;
        }
        return new LoaderResult<D>(null,data);
    }

    @Override
    public void deliverResult(LoaderResult<D> data) {
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

    @Override
    protected void onStartLoading() {
        if(mResult != null) {
            deliverResult(mResult);
        }
        if(takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
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

    public abstract D loadInBackgroundImpl() throws Exception;
    public abstract D cloneInBackground(D oldData);
    public abstract void onReleaseData(D data);
    public abstract void registerContentObserver(D data,ForceLoadContentObserver observer);

}
