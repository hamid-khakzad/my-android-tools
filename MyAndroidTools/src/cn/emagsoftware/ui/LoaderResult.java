package cn.emagsoftware.ui;

/**
 * Created by Wendell on 14-8-20.
 */
public class LoaderResult<D> {

    private Exception mException = null;
    private D mData = null;

    public LoaderResult(Exception exception) {
        this.mException = exception;
    }

    public LoaderResult(D data) {
        this.mData = data;
    }

    public Exception getException() {
        return mException;
    }

    public D getData() {
        if(getException() != null) throw new IllegalStateException("current LoaderResult contains exception,can not get data.");
        return mData;
    }

}
