package cn.emagsoftware.ui.adapterview;

public abstract class AsyncDataExecutor
{

    /**
     * <p>�����첽���ݵĻص����������׳��κ��쳣���׳��쳣ʱ���ⲿ����Ϊ��ǰ���첽����ִ��ʧ��</>
     * @param position ����Adapter�е�λ��
     * @param dataHolder ����Adapter��DataHolder����
     * @param asyncDataIndex ��Ҫ���ص�DataHolder���첽���ݵ�����
     * @param asyncDataGlobalId ��Ҫ���ص�DataHolder���첽���ݵ�ȫ��ID����ͼƬ��URL
     * @return
     * @throws Exception
     */
    public abstract Object onExecute(int position, DataHolder dataHolder, int asyncDataIndex, String asyncDataGlobalId) throws Exception;

    /**
     * <p>�����Ҫ�ڼ������첽���ݺ�֪ͨʹ���˵�ǰAdapterView��Adapter������AdapterView������Ը��Ǹ÷���������true������Ҫ������ô������Ϊ��Ӱ�쵽����</>
     * @return
     */
    public boolean isNotifyAsyncDataForAll()
    {
        return false;
    }

}
