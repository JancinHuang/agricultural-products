package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.entity.Cart;
import com.agricultural.products.mapper.CartMapper;
import com.agricultural.products.service.ObjectStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest extends ControllerSecurityTestSupport {

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ObjectStorageService objectStorageService;

    private CartController cartController;

    @BeforeEach
    void setUp() {
        cartController = new CartController(cartMapper, objectStorageService);
        loginAsUser(2L);
    }

    @Test
    void addRejectsEmptyProduct() {
        Cart cart = new Cart();
        cart.setQuantity(1);

        Result<Void> result = cartController.add(cart);

        assertEquals(400, result.getCode());
        verify(cartMapper, never()).insertOrIncrease(cart);
    }

    @Test
    void addUsesCurrentUserAndInsertOrIncrease() {
        Cart cart = new Cart();
        cart.setProductId(3L);
        cart.setQuantity(2);
        cart.setUserId(999L);

        Result<Void> result = cartController.add(cart);

        assertEquals(200, result.getCode());
        assertEquals(2L, cart.getUserId());
        verify(cartMapper).insertOrIncrease(cart);
    }

    @Test
    void updateQuantityRejectsInvalidQuantity() {
        Result<Void> result = cartController.updateQuantity(10L, 0);

        assertEquals(400, result.getCode());
        verify(cartMapper, never()).updateQuantityByUserId(10L, 2L, 0);
    }

    @Test
    void updateQuantityReturnsForbiddenWhenCartDoesNotBelongToUser() {
        when(cartMapper.updateQuantityByUserId(10L, 2L, 3)).thenReturn(0);

        Result<Void> result = cartController.updateQuantity(10L, 3);

        assertEquals(403, result.getCode());
    }

    @Test
    void deleteReturnsForbiddenWhenCartDoesNotBelongToUser() {
        when(cartMapper.deleteByIdAndUserId(10L, 2L)).thenReturn(0);

        Result<Void> result = cartController.delete(10L);

        assertEquals(403, result.getCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTotalOnlyCountsSelectedItems() {
        Cart selected = new Cart();
        selected.setSelected(1);
        selected.setPrice(new BigDecimal("8.50"));
        selected.setQuantity(2);

        Cart unselected = new Cart();
        unselected.setSelected(0);
        unselected.setPrice(new BigDecimal("99.00"));
        unselected.setQuantity(1);

        when(cartMapper.findByUserId(2L)).thenReturn(List.of(selected, unselected));

        Result<Map<String, Object>> result = cartController.getTotal();

        assertEquals(200, result.getCode());
        assertEquals(new BigDecimal("17.00"), result.getData().get("totalPrice"));
        assertEquals(2, result.getData().get("totalCount"));
    }
}
