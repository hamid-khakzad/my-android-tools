package cn.emagsoftware.ui;

import cn.emagsoftware.util.LogManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ToastWindow
{

    private PopupWindow pw   = null;
    private TextView    text = null;

    public ToastWindow(Context context)
    {
        pw = new PopupWindow(context);
        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setFocusable(false);
        text = new TextView(context);
        text.setGravity(Gravity.CENTER);
        setContentView(text);
        setBackgroundDrawable(context.getResources().getDrawable(context.getResources().getIdentifier("bitmap_generic_toast_bg", "drawable", context.getPackageName())));
    }

    public void setSoftInputMode(int mode)
    {
        pw.setSoftInputMode(mode);
    }

    public void setWidth(int width)
    {
        pw.setWidth(width);
    }

    public void setHeight(int height)
    {
        pw.setHeight(height);
    }

    public void setContentView(View contentView)
    {
        pw.setContentView(contentView);
    }

    public void setBackgroundDrawable(Drawable background)
    {
        pw.setBackgroundDrawable(background);
    }

    public void setText(int textResId)
    {
        text.setText(textResId);
    }

    public void setText(CharSequence textStr)
    {
        text.setText(textStr);
    }

    public void show(Window mainWindow, int distanceToBottom)
    {
        pw.showAtLocation(mainWindow.getDecorView(), Gravity.BOTTOM, 0, distanceToBottom);
    }

    public void showForMillis(Window mainWindow, int distanceToBottom, int millis)
    {
        show(mainWindow, distanceToBottom);
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                dismiss();
            }
        }, millis);
    }

    public void showCenter(Window mainWindow)
    {
        pw.showAtLocation(mainWindow.getDecorView(), Gravity.CENTER, 0, 0);
    }

    public void showCenterForMillis(Window mainWindow, int millis)
    {
        showCenter(mainWindow);
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                dismiss();
            }
        }, millis);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff)
    {
        pw.showAsDropDown(anchor, xoff, yoff);
    }

    public void setFocusable(boolean focusable)
    {
        pw.setFocusable(focusable);
    }

    public void setAnimationStyle(int animationStyle)
    {
        pw.setAnimationStyle(animationStyle);
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener)
    {
        pw.setOnDismissListener(listener);
    }

    public void dismiss()
    {
        try
        {
            pw.dismiss();
        } catch (RuntimeException e)
        { // 如果依附的window已关闭，则调用dismiss会导致异常，这里原则上不应该向外抛出
            LogManager.logE(ToastWindow.class, "dismiss ToastWindow failed.", e);
        }
    }

}
