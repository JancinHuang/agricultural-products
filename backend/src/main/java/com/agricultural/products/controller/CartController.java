package com.agricultural.products.controller;

import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.Cart;
import com.agricultural.products.mapper.CartMapper;
import com.agricultural.products.service.ObjectStorageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartMapper cartMapper;
    private final ObjectStorageService objectStorageService;

    public CartController(CartMapper cartMapper, ObjectStorageService objectStorageService) {
        this.cartMapper = cartMapper;
        this.objectStorageService = objectStorageService;
    }

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
        if (cart.getProductId() == null) {
            return Result.error(400, "商品不能为空");
        }
        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
            return Result.error(400, "商品数量必须大于 0");
        }

        cart.setUserId(userId);
        cartMapper.insertOrIncrease(cart);
        return Result.success();
    }

    @PutMapping("/quantity/{id}")
    public Result<Void> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return Result.error(400, "商品数量必须大于 0");
        }
        int updated = cartMapper.updateQuantityByUserId(id, SecurityUtils.getCurrentUserId(), quantity);
        return updated > 0 ? Result.success() : Result.error(403, "无权操作该购物车项");
    }

    @PutMapping("/selected/{id}")
    public Result<Void> updateSelected(@PathVariable Long id, @RequestParam Integer selected) {
        if (selected == null || (selected != 0 && selected != 1)) {
            return Result.error(400, "选中状态不正确");
        }
        int updated = cartMapper.updateSelectedByUserId(id, SecurityUtils.getCurrentUserId(), selected);
        return updated > 0 ? Result.success() : Result.error(403, "无权操作该购物车项");
    }

    @PutMapping("/selected/all")
    public Result<Void> updateAllSelected(@RequestParam Integer selected) {
        if (selected == null || (selected != 0 && selected != 1)) {
            return Result.error(400, "选中状态不正确");
        }
        cartMapper.updateAllSelected(SecurityUtils.getCurrentUserId(), selected);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        int deleted = cartMapper.deleteByIdAndUserId(id, SecurityUtils.getCurrentUserId());
        return deleted > 0 ? Result.success() : Result.error(403, "无权删除该购物车项");
    }

    @DeleteMapping("/selected")
    public Result<Void> deleteSelected() {
        cartMapper.deleteSelectedByUserId(SecurityUtils.getCurrentUserId());
        return Result.success();
    }

    @GetMapping("/total")
    public Result<Map<String, Object>> getTotal() {
        List<Cart> cartList = cartMapper.findByUserId(SecurityUtils.getCurrentUserId());

        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalCount = 0;
        for (Cart cart : cartList) {
            if (cart.getSelected() != null && cart.getSelected() == 1
                    && cart.getPrice() != null && cart.getQuantity() != null) {
                totalPrice = totalPrice.add(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
                totalCount += cart.getQuantity();
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
