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

    private List<DataHolder> mOldData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        getSupportActionBar().setTitle("MyAndroidTools");
        final SwipeRefreshLayout swiper = (SwipeRefreshLayout)findViewById(R.id.swiper);
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
                getSupportLoaderManager().restartLoader(0,null,new LoaderManager.LoaderCallbacks<LoaderResult<List<DataHolder>>>() {
                    @Override
                    public Loader<LoaderResult<List<DataHolder>>> onCreateLoader(int i, Bundle bundle) {
                        return new TestLoader(TestActivity.this,mOldData);
                    }
                    @Override
                    public void onLoadFinished(Loader<LoaderResult<List<DataHolder>>> loaderResultLoader, LoaderResult<List<DataHolder>> listLoaderResult) {
                        mOldData = listLoaderResult.getData();
                        swiper.setRefreshing(false);
                        adapter.clearDataHolders();
                        if(mOldData != null) {
                            adapter.addDataHolders(mOldData);
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
        });
        TestLoader loader = (TestLoader)getSupportLoaderManager().initLoader(0,null,new LoaderManager.LoaderCallbacks<LoaderResult<List<DataHolder>>>() {
            @Override
            public Loader<LoaderResult<List<DataHolder>>> onCreateLoader(int i, Bundle bundle) {
                return new TestLoader(TestActivity.this,mOldData);
            }
            @Override
            public void onLoadFinished(Loader<LoaderResult<List<DataHolder>>> loaderResultLoader, LoaderResult<List<DataHolder>> listLoaderResult) {
                mOldData = listLoaderResult.getData();
                swiper.setRefreshing(false);
                adapter.clearDataHolders();
                if(mOldData != null) {
                    adapter.addDataHolders(mOldData);
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
        List<DataHolder> oldHolders = loader.getOldData();
        if(oldHolders != null) {
            adapter.addDataHolders(oldHolders);
            mOldData = oldHolders;
        }
    }
    
}
