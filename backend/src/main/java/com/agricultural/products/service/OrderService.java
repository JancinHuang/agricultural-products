package com.agricultural.products.service;

import com.agricultural.products.entity.Order;
import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.vo.OrderVO;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    Order findById(Long id);
    
    Order findByOrderNo(String orderNo);
    
    PageResult<Order> findByPage(PageRequest request);
    
    Long count();
    
    boolean save(Order order);
    
    boolean update(Order order);
    
    boolean deleteById(Long id);
    
    boolean updateStatus(Long id, Integer status);

    List<OrderVO> findUserOrders(Long userId);

    Map<String, Object> getOrderDetail(Long id, Long currentUserId, boolean admin);

    Order createOrder(Long userId, Order order);

    void payOrder(Long id, Long currentUserId, boolean admin);

    void cancelOrder(Long id, Long currentUserId, boolean admin);

    void confirmOrder(Long id, Long currentUserId, boolean admin);
    
    Map<String, Object> getStatistics();
}
