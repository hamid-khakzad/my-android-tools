package cn.emagsoftware.ui.test;

import cn.emagsoftware.ui.BaseLoaderCallbacks;
import cn.emagsoftware.ui.BaseTaskPageLoader;
import cn.emagsoftware.ui.LoaderResult;
import cn.emagsoftware.ui.R;
import cn.emagsoftware.ui.ToastManager;
import cn.emagsoftware.ui.adapterview.DataHolder;
import cn.emagsoftware.ui.adapterview.GenericAdapter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class TestActivity extends ActionBarActivity
{

    private SwipeRefreshLayout swiper = null;
    private TextView loading = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        swiper = (SwipeRefreshLayout)findViewById(R.id.swiper);
        swiper.setColorSchemeColors(Color.parseColor("#ff33b5e5"),Color.parseColor("#ff99cc00"),Color.parseColor("#ffffbb33"),Color.parseColor("#ffff4444"));
        final ListView list = (ListView)findViewById(R.id.list);
        final GenericAdapter adapter = new GenericAdapter(this);
        View temp = new View(this);
        list.addFooterView(temp); // 兼容Android 2.x，Android 2.x第一次调用addFooterView必须在setAdapter之前
        list.setAdapter(adapter);
        list.removeFooterView(temp);
        swiper.setRefreshing(true);
        if(savedInstanceState == null) {
            swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Loader loader = getSupportLoaderManager().getLoader(0);
                    TestLoader testLoader = (TestLoader)loader;
                    testLoader.forceRefresh();
                }
            });
        }
        getSupportLoaderManager().initLoader(0,null,new BaseLoaderCallbacks<List<DataHolder>>() {
            @Override
            public Loader<LoaderResult<List<DataHolder>>> onCreateLoader(int i, Bundle bundle) {
                return new TestLoader(TestActivity.this);
            }
            @Override
            protected void onLoadFinished(Loader<LoaderResult<List<DataHolder>>> loader, List<DataHolder> result, Exception e, boolean isNew, boolean isRefresh) {
                swiper.setRefreshing(false);
                adapter.setDataHolders(result);
                TestLoader testLoader = (TestLoader)loader;
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

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        swiper.setRefreshing(savedInstanceState.getBoolean("isRefresh"));
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Loader loader = getSupportLoaderManager().getLoader(0);
                TestLoader testLoader = (TestLoader)loader;
                testLoader.forceRefresh();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRefresh",swiper.isRefreshing());
    }

}
