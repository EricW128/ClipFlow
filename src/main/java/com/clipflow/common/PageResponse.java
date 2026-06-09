package com.clipflow.common;

import java.util.List;

public class PageResponse<T> {

    private long total;
    private long page;
    private long size;
    private List<T> records;

    public PageResponse(
            long total,
            long page,
            long size,
            List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public long getPage() {
        return page;
    }

    public long getSize() {
        return size;
    }

    public List<T> getRecords() {
        return records;
    }

}