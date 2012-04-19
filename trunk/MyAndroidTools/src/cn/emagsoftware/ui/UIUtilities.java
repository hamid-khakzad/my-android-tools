package cn.emagsoftware.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.widget.Gallery;

public final class UIUtilities
{

    private UIUtilities()
    {
    }

    /**
     * <p>ʹ����ȷ�����ƣ���Gallery�϶�̬����ָ���ľ��룬��������ָ����������Ч��
     * 
     * @param gallery
     * @param distance
     */
    public static void scrollGallery(Gallery gallery, int distance)
    {
        try
        {
            Field field = Gallery.class.getDeclaredField("mFlingRunnable");
            field.setAccessible(true);
            Object flingRunnable = field.get(gallery);
            Method method = flingRunnable.getClass().getDeclaredMethod("startUsingDistance", int.class);
            method.setAccessible(true);
            method.invoke(flingRunnable, distance);
        } catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

}
