package cn.emagsoftware.dynamicapk;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Wendell on 14-6-18.
 */
public final class DynamicApkManager {

    private DynamicApkManager(){}

    public static void launchApk(Context context, String apkPath) {
        Intent intent = new Intent(context, ProxyActivity.class);
        intent.putExtra(ProxyActivity.EXTRA_APK_PATH, apkPath);
        context.startActivity(intent);
    }

    public static void launchApk(Context context, Intent intent, String apkPath) {
        intent = new Intent(intent);
        intent.setClass(context, ProxyActivity.class);
        intent.setAction(null);
        intent.putExtra(ProxyActivity.EXTRA_APK_PATH, apkPath);
        context.startActivity(intent);
    }

}
