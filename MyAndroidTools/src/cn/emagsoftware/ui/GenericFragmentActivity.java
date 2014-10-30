package cn.emagsoftware.ui;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import cn.emagsoftware.telephony.TelephonyMgr;

/**
 * Created by Wendell on 14-10-29.
 */
public class GenericFragmentActivity extends FragmentActivity {

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
                            dispatchRefresh(refreshTypes[index],intent.getBundleExtra("DATA"));
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
     * <p>ͨ����д�÷����Է�������ˢ�����ͣ�����null��ʾ����Ҫ��֪ͨˢ��</>
     * @return
     */
    protected String[] getRefreshTypes() {
        return null;
    }

    protected void dispatchRefresh(String refreshType,Bundle bundle) {
        onRefresh(refreshType,bundle);
    }

    /**
     * <p>֪ͨˢ�µĻص�����</>
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
