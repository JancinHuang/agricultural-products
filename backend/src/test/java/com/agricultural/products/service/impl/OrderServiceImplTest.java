package com.agricultural.products.service.impl;

import com.agricultural.products.common.BusinessException;
import com.agricultural.products.entity.Cart;
import com.agricultural.products.entity.Order;
import com.agricultural.products.mapper.CartMapper;
import com.agricultural.products.mapper.OrderItemMapper;
import com.agricultural.products.mapper.OrderMapper;
import com.agricultural.products.mapper.ProductMapper;
import com.agricultural.products.service.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ObjectStorageService objectStorageService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Cart selectedCart;

    @BeforeEach
    void setUp() {
        selectedCart = new Cart();
        selectedCart.setId(10L);
        selectedCart.setUserId(2L);
        selectedCart.setProductId(3L);
        selectedCart.setProductName("苹果");
        selectedCart.setProductImage("apple.png");
        selectedCart.setPrice(new BigDecimal("8.50"));
        selectedCart.setQuantity(2);
        selectedCart.setSelected(1);
        selectedCart.setProductStatus(1);
        selectedCart.setProductStock(10);
    }

    @Test
    void createOrderCreatesItemsAndDecreasesStock() {
        when(cartMapper.findByUserId(2L)).thenReturn(List.of(selectedCart));
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return 1;
        });
        when(orderItemMapper.insert(any())).thenReturn(1);
        when(productMapper.decreaseStock(3L, 2)).thenReturn(1);

        Order order = new Order();
        order.setReceiverName("张三");
        order.setReceiverPhone("13800138000");
        order.setReceiverAddress("测试地址");

        Order created = orderService.createOrder(2L, order);

        assertEquals(100L, created.getId());
        assertEquals(2L, created.getUserId());
        assertEquals(new BigDecimal("17.00"), created.getTotalAmount());
        assertEquals(0, created.getStatus());

        verify(orderItemMapper).insert(any());
        verify(productMapper).decreaseStock(3L, 2);
        verify(productMapper).increaseSales(3L, 2);
        verify(cartMapper).deleteSelectedByUserId(2L);
    }

    @Test
    void createOrderThrowsWhenStockDecrementFails() {
        when(cartMapper.findByUserId(2L)).thenReturn(List.of(selectedCart));
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return 1;
        });
        when(orderItemMapper.insert(any())).thenReturn(1);
        when(productMapper.decreaseStock(3L, 2)).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.createOrder(2L, new Order())
        );

        assertTrue(exception.getMessage().contains("库存不足"));
        verify(productMapper, never()).increaseSales(anyLong(), anyInt());
        verify(cartMapper, never()).deleteSelectedByUserId(anyLong());
    }

    @Test
    void createOrderUsesCurrentUserIdNotRequestUserId() {
        when(cartMapper.findByUserId(2L)).thenReturn(List.of(selectedCart));
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return 1;
        });
        when(orderItemMapper.insert(any())).thenReturn(1);
        when(productMapper.decreaseStock(3L, 2)).thenReturn(1);

        Order order = new Order();
        order.setUserId(999L);

        orderService.createOrder(2L, order);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).insert(captor.capture());
        assertEquals(2L, captor.getValue().getUserId());
    }
}
