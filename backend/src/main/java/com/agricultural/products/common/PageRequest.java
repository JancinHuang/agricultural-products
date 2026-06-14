package com.agricultural.products.common;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Integer categoryId;
    private Integer status;
    
    public Integer getOffset() {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        return (safePageNum - 1) * safePageSize;
    }

    public Integer getPageNum() {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    public Integer getPageSize() {
        return pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
    }
}
