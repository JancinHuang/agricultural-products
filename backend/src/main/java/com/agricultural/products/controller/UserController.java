package com.agricultural.products.controller;

import com.agricultural.products.common.PageRequest;
import com.agricultural.products.common.PageResult;
import com.agricultural.products.common.Result;
import com.agricultural.products.common.SecurityUtils;
import com.agricultural.products.dto.UpdatePasswordRequest;
import com.agricultural.products.entity.User;
import com.agricultural.products.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public Result<PageResult<User>> list(PageRequest request) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        PageResult<User> result = userService.findByPage(request);
        result.getList().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @GetMapping("/{id:\\d+}")
    public Result<User> getById(@PathVariable Long id) {
        if (!SecurityUtils.isAdmin() && !id.equals(SecurityUtils.getCurrentUserId())) {
            return Result.error(403, "无权访问该用户信息");
        }
        User user = userService.findById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<String> save(@RequestBody User user) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        boolean success = userService.register(user);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody User user) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        boolean success = userService.update(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<String> delete(@PathVariable Long id) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        boolean success = userService.deleteById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/status/{id:\\d+}/{status:\\d+}")
    public Result<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        boolean success = userService.updateStatus(id, status);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }

    @GetMapping("/count")
    public Result<Long> count() {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权操作");
        }
        return Result.success(userService.count());
    }

    @PutMapping("/info")
    public Result<String> updateInfo(@RequestBody User user) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        user.setId(currentUserId);
        user.setUsername(null);
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        boolean success = userService.updateInfo(user);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    @PutMapping("/password")
    public Result<String> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        boolean success = userService.updatePassword(
                SecurityUtils.getCurrentUserId(),
                request.getOldPassword(),
                request.getNewPassword()
        );
        return success ? Result.success("密码修改成功") : Result.error("原密码错误");
    }
}
