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
        return (pageNum - 1) * pageSize;
    }
}
