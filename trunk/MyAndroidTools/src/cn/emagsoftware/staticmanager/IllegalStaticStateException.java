package cn.emagsoftware.staticmanager;

/**
 * Created by Wendell on 14-8-14.
 */
public class IllegalStaticStateException extends IllegalStateException {

    public IllegalStaticStateException() {
        super();
    }

    public IllegalStaticStateException(String detailMessage) {
        super(detailMessage);
    }

    public IllegalStaticStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalStaticStateException(Throwable cause) {
        super(cause);
    }

}
