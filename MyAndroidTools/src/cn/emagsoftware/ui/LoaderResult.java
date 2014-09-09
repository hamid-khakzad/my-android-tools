package cn.emagsoftware.ui;

/**
 * Created by Wendell on 14-8-20.
 */
public class LoaderResult<D> {

    private Exception mException = null;
    private D mData = null;
    boolean mIsNew = true;

    public LoaderResult(Exception exception,D data) {
        this.mException = exception;
        this.mData = data;
    }

    public Exception getException() {
        return mException;
    }

    public D getData() {
        return mData;
    }

}
