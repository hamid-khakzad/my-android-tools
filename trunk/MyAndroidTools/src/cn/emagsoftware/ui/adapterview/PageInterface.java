package cn.emagsoftware.ui.adapterview;

/**
 * Created by Wendell on 14-8-25.
 */
public interface PageInterface<D> {

    /**
     * <p>只返回单页数据，但对Cursor而言，尽管加载的可以是单页数据，但返回的需要是整个结果集</>
     * @param start 起始位置，最小为0
     * @param page 起始页，最小为1
     * @return
     * @throws Exception
     */
    public D loadPageInBackground(int start,int page) throws Exception;
    public void setPageCount(int pageCount);
    public boolean isLoadedAll();
    public boolean isException();

}
