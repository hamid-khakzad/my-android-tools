package cn.emagsoftware.ui;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by Wendell on 14-8-20.
 */
public abstract class BaseTaskLoader<D> extends AsyncTaskLoader<LoaderResult<D>> {

    private ForceLoadContentObserver mObserver = null;
    private Object mParam = null;
    private D mOldData = null;
    private LoaderResult<D> mResult = null;

    public BaseTaskLoader(Context context,D oldData) {
        this(context,oldData,null);
    }

    public BaseTaskLoader(Context context,D oldData,Object param) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mParam = param;
        mOldData = oldData;
    }

    public D getOldData() {
        return mOldData;
    }

    @Override
    public LoaderResult<D> loadInBackground() {
        D data = null;
        try {
            data = loadInBackgroundImpl(mParam);
        }catch (Exception e) {
            return new LoaderResult<D>(e,mOldData == null?null:cloneInBackground(mOldData));
        }finally {
            mOldData = null;
        }
        return new LoaderResult<D>(null,data);
    }

    @Override
    public void deliverResult(LoaderResult<D> data) {
        if(isReset()) {
            if(data != null) {
                D curData = data.getData();
                if(curData != null) {
                    onReleaseData(curData);
                }
            }
            return;
        }
        if(data != null) {
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
        if(oldResult != null && oldResult != data) {
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

    public abstract D loadInBackgroundImpl(Object param) throws Exception;
    public abstract D cloneInBackground(D oldData);
    public abstract void onReleaseData(D data);
    public abstract void registerContentObserver(D data,ForceLoadContentObserver observer);

}
