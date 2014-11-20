package cn.emagsoftware.ui;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Created by Wendell on 14-9-9.
 */
public abstract class BaseLoaderCallbacks<D> implements LoaderManager.LoaderCallbacks<LoaderResult<D>> {

    @Override
    public final void onLoadFinished(Loader<LoaderResult<D>> loaderResultLoader, LoaderResult<D> dLoaderResult) {
        if(dLoaderResult == null) {
            onLoadSuccess(loaderResultLoader,null,false);
        }else {
            boolean isNew = dLoaderResult.mIsNew;
            dLoaderResult.mIsNew = false;
            D result = dLoaderResult.getData();
            Exception e = dLoaderResult.getException();
            if(isNew) {
                if(e == null) onLoadSuccess(loaderResultLoader,result,dLoaderResult.mIsRefresh);
                else onLoadFailure(loaderResultLoader,e,dLoaderResult.mIsRefresh);
            }else {
                if(e == null || dLoaderResult.mIsRefresh) onLoadSuccess(loaderResultLoader,result,false);
                else {
                    if(result != null) onLoadSuccess(loaderResultLoader,result,false);
                    onLoadFailure(loaderResultLoader,e,false);
                }
            }
        }
    }

    protected abstract void onLoadSuccess(Loader<LoaderResult<D>> loader,D result,boolean isRefresh);

    protected abstract void onLoadFailure(Loader<LoaderResult<D>> loader,Exception e,boolean isRefresh);

    @Override
    public void onLoaderReset(Loader<LoaderResult<D>> loaderResultLoader) {
    }

}
