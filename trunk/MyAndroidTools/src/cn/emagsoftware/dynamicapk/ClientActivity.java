package cn.emagsoftware.dynamicapk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import cn.emagsoftware.ui.GenericActivity;

/**
 * Created by Wendell on 14-6-18.
 */
public class ClientActivity extends GenericActivity {

    private static final String TAG = "ClientActivity";

    protected Activity mProxyActivity;
    protected String mApkPath;

    @Override
    protected void onRestoreStaticState() {
    }

    private void setProxy(Activity proxyActivity, String apkPath) {
        if(proxyActivity == null || apkPath == null) throw new NullPointerException();
        mProxyActivity = proxyActivity;
        mApkPath = apkPath;
    }

    public Activity getProxyActivity() {
        checkingProxy();
        return mProxyActivity;
    }

    private void checkingProxy() {
        if(mProxyActivity == null || mApkPath == null) throw new IllegalStateException("ClientActivity should only be launched by ProxyActivity.");
    }

    @Override
    public void setContentView(View view) {
        checkingProxy();
        mProxyActivity.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        checkingProxy();
        mProxyActivity.setContentView(view, params);
    }

    @Override
    public void setContentView(int layoutResID) {
        checkingProxy();
        mProxyActivity.setContentView(layoutResID);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        checkingProxy();
        mProxyActivity.addContentView(view, params);
    }

    @Override
    public View findViewById(int id) {
        checkingProxy();
        return mProxyActivity.findViewById(id);
    }

    @Override
    public void startActivity(Intent intent) {
        checkingProxy();
        mProxyActivity.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        checkingProxy();
        mProxyActivity.startActivityForResult(intent, requestCode);
    }

    public void startActivityByProxy(Intent intent) {
        checkingProxy();
        intent = createProxyIntent(intent);
        mProxyActivity.startActivity(intent);
    }

    public void startActivityForResultByProxy(Intent intent, int requestCode) {
        checkingProxy();
        intent = createProxyIntent(intent);
        mProxyActivity.startActivityForResult(intent, requestCode);
    }

    private Intent createProxyIntent(Intent intent) {
        intent = new Intent(intent);
        ComponentName componentName = intent.getComponent();
        if(componentName == null) throw new IllegalStateException("intent should set className explicitly.");
        intent.setClass(mProxyActivity, ProxyActivity.class);
        intent.setAction(null);
        intent.putExtra(ProxyActivity.EXTRA_APK_PATH, mApkPath);
        intent.putExtra(ProxyActivity.EXTRA_CLASS, componentName.getClassName());
        return intent;
    }

    @Override
    public void finish() {
        checkingProxy();
        mProxyActivity.finish();
    }

    @Override
    public Intent getIntent() {
        checkingProxy();
        return mProxyActivity.getIntent();
    }

}
