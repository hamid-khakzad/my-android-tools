package cn.emagsoftware.ui.adapterview;

/**
 * Created by Wendell on 14-8-25.
 */
public interface PageInterface<D> {

    public void forcePageLoad();
    /**
     *<p>���ص�ǰ�Ѿ����ڵĸ���������-1��ʾʹ�õ�ǰ����ĸ�������ʱloadPageInBackgroundֻ�践����һҳ�����ݣ�����������ֵ����ʾ������ˢ�µĸ�������loadPageInBackground��Ҫ�������е�����</>
     * @return
     * @throws Exception
     */
    public int loadCountInBackground() throws Exception;
    /**
     * <p>ֻ���ص�ҳ���ݣ�����Cursor���ԣ����ܼ��صĿ����ǵ�ҳ���ݣ������ص���Ҫ�����������</>
     * @param isRefresh
     * @param start ��ʼλ�ã���СΪ0
     * @param page ��ʼҳ����СΪ1
     * @return
     * @throws Exception
     */
    public D loadPageInBackground(boolean isRefresh,int start,int page) throws Exception;
    public void setPageCount(int pageCount);
    public boolean isLoadedAll();
    public boolean isException();

}
