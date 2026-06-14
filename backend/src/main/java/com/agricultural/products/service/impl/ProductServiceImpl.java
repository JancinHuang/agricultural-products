package com.agricultural.products.service.impl;

import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.entity.Product;
import com.agricultural.products.mapper.ProductMapper;
import com.agricultural.products.service.ObjectStorageService;
import com.agricultural.products.service.ProductService;
import com.agricultural.products.service.RedisCacheService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final RedisCacheService redisCacheService;
    private final ObjectStorageService objectStorageService;

    public ProductServiceImpl(
            ProductMapper productMapper,
            RedisCacheService redisCacheService,
            ObjectStorageService objectStorageService) {
        this.productMapper = productMapper;
        this.redisCacheService = redisCacheService;
        this.objectStorageService = objectStorageService;
    }

    @Override
    public Product findById(Long id) {
        return signProductImage(productMapper.findById(id));
    }

    @Override
    public List<Product> findHotProducts(int limit) {
        List<Product> products = redisCacheService.getHotProducts(limit)
                .orElseGet(() -> {
                    List<Product> rawProducts = productMapper.findHotProducts(limit);
                    redisCacheService.setHotProducts(limit, rawProducts);
                    return rawProducts;
                });
        return signProductImages(products);
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return signProductImages(productMapper.findByCategoryId(categoryId));
    }

    @Override
    public PageResult<Product> findByPage(PageRequest request) {
        List<Product> list = productMapper.findByPage(request);
        Long total = request.getKeyword() != null && !request.getKeyword().isEmpty()
                ? productMapper.countByKeyword(request.getKeyword())
                : productMapper.count();
        return new PageResult<>(signProductImages(list), total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public Map<String, Object> search(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                      String orderBy, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Product> products = productMapper.searchProducts(keyword, categoryId, minPrice, maxPrice, orderBy, pageSize, offset);
        Long total = productMapper.countSearchProducts(keyword, categoryId, minPrice, maxPrice);

        Map<String, Object> result = new HashMap<>();
        result.put("list", signProductImages(products));
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Long count() {
        return productMapper.count();
    }

    @Override
    public boolean save(Product product) {
        if (product.getSales() == null) {
            product.setSales(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        boolean success = productMapper.insert(product) > 0;
        if (success) {
            redisCacheService.evictProductCaches();
        }
        return success;
    }

    @Override
    public boolean update(Product product) {
        boolean success = productMapper.update(product) > 0;
        if (success) {
            redisCacheService.evictProductCaches();
        }
        return success;
    }

    @Override
    public boolean deleteById(Long id) {
        boolean success = productMapper.deleteById(id) > 0;
        if (success) {
            redisCacheService.evictProductCaches();
        }
        return success;
    }

    private List<Product> signProductImages(List<Product> products) {
        if (products == null) {
            return null;
        }
        products.forEach(this::signProductImage);
        return products;
    }

    private Product signProductImage(Product product) {
        if (product == null) {
            return null;
        }
        product.setImage(objectStorageService.toAccessibleUrl(product.getImage()));
        return product;
    }
}
