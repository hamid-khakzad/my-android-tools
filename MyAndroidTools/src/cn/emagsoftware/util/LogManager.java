package cn.emagsoftware.util;

import cn.emagsoftware.util.jlog.JLog;
import android.util.Log;

/**
 * <p>��־�����࣬��Ҫ����־�������¼��㹦�ܲ��䣺<br> 1.ǿ��ִ��TAG����ʹ�������Ĺ淶<br> 2.������ֱ������������ܵ��µĿ�ָ���쳣<br> 3.������������أ��ɸ��ݵȼ�������־������<br> 4.�ɿ����Ƿ�ͬ������JLog��־
 * 
 * @author Wendell
 * 
 */
public final class LogManager
{

    public static final int VERBOSE       = 2;

    public static final int DEBUG         = 3;

    public static final int INFO          = 4;

    public static final int WARN          = 5;

    public static final int ERROR         = 6;

    public static final int ASSERT        = 7;

    /**
     * <p>ͨ�����ô˱�����������־�������ֻ�еȼ����ڵ������õĵȼ�ʱ��־�Żᱻ��������ô˱���ΪLogManager.ASSERT�ɽ�ֹ���еȼ�����־���
     */
    public static int       LOGGING_LEVEL = VERBOSE;
    /**
     * <p>ͨ�����ô˱����������Ƿ�ͬ������JLog��־
     */
    public static boolean   ENABLED_JLOG  = false;

    private LogManager()
    {
    }

    public static void logV(Class<? extends Object> tag, String msg)
    {
        if (VERBOSE >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.v(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.fine(tagStr.concat(":").concat(msg));
        }
    }

    public static void logV(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (VERBOSE >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.v(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.fine(tagStr.concat(":").concat(msg));
            } else
            {
                Log.v(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.fine(tagStr.concat(":").concat(msg), tr);
            }
        }
    }

    public static void logD(Class<? extends Object> tag, String msg)
    {
        if (DEBUG >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.d(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.config(tagStr.concat(":").concat(msg));
        }
    }

    public static void logD(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (DEBUG >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.d(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.config(tagStr.concat(":").concat(msg));
            } else
            {
                Log.d(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.config(tagStr.concat(":").concat(msg), tr);
            }
        }
    }

    public static void logI(Class<? extends Object> tag, String msg)
    {
        if (INFO >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.i(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.info(tagStr.concat(":").concat(msg));
        }
    }

    public static void logI(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (INFO >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.i(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.info(tagStr.concat(":").concat(msg));
            } else
            {
                Log.i(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.info(tagStr.concat(":").concat(msg), tr);
            }
        }
    }

    public static void logW(Class<? extends Object> tag, String msg)
    {
        if (WARN >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.w(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.warning(tagStr.concat(":").concat(msg));
        }
    }

    public static void logW(Class<? extends Object> tag, Throwable tr)
    {
        if (WARN >= LOGGING_LEVEL)
        {
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.w(tagStr, "");
                if (ENABLED_JLOG)
                    JLog.warning(tagStr.concat(":").concat(""));
            } else
            {
                Log.w(tagStr, tr);
                if (ENABLED_JLOG)
                    JLog.warning(tagStr.concat(":").concat(""), tr);
            }
        }
    }

    public static void logW(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (WARN >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.w(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.warning(tagStr.concat(":").concat(msg));
            } else
            {
                Log.w(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.warning(tagStr.concat(":").concat(msg), tr);
            }
        }
    }

    public static void logE(Class<? extends Object> tag, String msg)
    {
        if (ERROR >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            Log.e(tagStr, msg);
            if (ENABLED_JLOG)
                JLog.severe(tagStr.concat(":").concat(msg));
        }
    }

    public static void logE(Class<? extends Object> tag, String msg, Throwable tr)
    {
        if (ERROR >= LOGGING_LEVEL)
        {
            if (msg == null)
                msg = "";
            String tagStr = tag.getSimpleName();
            if (tr == null)
            {
                Log.e(tagStr, msg);
                if (ENABLED_JLOG)
                    JLog.severe(tagStr.concat(":").concat(msg));
            } else
            {
                Log.e(tagStr, msg, tr);
                if (ENABLED_JLOG)
                    JLog.severe(tagStr.concat(":").concat(msg), tr);
            }
        }
    }

}
