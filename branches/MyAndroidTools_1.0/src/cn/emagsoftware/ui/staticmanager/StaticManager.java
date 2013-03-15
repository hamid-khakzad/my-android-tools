package cn.emagsoftware.ui.staticmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public final class StaticManager
{

    private static final String QUITINTENT_SIGN_KEY   = "QUITINTENT_SIGN_KEY";
    private static final String QUITINTENT_SIGN_VALUE = "QUITINTENT_SIGN_VALUE";

    private StaticManager()
    {
    }

    /**
     * <p>当虚拟机被系统回收后调用此方法可以清空当前任务的Activity栈并重启应用 <p>调用此方法要保证两个前提： 1.一定要在判断了虚拟机已被回收的情况下调用，可以通过使用一个静态变量来判断回收与否 2.入口Activity必须有且只有一个实例位于Activity栈中，且必须位于栈底
     * 
     * @param context
     * @param launchActivity
     */
    public static void restartAppWhenDalvikRecycled(Context context, Class<? extends Activity> launchActivity)
    {
        Intent intent = new Intent(context, launchActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * <p>当任务的Activity栈中含有多个Activity而需要一次性退出时可以调用此方法清空中间的Activity，并向根Activity发送一个带有退出信息的Intent <p>根Activity一般在onCreate或onNewIntent中通过StaticManager.isQuitIntent(intent)判断是否为退出Intent并作出退出操作
     * <p>调用此方法要保证一个前提： 1.根Activity不能在Activity栈的中间部位存在其他的实例
     * 
     * @param context
     * @param rootActivity
     */
    public static void sendQuitIntentToRootActivity(Context context, Class<? extends Activity> rootActivity)
    {
        Intent intent = new Intent(context, rootActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(QUITINTENT_SIGN_KEY, QUITINTENT_SIGN_VALUE);
        context.startActivity(intent);
    }

    /**
     * <p>判断是否为退出Intent
     * 
     * @param intent
     * @return
     */
    public static boolean isQuitIntent(Intent intent)
    {
        return QUITINTENT_SIGN_VALUE.equals(intent.getStringExtra(QUITINTENT_SIGN_KEY));
    }

}
