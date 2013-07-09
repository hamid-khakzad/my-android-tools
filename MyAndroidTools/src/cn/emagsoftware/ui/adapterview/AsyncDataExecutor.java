package cn.emagsoftware.ui.adapterview;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import cn.emagsoftware.util.OptionalExecutorTask;

public abstract class AsyncDataExecutor
{

    private static PushTask                               PUSH_TASK              = new PushTask();
    static
    {
        PUSH_TASK.execute();
    }

    /**
     * <p>�����첽���ݵĻص����� <p>���׳��κ��쳣���׳��쳣ʱ���ⲿ����Ϊ��ǰ���첽����ִ��ʧ��
     * 
     * @param position ����Adapter�е�λ��
     * @param dataHolder ����Adapter��DataHolder����
     * @param asyncDataIndex ��Ҫ���ص�DataHolder���첽���ݵ�����
     * @return ִ�к�õ��Ľ��
     * @throws Exception
     */
    public abstract Object onExecute(int position, DataHolder dataHolder, int asyncDataIndex) throws Exception;

    /**
     * <p>�����Ҫ�ڼ������첽���ݺ�֪ͨʹ���˵�ǰAdapterView��Adapter������AdapterView������Ը��Ǹ÷���������true������Ҫ������ô������Ϊ��Ӱ�쵽����</>
     * @return
     */
    public boolean isNotifyAsyncDataForAll()
    {
        return false;
    }

    private static class PushTask extends OptionalExecutorTask<Object, Integer, Object>
    {
        private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

        @Override
        protected Object doInBackground(Object... params)
        {
            while(true)
            {
                try
                {
                    /**
                     * �첽�����߳̿�������ͨ�߳��г�ʼ����������ԭ���ǣ�
                     * 1.���첽�����߳�ͬ��ʹ��OptionalExecutorTask��PushTask��ʱ�Ѿ������̳߳�ʼ���˾�̬��Handler
                     * 2.�첽�����̲߳�����onPreExecute()���߼�������������ͨ�߳���ִ�п��ܵ��µĴ���
                     */
                    queue.poll(Long.MAX_VALUE,TimeUnit.SECONDS).run();
                }catch(InterruptedException e)
                {
                }
            }
        }

    }

}
