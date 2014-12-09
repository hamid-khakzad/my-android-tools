package cn.emagsoftware.ui;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import java.util.LinkedList;
import java.util.List;

import cn.emagsoftware.telephony.TelephonyMgr;

/**
 * Created by Wendell on 14-10-29.
 */
public class GenericFragmentActivity extends FragmentActivity {

    private boolean isResumed = false;
    private BroadcastReceiver[] refreshReceivers = null;
    private List<Object[]> cacheRefresh = new LinkedList<Object[]>();
    private Handler handler = new Handler();
    private boolean shouldRefreshNextTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String[] refreshTypes = getRefreshTypes();
        if(refreshTypes != null) {
            refreshReceivers = new BroadcastReceiver[refreshTypes.length];
            for(int i = 0;i < refreshTypes.length;i++) {
                final int index = i;
                refreshReceivers[i] = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle data = intent.getBundleExtra("DATA");
                        if(isResumed) dispatchRefresh(refreshTypes[index],data);
                        else cacheRefresh.add(new Object[]{refreshTypes[index],data});
                    }
                };
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(getPackageName() + "@" + refreshTypes[i]);
                registerReceiver(refreshReceivers[i], intentFilter);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        if(shouldRefreshNextTime) {
            shouldRefreshNextTime = false;
            refreshImmediately();
            return;
        }
        if(cacheRefresh.size() > 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for(Object[] refresh : cacheRefresh) {
                        dispatchRefresh((String)refresh[0],(Bundle)refresh[1]);
                    }
                    cacheRefresh.clear();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(refreshReceivers != null) {
            for(BroadcastReceiver refreshReceiver:refreshReceivers) {
                unregisterReceiver(refreshReceiver);
            }
            refreshReceivers = null;
        }
        cacheRefresh.clear();
    }

    /**
     * <p>通过重写该方法以返回所有刷新类型，返回null表示不需要被通知刷新</>
     * @return
     */
    protected String[] getRefreshTypes() {
        return null;
    }

    protected void dispatchRefresh(String refreshType,Bundle bundle) {
        onRefresh(refreshType,bundle);
    }

    /**
     * <p>通知刷新的回调方法</>
     * @param refreshType
     * @param bundle
     */
    protected void onRefresh(String refreshType,Bundle bundle) {
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void recreate() {
        if(TelephonyMgr.getSDKVersion() >= Build.VERSION_CODES.HONEYCOMB) {
            super.recreate();
        }else {
            refresh();
        }
    }

    public void refresh() {
        if(isResumed) refreshImmediately();
        else shouldRefreshNextTime = true;
    }

    private void refreshImmediately() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

}
