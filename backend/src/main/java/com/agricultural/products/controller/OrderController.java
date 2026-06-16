package com.agricultural.products.controller;

import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.entity.Order;
import com.agricultural.products.service.OrderService;
import com.agricultural.products.vo.OrderVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/page")
    public Result<PageResult<Order>> page(PageRequest request) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        return Result.success(orderService.findByPage(request));
    }

    @GetMapping("/{id:\\d+}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id, SecurityUtils.getCurrentUserId(), SecurityUtils.isAdmin()));
    }

    @GetMapping("/detail/{id:\\d+}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id, SecurityUtils.getCurrentUserId(), SecurityUtils.isAdmin()));
    }

    @PostMapping
    public Result<String> save(@RequestBody Order order) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        boolean success = orderService.save(order);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Order order) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        boolean success = orderService.update(order);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<String> delete(@PathVariable Long id) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        boolean success = orderService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/status/{id:\\d+}/{status:\\d+}")
    public Result<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        boolean success = orderService.updateStatus(id, status);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }

    @GetMapping("/count")
    public Result<Long> count() {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        return Result.success(orderService.count());
    }

    @GetMapping("/user")
    public Result<List<OrderVO>> getUserOrders() {
        return Result.success(orderService.findUserOrders(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody Order order) {
        return Result.success(orderService.createOrder(SecurityUtils.getCurrentUserId(), order));
    }

    @PutMapping("/pay/{id:\\d+}")
    public Result<String> payOrder(@PathVariable Long id) {
        orderService.payOrder(id, SecurityUtils.getCurrentUserId(), SecurityUtils.isAdmin());
        return Result.success("支付成功");
    }

    @PutMapping("/cancel/{id:\\d+}")
    public Result<String> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id, SecurityUtils.getCurrentUserId(), SecurityUtils.isAdmin());
        return Result.success("订单已取消");
    }

    @PutMapping("/confirm/{id:\\d+}")
    public Result<String> confirmOrder(@PathVariable Long id) {
        orderService.confirmOrder(id, SecurityUtils.getCurrentUserId(), SecurityUtils.isAdmin());
        return Result.success("确认收货成功");
    }
}
