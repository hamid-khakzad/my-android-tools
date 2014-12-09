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
import java.util.LinkedList;
import java.util.List;

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
    private boolean isResumed = false;
    private BroadcastReceiver[] refreshReceivers = null;
    private List<Object[]> cacheRefresh = new LinkedList<Object[]>();
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
                intentFilter.addAction(getActivity().getPackageName() + "@" + refreshTypes[i]);
                getActivity().registerReceiver(refreshReceivers[i], intentFilter);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
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
    public void onPause() {
        super.onPause();
        isResumed = false;
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
        if(refreshReceivers != null) {
            Context context = getActivity();
            for(BroadcastReceiver refreshReceiver:refreshReceivers) {
                context.unregisterReceiver(refreshReceiver);
            }
            refreshReceivers = null;
        }
        cacheRefresh.clear();
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

    /**
     * @deprecated ͨ���ж�findFragmentById��findFragmentByTag��ֵ�Ƿ�Ϊnull�������Ƿ����Fragment���ȫ��
     * @return
     */
    public boolean isViewDetached()
    {
        return isViewDetached;
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
