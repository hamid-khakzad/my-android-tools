package cn.emagsoftware.ui.pulltorefresh;

import android.view.View;

/**
 * Created by Wendell on 14-11-9.
 */
public interface OnPullListener {

    public void onBeginPull(View pullView,boolean isFirstPull);

    public void onReady(View pullView);

    public void onRefreshing(View pullView);

    public void onCanceled(View pullView);

    public void onScroll(View pullView,float progress,boolean isIncreased);

}
