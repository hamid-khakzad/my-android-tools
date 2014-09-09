package cn.emagsoftware.ui.adapterview;

/**
 * Created by Wendell on 14-8-25.
 */
public interface PageInterface<D> {

    public void forcePageLoad();
    /**
     *<p>加载当前已经存在的个数，返回-1表示使用当前缓存的个数，此时loadPageInBackground只需返回下一页的数据；若返回其他值，表示返回了刷新的个数，则loadPageInBackground需要返回所有的数据</>
     * @return
     * @throws Exception
     */
    public int loadCountInBackground() throws Exception;
    /**
     * <p>只返回单页数据，但对Cursor而言，尽管加载的可以是单页数据，但返回的需要是整个结果集</>
     * @param isRefresh
     * @param start 起始位置，最小为0
     * @param page 起始页，最小为1
     * @return
     * @throws Exception
     */
    public D loadPageInBackground(boolean isRefresh,int start,int page) throws Exception;
    public void setPageCount(int pageCount);
    public boolean isLoadedAll();
    public boolean isException();

}
