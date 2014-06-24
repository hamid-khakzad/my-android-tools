package cn.emagsoftware.ipc;

import android.os.Bundle;

/**
 * Created by Wendell on 13-11-15.
 */
public interface ReplyCallback {

    public void onReplyMessage(Bundle data);
    public void onTimeout();

}
