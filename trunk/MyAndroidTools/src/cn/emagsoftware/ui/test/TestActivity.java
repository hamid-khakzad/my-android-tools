package cn.emagsoftware.ui.test;

import cn.emagsoftware.ui.BaseLoaderCallbacks;
import cn.emagsoftware.ui.BaseTaskPageLoader;
import cn.emagsoftware.ui.GenericActionBarActivity;
import cn.emagsoftware.ui.LoaderResult;
import cn.emagsoftware.ui.R;
import cn.emagsoftware.ui.ToastManager;
import cn.emagsoftware.ui.adapterview.DataHolder;
import cn.emagsoftware.ui.adapterview.GenericAdapter;
import cn.emagsoftware.ui.pulltorefresh.OnPullListener;
import cn.emagsoftware.ui.pulltorefresh.PullListView;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.List;

public class TestActivity extends GenericActionBarActivity
{

    private TextView loading = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        final PullListView list = (PullListView)findViewById(R.id.list);
        final GenericAdapter adapter = new GenericAdapter(this);
        TextView pullView = new TextView(this);
        pullView.setText("Refresh...");
        pullView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        pullView.setPadding(0,0,0,35);
        pullView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,130));
        list.addPullView(pullView);
        View temp = new View(this);
        list.addFooterView(temp); // 兼容Android 2.x，Android 2.x第一次调用addFooterView必须在setAdapter之前
        list.setAdapter(adapter);
        list.removeFooterView(temp);
        list.setRefreshing(true);
        list.setOnPullListener(new OnPullListener() {
            @Override
            public void onBeginPull(View pullView) {
                ((TextView)pullView).setText("Pull to refresh...");
            }
            @Override
            public void onReady(View pullView) {
                ((TextView)pullView).setText("Release to refresh...");
            }
            @Override
            public void onRefreshing(View pullView) {
                ((TextView)pullView).setText("Refresh...");
                Loader loader = getSupportLoaderManager().getLoader(0);
                TestLoader testLoader = (TestLoader) loader;
                testLoader.forceRefresh();
            }
            @Override
            public void onCanceled(View pullView) {
            }
            @Override
            public void onScroll(View pullView, float progress, boolean isIncreased) {
            }
        });
        getSupportLoaderManager().initLoader(0,null,new BaseLoaderCallbacks<List<DataHolder>>() {
            @Override
            public Loader<LoaderResult<List<DataHolder>>> onCreateLoader(int i, Bundle bundle) {
                return new TestLoader(TestActivity.this);
            }
            @Override
            protected void onLoadFinished(Loader<LoaderResult<List<DataHolder>>> loader, List<DataHolder> result, Exception e, boolean isNew, boolean isRefresh) {
                TestLoader testLoader = (TestLoader)loader;
                if(!testLoader.isRefreshing()) { // 可能存在刷新的情况
                    list.setRefreshing(false);
                }
                adapter.setDataHolders(result);
                // 添加/删除Footer
                if(testLoader.isLoadedAll()) {
                    if(loading != null) {
                        list.removeFooterView(loading);
                        loading = null;
                    }
                }else {
                    if(loading == null) {
                        loading = new TextView(TestActivity.this);
                        loading.setGravity(Gravity.CENTER);
                        loading.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,100));
                        list.addFooterView(loading,null,false);
                    }
                    loading.setText("loading...");
                }
                // 处理异常
                if(e != null) {
                    if(isRefresh) {
                        if(isNew) {
                            ToastManager.showLong(TestActivity.this,"error.");
                        }
                    }else {
                        if(loading != null) {
                            loading.setText("error.");
                        }
                    }
                }
                // 针对刷新成功的处理
                if(isRefresh && isNew && e == null) {
                    list.setSelection(0);
                }
            }
            @Override
            public void onLoaderReset(Loader<LoaderResult<List<DataHolder>>> loaderResultLoader) {
                adapter.setDataHolders(null);
            }
        });
        BaseTaskPageLoader.bindPageLoading(list,new BaseTaskPageLoader.OnPageLoading() {
            @Override
            public void onPageLoading(AdapterView<? extends Adapter> adapterView) {
                Loader loader = getSupportLoaderManager().getLoader(0);
                TestLoader testLoader = (TestLoader)loader;
                if(!testLoader.isLoading() && !testLoader.isLoadedAll() && !testLoader.isPageException()) {
                    testLoader.forcePageLoad();
                }
            }
        });
    }

}
