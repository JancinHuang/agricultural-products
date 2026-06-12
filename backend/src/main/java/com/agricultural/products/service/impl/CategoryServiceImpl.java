package com.agricultural.products.service.impl;

import com.agricultural.products.entity.Category;
import com.agricultural.products.mapper.CategoryMapper;
import com.agricultural.products.service.CategoryService;
import com.agricultural.products.service.ObjectStorageService;
import com.agricultural.products.service.RedisCacheService;
import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private ObjectStorageService objectStorageService;
    
    @Override
    public Category findById(Long id) {
        return signCategoryIcon(categoryMapper.findById(id));
    }
    
    @Override
    public List<Category> findAll() {
        List<Category> categories = redisCacheService.getCategoryList()
            .orElseGet(() -> {
                List<Category> rawCategories = categoryMapper.findAll();
                redisCacheService.setCategoryList(rawCategories);
                return rawCategories;
            });
        return signCategoryIcons(categories);
    }
    
    @Override
    public PageResult<Category> findByPage(PageRequest request) {
        List<Category> list = categoryMapper.findByPage(request);
        Long total = request.getKeyword() != null && !request.getKeyword().isEmpty() 
            ? categoryMapper.countByKeyword(request.getKeyword()) 
            : categoryMapper.count();
        return new PageResult<>(signCategoryIcons(list), total, request.getPageNum(), request.getPageSize());
    }
    
    @Override
    public Long count() {
        return categoryMapper.count();
    }
    
    @Override
    public boolean save(Category category) {
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        boolean success = categoryMapper.insert(category) > 0;
        if (success) {
            redisCacheService.evictCategoryList();
        }
        return success;
    }
    
    @Override
    public boolean update(Category category) {
        boolean success = categoryMapper.update(category) > 0;
        if (success) {
            redisCacheService.evictCategoryList();
        }
        return success;
    }
    
    @Override
    public boolean deleteById(Long id) {
        boolean success = categoryMapper.deleteById(id) > 0;
        if (success) {
            redisCacheService.evictCategoryList();
        }
        return success;
    }

    private List<Category> signCategoryIcons(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        categories.forEach(this::signCategoryIcon);
        return categories;
    }

    private Category signCategoryIcon(Category category) {
        if (category == null) {
            return null;
        }
        category.setIconUrl(objectStorageService.toAccessibleUrl(category.getIcon()));
        return category;
    }
}
