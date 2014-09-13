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
            onLoadFinished(loaderResultLoader,null,null,true,false);
        }else {
            boolean isNew = dLoaderResult.mIsNew;
            dLoaderResult.mIsNew = false;
            onLoadFinished(loaderResultLoader,dLoaderResult.getData(),dLoaderResult.getException(),isNew,dLoaderResult.mIsRefresh);
        }
    }

    protected abstract void onLoadFinished(Loader<LoaderResult<D>> loader,D result,Exception e,boolean isNew,boolean isRefresh);

}
