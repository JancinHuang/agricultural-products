package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.Cart;
import com.agricultural.products.mapper.CartMapper;
import com.agricultural.products.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ObjectStorageService objectStorageService;

    @GetMapping("/list")
    public Result<List<Cart>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Cart> cartList = cartMapper.findByUserId(userId);
        cartList.forEach(this::fillProductImageUrl);
        return Result.success(cartList);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Cart cart) {
        Long userId = SecurityUtils.getCurrentUserId();
        Cart existCart = cartMapper.findByUserIdAndProductId(userId, cart.getProductId());

        if (existCart != null) {
            existCart.setQuantity(existCart.getQuantity() + cart.getQuantity());
            cartMapper.updateQuantity(existCart.getId(), existCart.getQuantity());
        } else {
            cart.setUserId(userId);
            cartMapper.insert(cart);
        }
        return Result.success();
    }

    @PutMapping("/quantity/{id}")
    public Result<Void> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        cartMapper.updateQuantity(id, quantity);
        return Result.success();
    }

    @PutMapping("/selected/{id}")
    public Result<Void> updateSelected(@PathVariable Long id, @RequestParam Integer selected) {
        cartMapper.updateSelected(id, selected);
        return Result.success();
    }

    @PutMapping("/selected/all")
    public Result<Void> updateAllSelected(@RequestParam Integer selected) {
        Long userId = SecurityUtils.getCurrentUserId();
        cartMapper.updateAllSelected(userId, selected);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartMapper.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/selected")
    public Result<Void> deleteSelected() {
        Long userId = SecurityUtils.getCurrentUserId();
        cartMapper.deleteSelectedByUserId(userId);
        return Result.success();
    }

    @GetMapping("/total")
    public Result<Map<String, Object>> getTotal() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Cart> cartList = cartMapper.findByUserId(userId);

        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalCount = 0;

        for (Cart cart : cartList) {
            if (cart.getSelected() != null && cart.getSelected() == 1) {
                if (cart.getPrice() != null && cart.getQuantity() != null) {
                    totalPrice = totalPrice.add(cart.getPrice().multiply(new BigDecimal(cart.getQuantity())));
                    totalCount += cart.getQuantity();
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalPrice", totalPrice);
        result.put("totalCount", totalCount);
        return Result.success(result);
    }

    private void fillProductImageUrl(Cart cart) {
        if (cart != null) {
            cart.setProductImage(objectStorageService.toAccessibleUrl(cart.getProductImage()));
        }
    }
}
