package cn.emagsoftware.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import java.util.UUID;

import cn.emagsoftware.telephony.TelephonyMgr;

/**
 * Created by Wendell on 14-10-29.
 */
public class GenericActivity extends Activity {

    private String[] refreshTypes = null;
    private String[] refreshTokens = null;
    private BroadcastReceiver[] refreshReceivers = null;
    private boolean isResumed = false;
    private boolean shouldRecreateNextTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            refreshTypes = savedInstanceState.getStringArray("genericactivity:support:refreshtypes");
            refreshTokens = savedInstanceState.getStringArray("genericactivity:support:refreshtokens");
        }
        if(refreshTypes == null) {
            refreshTypes = getRefreshTypes();
            if(refreshTypes != null) {
                refreshTypes = refreshTypes.clone();
                refreshTokens = new String[refreshTypes.length];
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(refreshTypes != null) {
            refreshReceivers = new BroadcastReceiver[refreshTypes.length];
            for(int i = 0;i < refreshTypes.length;i++) {
                final int index = i;
                refreshReceivers[i] = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String token = intent.getStringExtra("TOKEN");
                        if(token == null || token.equals("")) {
                            return;
                        }
                        if(refreshTokens[index] == null && isInitialStickyBroadcast()) {
                            refreshTokens[index] = token;
                            return;
                        }
                        if(!token.equals(refreshTokens[index])) {
                            refreshTokens[index] = token;
                            onRefresh(refreshTypes[index],intent.getBundleExtra("DATA"));
                        }
                    }
                };
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(getPackageName() + "@" + refreshTypes[i]);
                registerReceiver(refreshReceivers[i], intentFilter);
            }
        }
        isResumed = true;
        if(shouldRecreateNextTime) {
            shouldRecreateNextTime = false;
            recreateMeImmediately();
            return;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        clearReceivers();
        if(refreshTypes != null) {
            outState.putStringArray("genericactivity:support:refreshtypes",refreshTypes);
            outState.putStringArray("genericactivity:support:refreshtokens",refreshTokens);
        }
    }

    private void clearReceivers() {
        if(refreshReceivers != null) {
            for(BroadcastReceiver refreshReceiver:refreshReceivers) {
                unregisterReceiver(refreshReceiver);
            }
            refreshReceivers = null;
            for(int i = 0;i < refreshTokens.length;i++) {
                if(refreshTokens[i] == null) {
                    refreshTokens[i] = "";
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearReceivers();
        isResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refreshTypes = null;
        refreshTokens = null;
    }

    /**
     * <p>通过重写该方法以返回所有刷新类型，返回null表示不需要被通知刷新</>
     * @return
     */
    protected String[] getRefreshTypes() {
        return null;
    }

    /**
     * <p>通知刷新的回调方法</>
     * @param refreshType
     * @param bundle
     */
    protected void onRefresh(String refreshType,Bundle bundle) {
    }

    /**
     * <p>发送刷新通知</>
     * @param context
     * @param refreshType
     * @param bundle
     */
    public static void sendRefresh(Context context,String refreshType,Bundle bundle) {
        Intent intent = new Intent(context.getPackageName() + "@" + refreshType);
        intent.putExtra("TOKEN", UUID.randomUUID().toString());
        if(bundle != null) {
            intent.putExtra("DATA",bundle);
        }
        context.sendStickyBroadcast(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void recreate() {
        if(TelephonyMgr.getSDKVersion() >= Build.VERSION_CODES.HONEYCOMB) {
            super.recreate();
        }else {
            if(isResumed) recreateMeImmediately();
            else shouldRecreateNextTime = true;
        }
    }

    private void recreateMeImmediately() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

}
