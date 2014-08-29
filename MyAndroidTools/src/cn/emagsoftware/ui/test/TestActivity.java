package cn.emagsoftware.ui.test;

import cn.emagsoftware.ui.LoaderResult;
import cn.emagsoftware.ui.R;
import cn.emagsoftware.ui.ToastManager;
import cn.emagsoftware.ui.adapterview.DataHolder;
import cn.emagsoftware.ui.adapterview.GenericAdapter;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;

import java.util.List;

public class TestActivity extends ActionBarActivity
{

    private SwipeRefreshLayout swiper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        swiper = (SwipeRefreshLayout)findViewById(R.id.swiper);
        swiper.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        final ListView list = (ListView)findViewById(R.id.list);
        final GenericAdapter adapter = new GenericAdapter(this);
        list.setAdapter(adapter);
        swiper.setRefreshing(true);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Loader loader = getSupportLoaderManager().getLoader(0);
                TestLoader testLoader = (TestLoader)loader;
                if(!testLoader.isLoading()) {
                    testLoader.forceRefresh();
                }
            }
        });
        getSupportLoaderManager().initLoader(0,null,new LoaderManager.LoaderCallbacks<LoaderResult<List<DataHolder>>>() {
            @Override
            public Loader<LoaderResult<List<DataHolder>>> onCreateLoader(int i, Bundle bundle) {
                return new TestLoader(TestActivity.this);
            }
            @Override
            public void onLoadFinished(Loader<LoaderResult<List<DataHolder>>> loaderResultLoader, LoaderResult<List<DataHolder>> listLoaderResult) {
                swiper.setRefreshing(false);
                adapter.clearDataHolders();
                List<DataHolder> data = listLoaderResult.getData();
                if(data != null) {
                    adapter.addDataHolders(data);
                }
                if(listLoaderResult.getException() != null) {
                    ToastManager.showLong(TestActivity.this,"error...");
                }else{
                    list.setSelection(0);
                }
            }
            @Override
            public void onLoaderReset(Loader<LoaderResult<List<DataHolder>>> loaderResultLoader) {
                adapter.clearDataHolders();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        swiper.setRefreshing(savedInstanceState.getBoolean("isRefresh"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRefresh",swiper.isRefreshing());
    }

}
