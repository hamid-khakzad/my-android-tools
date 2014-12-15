package cn.emagsoftware.dynamicapk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

/**
 * Created by Wendell on 14-6-18.
 */
public class ProxyActivity extends Activity {

    private static final String TAG = "ProxyActivity";

    public static final String EXTRA_APK_PATH = "EXTRA_APK_PATH";
    public static final String EXTRA_CLASS = "EXTRA_CLASS";

    private String mApkPath;

    private AssetManager mAssetManager;
    private Resources mResources;
    private Resources.Theme mTheme;
    private ClassLoader mClassLoader;

    private Activity mRemoteActivity;
    private HashMap<String, Method> mLifecircleMethods = new HashMap<String, Method>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApkPath = getIntent().getStringExtra(EXTRA_APK_PATH);
        String className = getIntent().getStringExtra(EXTRA_CLASS);
        if(TextUtils.isEmpty(mApkPath)) throw new RuntimeException("the parameter named '" + EXTRA_APK_PATH + "' for ProxyActivity is necessary.");
        loadResources();
        if(className == null) launchRemoteActivity(savedInstanceState);
        else launchRemoteActivity(savedInstanceState, className);
    }

    protected void loadResources() {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, mApkPath);
            mAssetManager = assetManager;
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
        Resources superRes = super.getResources();
        mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(),
                superRes.getConfiguration());
        mTheme = mResources.newTheme();
        mTheme.setTo(super.getTheme());
    }

    protected void launchRemoteActivity(Bundle savedInstanceState) {
        PackageInfo packageInfo = getPackageManager().getPackageArchiveInfo(mApkPath, 1);
        if(packageInfo == null) throw new RuntimeException("apk from '" + mApkPath + "' is invalid or can not be launched.");
        if(packageInfo.activities == null || packageInfo.activities.length == 0) throw new RuntimeException("apk from '" + mApkPath + "' should have one Activity at least.");
        launchRemoteActivity(savedInstanceState, packageInfo.activities[0].name);
    }

    protected void launchRemoteActivity(Bundle savedInstanceState, String className) {
        File dexOutputDir = this.getDir("dex", Context.MODE_PRIVATE);
        final String dexOutputPath = dexOutputDir.getAbsolutePath();
        DexClassLoader dexClassLoader = new DexClassLoader(mApkPath,
                dexOutputPath, null, getClassLoader());
        mClassLoader = dexClassLoader;
        try {
            Class<?> localClass = dexClassLoader.loadClass(className);
            Constructor<?> localConstructor = localClass.getConstructor(new Class[] {});
            Object instance = localConstructor.newInstance(new Object[] {});
            if(!(instance instanceof Activity)) throw new RuntimeException("activity in secified apk should extend ClientActivity.");
            setRemoteActivity((Activity)instance);
            instantiateLifecircleMethods();
            Method setProxy;
            try {
                setProxy = localClass.getMethod("setProxy", new Class[] { Activity.class, String.class });
            }catch (NoSuchMethodException e) {
                throw new RuntimeException("activity in secified apk should extend ClientActivity.", e);
            }
            setProxy.setAccessible(true);
            setProxy.invoke(instance, new Object[] { this, mApkPath });

            Method onCreate = mLifecircleMethods.get("onCreate");
            onCreate.invoke(instance, new Object[] { savedInstanceState });
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
    }

    protected void setRemoteActivity(Activity activity) {
        mRemoteActivity = activity;
    }

    protected void instantiateLifecircleMethods() {
        String[] methodNames = new String[] {
                "onStart",
                "onRestart",
                "onResume",
                "onPause",
                "onStop",
                "onDestroy"
        };
        for (String methodName : methodNames) {
            Method method = null;
            try {
                method = Activity.class.getDeclaredMethod(methodName, new Class[]{});
                method.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            mLifecircleMethods.put(methodName, method);
        }

        Method onCreate = null;
        try {
            onCreate = Activity.class.getDeclaredMethod("onCreate", new Class[]{Bundle.class});
            onCreate.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        mLifecircleMethods.put("onCreate", onCreate);

        Method onActivityResult = null;
        try {
            onActivityResult = Activity.class.getDeclaredMethod("onActivityResult",
                    new Class[]{int.class, int.class, Intent.class});
            onActivityResult.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        mLifecircleMethods.put("onActivityResult", onActivityResult);

        Method onCreateOptionsMenu = null;
        try {
            onCreateOptionsMenu = Activity.class.getDeclaredMethod("onCreateOptionsMenu",
                    new Class[] { Menu.class });
            onCreateOptionsMenu.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        mLifecircleMethods.put("onCreateOptionsMenu", onCreateOptionsMenu);

        Method onOptionsItemSelected = null;
        try {
            onOptionsItemSelected = Activity.class.getDeclaredMethod("onOptionsItemSelected",
                    new Class[] { MenuItem.class });
            onOptionsItemSelected.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        mLifecircleMethods.put("onOptionsItemSelected", onOptionsItemSelected);
    }

    @Override
    public AssetManager getAssets() {
        return mAssetManager == null ? super.getAssets() : mAssetManager;
    }

    @Override
    public Resources getResources() {
        return mResources == null ? super.getResources() : mResources;
    }

    /*@Override
    public Resources.Theme getTheme() {
        return mTheme == null ? super.getTheme() : mTheme;
    }*/

    @Override
    public ClassLoader getClassLoader() {
        ClassLoader classLoader = new ClassLoader(super.getClassLoader()) {
            @Override
            public Class<?> loadClass(String className) throws ClassNotFoundException {
                Class<?> clazz = null;
                clazz = mClassLoader.loadClass(className);
                if (clazz == null) {
                    clazz = getParent().loadClass(className);
                }
                // still not found
                if (clazz == null) {
                    throw new ClassNotFoundException(className);
                }

                return clazz;
            }
        };

        return classLoader;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Method onStart = mLifecircleMethods.get("onStart");
        try {
            onStart.invoke(mRemoteActivity, new Object[] { });
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Method onRestart = mLifecircleMethods.get("onRestart");
        try {
            onRestart.invoke(mRemoteActivity, new Object[] { });
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Method onResume = mLifecircleMethods.get("onResume");
        try {
            onResume.invoke(mRemoteActivity, new Object[] { });
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
    }

    @Override
    protected void onPause() {
        Method onPause = mLifecircleMethods.get("onPause");
        try {
            onPause.invoke(mRemoteActivity, new Object[] { });
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        Method onStop = mLifecircleMethods.get("onStop");
        try {
            onStop.invoke(mRemoteActivity, new Object[] { });
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Method onDestroy = mLifecircleMethods.get("onDestroy");
        try {
            onDestroy.invoke(mRemoteActivity, new Object[] { });
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Method onActivityResult = mLifecircleMethods.get("onActivityResult");
        try {
            onActivityResult.invoke(mRemoteActivity, new Object[] { requestCode, resultCode, data });
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Method onCreateOptionsMenu = mLifecircleMethods.get("onCreateOptionsMenu");
        try {
            return (Boolean)onCreateOptionsMenu.invoke(mRemoteActivity, new Object[] { menu });
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Method onOptionsItemSelected = mLifecircleMethods.get("onOptionsItemSelected");
        try {
            return (Boolean)onOptionsItemSelected.invoke(mRemoteActivity, new Object[] { item });
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException) throw (RuntimeException)cause;
            else throw new RuntimeException(cause);
        }
    }

}
