package cn.emagsoftware.dynamicapk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Wendell on 14-6-18.
 */
public class ClientActivity extends Activity {

    private static final String TAG = "ClientActivity";

    protected Activity mProxyActivity = this;
    protected String mApkPath;

    public final void setProxy(Activity proxyActivity, String apkPath) {
        if(proxyActivity == null || apkPath == null) throw new NullPointerException();
        mProxyActivity = proxyActivity;
        mApkPath = apkPath;
    }

    public Activity getProxyActivity() {
        return mProxyActivity;
    }

    @Override
    protected void onStart() {
        if(mProxyActivity == this) {
            super.onStart();
        }
    }

    @Override
    protected void onRestart() {
        if(mProxyActivity == this) {
            super.onRestart();
        }
    }

    @Override
    protected void onResume() {
        if(mProxyActivity == this) {
            super.onResume();
        }
    }

    @Override
    protected void onPause() {
        if(mProxyActivity == this) {
            super.onPause();
        }
    }

    @Override
    protected void onStop() {
        if(mProxyActivity == this) {
            super.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        if(mProxyActivity == this) {
            super.onDestroy();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(mProxyActivity == this) {
            super.onCreate(savedInstanceState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(mProxyActivity == this) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mProxyActivity == this) {
            return super.onCreateOptionsMenu(menu);
        }else {
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mProxyActivity == this) {
            return super.onOptionsItemSelected(item);
        }else {
            return false;
        }
    }

    @Override
    public void setContentView(View view) {
        if(mProxyActivity == this) {
            super.setContentView(view);
        }else {
            mProxyActivity.setContentView(view);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if(mProxyActivity == this) {
            super.setContentView(view, params);
        }else {
            mProxyActivity.setContentView(view, params);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        if(mProxyActivity == this) {
            super.setContentView(layoutResID);
        }else {
            mProxyActivity.setContentView(layoutResID);
        }
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        if(mProxyActivity == this) {
            super.addContentView(view, params);
        }else {
            mProxyActivity.addContentView(view, params);
        }
    }

    @Override
    public View findViewById(int id) {
        if(mProxyActivity == this) {
            return super.findViewById(id);
        }else {
            return mProxyActivity.findViewById(id);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if(mProxyActivity == this) {
            super.startActivity(intent);
        }else {
            mProxyActivity.startActivity(intent);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if(mProxyActivity == this) {
            super.startActivityForResult(intent, requestCode);
        }else {
            mProxyActivity.startActivityForResult(intent, requestCode);
        }
    }

    public void startActivityByProxy(Intent intent) {
        if(mProxyActivity == this) {
            startActivity(intent);
        }else {
            intent = createProxyIntent(intent);
            mProxyActivity.startActivity(intent);
        }
    }

    public void startActivityForResultByProxy(Intent intent, int requestCode) {
        if(mProxyActivity == this) {
            startActivityForResult(intent, requestCode);
        }else {
            intent = createProxyIntent(intent);
            mProxyActivity.startActivityForResult(intent, requestCode);
        }
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
        if(mProxyActivity == this) {
            super.finish();
        }else {
            mProxyActivity.finish();
        }
    }

    @Override
    public Intent getIntent() {
        if(mProxyActivity == this) {
            return super.getIntent();
        }else {
            return mProxyActivity.getIntent();
        }
    }

    @Override
    public AssetManager getAssets() {
        if(mProxyActivity == this) {
            return super.getAssets();
        }else {
            return mProxyActivity.getAssets();
        }
    }

    @Override
    public Resources getResources() {
        if(mProxyActivity == this) {
            return super.getResources();
        }else {
            return mProxyActivity.getResources();
        }
    }

    @Override
    public String getPackageName() {
        if(mProxyActivity == this) {
            return super.getPackageName();
        }else {
            return mProxyActivity.getPackageName();
        }
    }

    @Override
    public Object getSystemService(String name) {
        if(mProxyActivity == this) {
            return super.getSystemService(name);
        }else {
            return mProxyActivity.getSystemService(name);
        }
    }

    @Override
    public Context getApplicationContext() {
        if(mProxyActivity == this) {
            return super.getApplicationContext();
        }else {
            return mProxyActivity.getApplicationContext();
        }
    }

    public final Drawable getDrawableFromAssets(String fileName) {
        InputStream input = null;
        try {
            input = getAssets().open(fileName);
            return Drawable.createFromResourceStream(getResources(), null, input, new File(fileName).getName());
        }catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                if(input != null) input.close();
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
