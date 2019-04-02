package Adapter;

public class PageControl {
    private static int page = 0;
    private static int rowCount = 0;
    private static int pageSize = 10;
    private static final int firstPage = 0;
    private static final int endPage = 999;
    private static final int midPage = 250;

    public static void setNextPage() {
        page = (page + 1) * pageSize >= rowCount ? page : page + 1;
    }

    public static void setPrevPage() {
        page = page > 0 ? page - 1 : page;
    }

    public static void setFirstPage() {
        page = 0;
    }

    public static void setLastPage() {
        page = rowCount % pageSize == 0 ? (rowCount < pageSize ? 0 : rowCount / pageSize - 1) : rowCount / pageSize;
    }


    public static int getPage() {
        return page;
    }

    public static int getAllPage() {
        return rowCount % pageSize == 0 ? (rowCount < pageSize ? 0 : rowCount / pageSize - 1) : rowCount / pageSize;
    }

    public static int PageState() {
        if ((page + 1) * pageSize > rowCount)
            return endPage;
        if (page == 0)
            return firstPage;
        else
            return midPage;
    }

    public static void setPageSize(int pageSize) {
        PageControl.pageSize = pageSize;
    }

    static int getPageSize() {
        return pageSize;
    }

    public static void setPage(int page) {
        PageControl.page = page;
    }

    static int getRowCount() {
        return rowCount;
    }

    static void setRowCount(int rowCount) {
        PageControl.rowCount = rowCount;
    }
}
