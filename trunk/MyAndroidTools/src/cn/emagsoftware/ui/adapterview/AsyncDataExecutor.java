package cn.emagsoftware.ui.adapterview;

public abstract class AsyncDataExecutor
{

    /**
     * <p>加载异步数据的回调方法。可抛出任何异常，抛出异常时，外部会认为当前的异步数据执行失败</>
     * @param position 所在Adapter中的位置
     * @param dataHolder 用于Adapter的DataHolder对象
     * @param asyncDataIndex 需要加载的DataHolder中异步数据的索引
     * @param asyncDataGlobalId 需要加载的DataHolder中异步数据的全局ID，如图片的URL
     * @return
     * @throws Exception
     */
    public abstract Object onExecute(int position, DataHolder dataHolder, int asyncDataIndex, String asyncDataGlobalId) throws Exception;

    /**
     * <p>如果需要在加载完异步数据后通知使用了当前AdapterView的Adapter的所有AdapterView，则可以覆盖该方法并返回true，但不要轻易这么做，因为会影响到性能</>
     * @return
     */
    public boolean isNotifyAsyncDataForAll()
    {
        return false;
    }

}
