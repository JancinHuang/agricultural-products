package com.agricultural.products.service.impl;

import com.agricultural.products.common.BusinessException;
import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.entity.Cart;
import com.agricultural.products.entity.Order;
import com.agricultural.products.entity.OrderItem;
import com.agricultural.products.mapper.CartMapper;
import com.agricultural.products.mapper.OrderItemMapper;
import com.agricultural.products.mapper.OrderMapper;
import com.agricultural.products.mapper.ProductMapper;
import com.agricultural.products.service.ObjectStorageService;
import com.agricultural.products.service.OrderService;
import com.agricultural.products.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ObjectStorageService objectStorageService;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            CartMapper cartMapper,
                            ProductMapper productMapper,
                            ObjectStorageService objectStorageService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.objectStorageService = objectStorageService;
    }

    @Override
    public Order findById(Long id) {
        return orderMapper.findById(id);
    }

    @Override
    public Order findByOrderNo(String orderNo) {
        return orderMapper.findByOrderNo(orderNo);
    }

    @Override
    public PageResult<Order> findByPage(PageRequest request) {
        List<Order> list = orderMapper.findByPage(request);
        Long total = request.getKeyword() != null && !request.getKeyword().isEmpty()
                ? orderMapper.countByKeyword(request.getKeyword())
                : orderMapper.count();
        return new PageResult<>(list, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public Long count() {
        return orderMapper.count();
    }

    @Override
    public boolean save(Order order) {
        order.setOrderNo(generateOrderNo());
        order.setStatus(0);
        return orderMapper.insert(order) > 0;
    }

    @Override
    public boolean update(Order order) {
        return orderMapper.update(order) > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        orderItemMapper.deleteByOrderId(id);
        return orderMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        return orderMapper.update(order) > 0;
    }

    @Override
    public List<OrderVO> findUserOrders(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return buildOrderViews(orderMapper.findByUserId(userId));
    }

    @Override
    public Map<String, Object> getOrderDetail(Long id, Long currentUserId, boolean admin) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        assertCanAccessOrder(order, currentUserId, admin);

        List<OrderItem> items = orderItemMapper.findByOrderId(id);
        fillProductImageUrls(items);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return result;
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, Order order) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        List<Cart> selectedCart = cartMapper.findByUserId(userId).stream()
                .filter(cart -> cart.getSelected() != null && cart.getSelected() == 1)
                .toList();

        if (selectedCart.isEmpty()) {
            throw new BusinessException("请选择要购买的商品");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : selectedCart) {
            validateCartItem(cart);
            totalAmount = totalAmount.add(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        order.setOrderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);

        if (orderMapper.insert(order) <= 0) {
            throw new BusinessException("订单创建失败");
        }

        for (Cart cart : selectedCart) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(cart.getProductId());
            item.setProductName(cart.getProductName());
            item.setProductImage(cart.getProductImage());
            item.setPrice(cart.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));

            if (orderItemMapper.insert(item) <= 0) {
                throw new BusinessException("订单明细创建失败");
            }
            if (productMapper.decreaseStock(cart.getProductId(), cart.getQuantity()) <= 0) {
                throw new BusinessException("商品 " + cart.getProductName() + " 库存不足");
            }
            productMapper.increaseSales(cart.getProductId(), cart.getQuantity());
        }

        cartMapper.deleteSelectedByUserId(userId);
        return order;
    }

    @Override
    public void payOrder(Long id, Long currentUserId, boolean admin) {
        Order order = requireAccessibleOrder(id, currentUserId, admin);
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确");
        }
        orderMapper.updateStatus(id, 1);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id, Long currentUserId, boolean admin) {
        Order order = requireAccessibleOrder(id, currentUserId, admin);
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待付款订单");
        }
        orderMapper.updateStatus(id, 4);

        List<OrderItem> items = orderItemMapper.findByOrderId(id);
        for (OrderItem item : items) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }
    }

    @Override
    public void confirmOrder(Long id, Long currentUserId, boolean admin) {
        Order order = requireAccessibleOrder(id, currentUserId, admin);
        if (order.getStatus() != 2) {
            throw new BusinessException("只能确认待收货订单");
        }
        orderMapper.updateStatus(id, 3);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderMapper.count());
        return stats;
    }

    private void validateCartItem(Cart cart) {
        if (cart.getProductId() == null || cart.getProductName() == null) {
            throw new BusinessException("购物车商品不存在");
        }
        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
            throw new BusinessException("商品 " + cart.getProductName() + " 数量不正确");
        }
        if (cart.getPrice() == null) {
            throw new BusinessException("商品 " + cart.getProductName() + " 价格不正确");
        }
        if (cart.getProductStatus() == null || cart.getProductStatus() != 1) {
            throw new BusinessException("商品 " + cart.getProductName() + " 已下架");
        }
        if (cart.getProductStock() == null || cart.getQuantity() > cart.getProductStock()) {
            throw new BusinessException("商品 " + cart.getProductName() + " 库存不足");
        }
    }

    private Order requireAccessibleOrder(Long id, Long currentUserId, boolean admin) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        assertCanAccessOrder(order, currentUserId, admin);
        return order;
    }

    private void assertCanAccessOrder(Order order, Long currentUserId, boolean admin) {
        if (admin) {
            return;
        }
        if (currentUserId == null || !currentUserId.equals(order.getUserId())) {
            throw new BusinessException(403, "无权操作该订单");
        }
    }

    private List<OrderVO> buildOrderViews(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> allItems = orderItemMapper.findByOrderIds(orderIds);
        fillProductImageUrls(allItems);
        Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        return orders.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            vo.setItems(itemsByOrderId.getOrDefault(order.getId(), List.of()));
            return vo;
        }).toList();
    }

    private void fillProductImageUrls(List<OrderItem> items) {
        if (items == null) {
            return;
        }
        items.forEach(item -> item.setProductImage(objectStorageService.toAccessibleUrl(item.getProductImage())));
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + new Random().nextInt(1000);
    }
}
