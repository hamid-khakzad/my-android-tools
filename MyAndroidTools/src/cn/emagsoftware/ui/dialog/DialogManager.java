package cn.emagsoftware.ui.dialog;

import java.lang.reflect.Field;

import android.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.View;

/**
 * <p>对话框管理类
 * 
 * @author Wendell
 * @version 1.6
 * 
 */
public abstract class DialogManager
{

    /**
     * <p>默认情况下，对话框利用了与当前方法相同的原理将程序定义的主题样式替换成自己的R.style.Theme_Dialog，这就是对话框没有显示程序定义样式的原因。 但若是外部创建了View（如通过AlertDialog.Builder或手工创建）再添加进对话框，则这些View将使用程序定义的样式，从而与对话框样式不一致。
     * <b>若要使外部创建的View也拥有对话框的默认样式，在构造View时，可调用当前方法转换Context后再传入View的构造函数，便会使View获得与对话框一致的样式。</b>
     * <p>引：若要使对话框应用已定义的主题样式而不是使用默认的，可使用对话框带有主题的构造函数构造对话框即可。如Dialog、ProgressDialog、ThemeAlertDialog均提供了这样的构造函数。
     * 
     * @param context
     * @return
     */
    public static Context convertToSystemDialogTheme(Context context)
    {
        return new ContextThemeWrapper(context, R.style.Theme_Dialog);
    }

    public static AlertDialog.Builder createAlertDialogBuilder(Context context, String title, String[] buttons, OnClickListener onClickListener, boolean cancelable)
    {
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        if (title != null)
            ab.setTitle(title);
        if (buttons != null)
        {
            if (buttons.length >= 1)
                ab.setPositiveButton(buttons[0], onClickListener);
            if (buttons.length >= 2)
                ab.setNeutralButton(buttons[1], onClickListener);
            if (buttons.length >= 3)
                ab.setNegativeButton(buttons[2], onClickListener);
        }
        ab.setCancelable(cancelable);
        return ab;
    }

    public static AlertDialog.Builder createAlertDialogBuilder(Context context, int titleId, int[] buttonIds, OnClickListener onClickListener, boolean cancelable)
    {
        String title = null;
        if (titleId != -1)
            title = context.getString(titleId);
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return createAlertDialogBuilder(context, title, buttons, onClickListener, cancelable);
    }

    public static AlertDialog showAlertDialog(Context context, String title, String msg, String[] buttons, OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        AlertDialog.Builder ab = createAlertDialogBuilder(context, title, buttons, onClickListener, cancelable);
        if (msg != null)
            ab.setMessage(msg);
        if (isNotAutoDismiss)
            return setNotAutoDismiss(ab.show());
        else
            return ab.show();
    }

    public static AlertDialog showAlertDialog(Context context, int titleId, int msgId, int[] buttonIds, OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
            title = context.getString(titleId);
        String msg = null;
        if (msgId != -1)
            msg = context.getString(msgId);
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showAlertDialog(context, title, msg, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static AlertDialog showAlertDialog(Context context, String title, View view, String[] buttons, OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        AlertDialog.Builder ab = createAlertDialogBuilder(context, title, buttons, onClickListener, cancelable);
        if (view != null)
            ab.setView(view);
        if (isNotAutoDismiss)
            return setNotAutoDismiss(ab.show());
        else
            return ab.show();
    }

    public static AlertDialog showAlertDialog(Context context, int titleId, View view, int[] buttonIds, OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
            title = context.getString(titleId);
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showAlertDialog(context, title, view, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static ProgressDialog showProgressDialog(Context context, String title, String msg, String[] buttons, OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        return showProgressDialog(context, -1, title, msg, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static ProgressDialog showProgressDialog(Context context, int titleId, int msgId, int[] buttonIds, OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
            title = context.getString(titleId);
        String msg = null;
        if (msgId != -1)
            msg = context.getString(msgId);
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showProgressDialog(context, title, msg, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static ProgressDialog showProgressDialog(Context context, int theme, String title, String msg, String[] buttons, OnClickListener onClickListener, boolean cancelable,
            boolean isNotAutoDismiss)
    {
        ProgressDialog pd = null;
        if (theme == -1)
            pd = new ProgressDialog(context);
        else
            pd = new ProgressDialog(context, theme);
        if (title != null)
            pd.setTitle(title);
        if (msg != null)
            pd.setMessage(msg);
        if (buttons != null)
        {
            if (buttons.length >= 1)
                pd.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
            if (buttons.length >= 2)
                pd.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[1], onClickListener);
            if (buttons.length >= 3)
                pd.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[2], onClickListener);
        }
        pd.setCancelable(cancelable);
        pd.show();
        if (isNotAutoDismiss)
            return (ProgressDialog) setNotAutoDismiss(pd);
        else
            return pd;
    }

    public static ProgressDialog showProgressDialog(Context context, int theme, int titleId, int msgId, int[] buttonIds, OnClickListener onClickListener, boolean cancelable, boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
            title = context.getString(titleId);
        String msg = null;
        if (msgId != -1)
            msg = context.getString(msgId);
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showProgressDialog(context, theme, title, msg, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static ThemeAlertDialog showThemeAlertDialog(Context context, int theme, String title, String msg, String[] buttons, OnClickListener onClickListener, boolean cancelable,
            boolean isNotAutoDismiss)
    {
        ThemeAlertDialog tad = null;
        if (theme == -1)
            tad = new ThemeAlertDialog(context);
        else
            tad = new ThemeAlertDialog(context, theme);
        if (title != null)
            tad.setTitle(title);
        if (msg != null)
            tad.setMessage(msg);
        if (buttons != null)
        {
            if (buttons.length >= 1)
                tad.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
            if (buttons.length >= 2)
                tad.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[1], onClickListener);
            if (buttons.length >= 3)
                tad.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[2], onClickListener);
        }
        tad.setCancelable(cancelable);
        tad.show();
        if (isNotAutoDismiss)
            return (ThemeAlertDialog) setNotAutoDismiss(tad);
        else
            return tad;
    }

    public static ThemeAlertDialog showThemeAlertDialog(Context context, int theme, int titleId, int msgId, int[] buttonIds, OnClickListener onClickListener, boolean cancelable,
            boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
            title = context.getString(titleId);
        String msg = null;
        if (msgId != -1)
            msg = context.getString(msgId);
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showThemeAlertDialog(context, theme, title, msg, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static ThemeAlertDialog showThemeAlertDialog(Context context, int theme, String title, View view, String[] buttons, OnClickListener onClickListener, boolean cancelable,
            boolean isNotAutoDismiss)
    {
        ThemeAlertDialog tad = null;
        if (theme == -1)
            tad = new ThemeAlertDialog(context);
        else
            tad = new ThemeAlertDialog(context, theme);
        if (title != null)
            tad.setTitle(title);
        if (view != null)
            tad.setView(view);
        if (buttons != null)
        {
            if (buttons.length >= 1)
                tad.setButton(DialogInterface.BUTTON_POSITIVE, buttons[0], onClickListener);
            if (buttons.length >= 2)
                tad.setButton(DialogInterface.BUTTON_NEUTRAL, buttons[1], onClickListener);
            if (buttons.length >= 3)
                tad.setButton(DialogInterface.BUTTON_NEGATIVE, buttons[2], onClickListener);
        }
        tad.setCancelable(cancelable);
        tad.show();
        if (isNotAutoDismiss)
            return (ThemeAlertDialog) setNotAutoDismiss(tad);
        else
            return tad;
    }

    public static ThemeAlertDialog showThemeAlertDialog(Context context, int theme, int titleId, View view, int[] buttonIds, OnClickListener onClickListener, boolean cancelable,
            boolean isNotAutoDismiss)
    {
        String title = null;
        if (titleId != -1)
            title = context.getString(titleId);
        String[] buttons = null;
        if (buttonIds != null)
        {
            buttons = new String[buttonIds.length];
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i] = context.getString(buttonIds[i]);
            }
        }
        return showThemeAlertDialog(context, theme, title, view, buttons, onClickListener, cancelable, isNotAutoDismiss);
    }

    public static AlertDialog setNotAutoDismiss(final AlertDialog dialog)
    {
        try
        {
            Field field = dialog.getClass().getDeclaredField("mAlert");
            field.setAccessible(true);
            // retrieve mAlert value
            Object obj = field.get(dialog);
            field = obj.getClass().getDeclaredField("mHandler");
            field.setAccessible(true);
            // replace mHandler with our own handler
            field.set(obj, new Handler(Looper.getMainLooper())
            {
                @Override
                public void handleMessage(Message msg)
                {
                    // TODO Auto-generated method stub
                    switch (msg.what)
                    {
                        case DialogInterface.BUTTON_POSITIVE:
                        case DialogInterface.BUTTON_NEUTRAL:
                        case DialogInterface.BUTTON_NEGATIVE:
                            ((DialogInterface.OnClickListener) msg.obj).onClick(dialog, msg.what);
                            break;
                    }
                }
            });
            return dialog;
        } catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

}
