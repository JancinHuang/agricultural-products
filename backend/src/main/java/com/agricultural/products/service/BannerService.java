package com.agricultural.products.service;

import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.entity.Banner;

import java.util.List;

public interface BannerService {

    List<Banner> listEnabled();

    PageResult<Banner> findByPage(PageRequest request);

    Banner findById(Long id);

    boolean save(Banner banner);

    boolean update(Banner banner);

    boolean deleteById(Long id);
}
