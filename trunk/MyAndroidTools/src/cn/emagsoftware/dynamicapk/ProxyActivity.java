package cn.emagsoftware.dynamicapk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import cn.emagsoftware.ui.GenericActivity;
import dalvik.system.DexClassLoader;

/**
 * Created by Wendell on 14-6-18.
 */
public class ProxyActivity extends GenericActivity {

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
    protected void onRestoreStaticState() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApkPath = getIntent().getStringExtra(EXTRA_APK_PATH);
        String className = getIntent().getStringExtra(EXTRA_CLASS);
        if(TextUtils.isEmpty(mApkPath)) throw new RuntimeException("the parameter named '" + EXTRA_APK_PATH + "' for ProxyActivity is necessary.");
        loadResources();
        if(className == null) launchRemoteActivity();
        else launchRemoteActivity(className);
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
            throw new RuntimeException(e);
        }
        Resources superRes = super.getResources();
        mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(),
                superRes.getConfiguration());
        mTheme = mResources.newTheme();
        mTheme.setTo(super.getTheme());
    }

    protected void launchRemoteActivity() {
        PackageInfo packageInfo = getPackageManager().getPackageArchiveInfo(mApkPath, 1);
        if(packageInfo.activities == null || packageInfo.activities.length == 0) throw new RuntimeException("apk from '" + mApkPath + "' should have one Activity at least.");
        launchRemoteActivity(packageInfo.activities[0].name);
    }

    protected void launchRemoteActivity(String className) {
        File dexOutputDir = this.getDir("dex", Context.MODE_PRIVATE);
        final String dexOutputPath = dexOutputDir.getAbsolutePath();
        ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(mApkPath,
                dexOutputPath, null, localClassLoader);
        mClassLoader = dexClassLoader;
        try {
            Class<?> localClass = dexClassLoader.loadClass(className);
            Constructor<?> localConstructor = localClass.getConstructor(new Class[] {});
            Object instance = localConstructor.newInstance(new Object[] {});
            if(!(instance instanceof ClientActivity)) throw new RuntimeException("activity in secified apk should extend ClientActivity.");
            setRemoteActivity((Activity)instance);
            instantiateLifecircleMethods(localClass);

            Method setProxy = localClass.getMethod("setProxy", new Class[] { Activity.class, String.class });
            setProxy.setAccessible(true);
            setProxy.invoke(instance, new Object[] { this, mApkPath });

            Method onCreate = mLifecircleMethods.get("onCreate");
            onCreate.invoke(instance, new Object[] { new Bundle() });
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setRemoteActivity(Activity activity) {
        mRemoteActivity = activity;
    }

    protected void instantiateLifecircleMethods(Class<?> localClass) {
        String[] methodNames = new String[] {
                "onStart",
                "onRestart",
                "onResume",
                "onPause",
                "onStop",
                "onDestory"
        };
        for (String methodName : methodNames) {
            Method method = null;
            try {
                method = localClass.getDeclaredMethod(methodName, new Class[] { });
                method.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            mLifecircleMethods.put(methodName, method);
        }

        Method onCreate = null;
        try {
            onCreate = localClass.getDeclaredMethod("onCreate", new Class[] { Bundle.class });
            onCreate.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        mLifecircleMethods.put("onCreate", onCreate);

        Method onActivityResult = null;
        try {
            onActivityResult = localClass.getDeclaredMethod("onActivityResult",
                    new Class[] { int.class, int.class, Intent.class });
            onActivityResult.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        mLifecircleMethods.put("onActivityResult", onActivityResult);
    }

    @Override
    public AssetManager getAssets() {
        return mAssetManager;
    }

    @Override
    public Resources getResources() {
        return mResources;
    }

    @Override
    public Resources.Theme getTheme() {
        return mTheme;
    }

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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
