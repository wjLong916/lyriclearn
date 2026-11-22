package com.lyriclearn.user.controller;

import com.lyriclearn.common.ApiResponse;
import com.lyriclearn.user.model.PlatformUser;
import com.lyriclearn.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 用户：内置学员/管理员账号（P0 mock，无注册登录）。 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<PlatformUser>> list() {
        return ApiResponse.ok(userService.all());
    }

    @GetMapping("/{userId}")
    public ApiResponse<PlatformUser> get(@PathVariable String userId) {
        return ApiResponse.ok(userService.byId(userId));
    }
}
