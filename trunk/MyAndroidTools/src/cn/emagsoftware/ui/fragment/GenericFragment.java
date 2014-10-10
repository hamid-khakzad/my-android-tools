package cn.emagsoftware.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import java.lang.reflect.Field;
import java.util.UUID;

import cn.emagsoftware.util.LogManager;

/**
 * Created by Wendell on 13-8-26.
 */
public class GenericFragment extends Fragment {

    private static final Field sChildFragmentManagerField;
    static {
        Field f = null;
        try {
            f = Fragment.class.getDeclaredField("mChildFragmentManager");
            f.setAccessible(true);
        }catch (NoSuchFieldException e) {
            LogManager.logE(GenericFragment.class,"Error getting mChildFragmentManager field",e);
        }
        sChildFragmentManagerField = f;
    }

    private boolean isViewDetached = false;
    private String[] refreshTypes = null;
    private String[] refreshTokens = null;
    private BroadcastReceiver[] refreshReceivers = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            refreshTypes = savedInstanceState.getStringArray("genericfragment:support:refreshtypes");
            refreshTokens = savedInstanceState.getStringArray("genericfragment:support:refreshtokens");
            if(refreshTokens != null) {
                for(int i = 0;i < refreshTokens.length;i++) {
                    if(refreshTokens[i] == null) {
                        refreshTokens[i] = "";
                    }
                }
            }
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
    public void onStart() {
        super.onStart();
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
                intentFilter.addAction(getActivity().getPackageName() + "@" + refreshTypes[i]);
                getActivity().registerReceiver(refreshReceivers[i], intentFilter);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(refreshReceivers != null) {
            Context context = getActivity();
            for(BroadcastReceiver refreshReceiver:refreshReceivers) {
                context.unregisterReceiver(refreshReceiver);
            }
            refreshReceivers = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewDetached = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isViewDetached = false;
        refreshTypes = null;
        refreshTokens = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(sChildFragmentManagerField != null) {
            try {
                sChildFragmentManagerField.set(this, null);
            }catch (Exception e) {
                LogManager.logE(GenericFragment.class,"Error setting mChildFragmentManager field",e);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(refreshTypes != null) {
            outState.putStringArray("genericfragment:support:refreshtypes",refreshTypes);
            outState.putStringArray("genericfragment:support:refreshtokens",refreshTokens);
        }
    }

    /**
     * @deprecated 通过判断findFragmentById或findFragmentByTag的值是否为null来决定是否添加Fragment会更全面
     * @return
     */
    public boolean isViewDetached()
    {
        return isViewDetached;
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

    public void refresh() {
        final int id = getId();
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final String tag = getTag();
        getFragmentManager().beginTransaction().remove(this).commit();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ft.add(id,GenericFragment.this,tag).commit();
            }
        });
    }

}
