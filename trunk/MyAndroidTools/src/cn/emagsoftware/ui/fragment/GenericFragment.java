package cn.emagsoftware.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

/**
 * Created by Wendell on 13-8-26.
 */
public class GenericFragment extends Fragment {

    private boolean isViewDetached = false;
    private BroadcastReceiver refreshReceiver = null;
    private String refreshToken = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            refreshToken = savedInstanceState.getString("genericfragment:support:refreshtoken");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        String refreshType = getRefreshType();
        if(refreshType != null) {
            refreshReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String token = intent.getStringExtra("TOKEN");
                    if(token == null) {
                        return;
                    }
                    if(refreshToken == null) {
                        refreshToken = token;
                        return;
                    }
                    if(!refreshToken.equals(token)) {
                        refreshToken = token;
                        onRefresh(intent.getBundleExtra("DATA"));
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(getActivity().getPackageName() + "@" + refreshType);
            getActivity().registerReceiver(refreshReceiver, intentFilter);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewDetached = true;
        if(refreshReceiver != null) {
            getActivity().unregisterReceiver(refreshReceiver);
            refreshReceiver = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isViewDetached = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(refreshToken != null) {
            outState.putString("genericfragment:support:refreshtoken",refreshToken);
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
     * <p>通过重写该方法以返回刷新类型，返回null表示不需要被通知刷新</>
     * @return
     */
    protected String getRefreshType() {
        return null;
    }

    /**
     * <p>通知刷新的回调方法</>
     * @param bundle
     */
    protected void onRefresh(Bundle bundle) {
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

}
