package com.agricultural.products.service;

import com.agricultural.products.entity.Product;
import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 产品服务接口
 */
public interface ProductService {
    
    Product findById(Long id);
    
    List<Product> findHotProducts(int limit);
    
    List<Product> findByCategoryId(Long categoryId);
    
    PageResult<Product> findByPage(PageRequest request);

    Map<String, Object> search(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                               String orderBy, int pageNum, int pageSize);
    
    Long count();
    
    boolean save(Product product);
    
    boolean update(Product product);
    
    boolean deleteById(Long id);
}
