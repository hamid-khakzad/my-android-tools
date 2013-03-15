package cn.emagsoftware.ui;

import android.content.Context;
import android.os.Handler;

/**
 * <p>����Ĺ���ʵ��������AsyncTask��
 * 
 * @author Wendell
 * @version 2.1
 * @deprecated ���ڸ�������������Ҳ���ڷ�UI�߳����кͻص������������߳����������ڻ���Context�Ҳ�֧���̳߳أ����������࣬��ʹ��AsyncTask��AsyncWeakTask����
 */
public class UIThread extends Thread
{

    protected Context context     = null;
    protected boolean isCancelled = false;
    protected Handler handler     = new Handler();

    public UIThread(Context context)
    {
        if (context == null)
            throw new NullPointerException();
        this.context = context;
    }

    public final void run()
    {
        super.run();
        try
        {
            if (isCancelled)
                return;
            final boolean[] isOK = new boolean[1];
            isOK[0] = false;
            handler.post(new Runnable()
            { // onBeginUIֻ����run�лص���start���̳߳��в������ã����Բ���ͨ����дstart���ص�
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            if (isCancelled)
                            {
                                isOK[0] = true;
                                return;
                            }
                            onBeginUI(context);
                            isOK[0] = true;
                        }
                    });
            while (!isOK[0])
            {
                sleep(100);
            }
            if (isCancelled)
                return;
            final Object result = onRunNoUI(context);
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (isCancelled)
                        return;
                    onSuccessUI(context, result);
                }
            });
        } catch (final Exception e)
        {
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (isCancelled)
                        return;
                    onExceptionUI(context, e);
                }
            });
        }
    }

    public void cancel()
    {
        isCancelled = true;
    }

    public void postProgress(final Object progress)
    {
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                onProgressUI(context, progress);
            }
        });
    }

    /**
     * <p>onBeginUI���ڵ�ǰ�߳���������֮��Ż���ã���ʹ�����̲߳�����϶����Ҫʹ�����߳��ν�һ�£���������ⲿ�Ĳ�ͬ��������������ǰ�߳�֮ǰִ����ص����̵߳��߼���������onBeginUI��
     * 
     * @param context
     */
    protected void onBeginUI(Context context)
    {
    }

    protected Object onRunNoUI(Context context) throws Exception
    {
        return null;
    }

    protected void onProgressUI(Context context, Object progress)
    {
    }

    protected void onSuccessUI(Context context, Object result)
    {
    }

    protected void onExceptionUI(Context context, Exception e)
    {
    }

}
