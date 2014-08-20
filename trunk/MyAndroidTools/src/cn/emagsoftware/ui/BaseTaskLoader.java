package cn.emagsoftware.ui;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by Wendell on 14-8-20.
 */
public abstract class BaseTaskLoader<D> extends AsyncTaskLoader<LoaderResult<D>> {

    private ForceLoadContentObserver mObserver = null;
    private Object mParam = null;
    private LoaderResult<D> mResult = null;

    public BaseTaskLoader(Context context) {
        this(context,null);
    }

    public BaseTaskLoader(Context context,Object param) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mParam = param;
    }

    @Override
    public LoaderResult<D> loadInBackground() {
        D data = null;
        try {
            data = loadInBackgroundImpl(mParam);
        }catch (Exception e) {
            return new LoaderResult<D>(e);
        }
        return new LoaderResult<D>(data);
    }

    @Override
    public void deliverResult(LoaderResult<D> data) {
        if(isReset()) {
            if(data != null && data.getException() == null) {
                D curData = data.getData();
                if(curData != null) {
                    onReleaseData(curData);
                }
            }
            return;
        }
        if(data != null && data.getException() == null) {
            D curData = data.getData();
            if(curData != null) {
                registerContentObserver(curData, mObserver);
            }
        }
        LoaderResult<D> oldResult = mResult;
        mResult = data;
        if(isStarted()) {
            super.deliverResult(data);
        }
        if(oldResult != null && oldResult != data && oldResult.getException() == null) {
            D oldData = oldResult.getData();
            if(oldData != null) {
                onReleaseData(oldData);
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
        if(data != null && data.getException() == null) {
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
        if(mResult != null && mResult.getException() == null) {
            D curData = mResult.getData();
            if(curData != null) {
                onReleaseData(curData);
            }
        }
        mResult = null;
    }

    public abstract D loadInBackgroundImpl(Object param) throws Exception;
    public abstract void onReleaseData(D data);
    public abstract void registerContentObserver(D data,ForceLoadContentObserver observer);

}
