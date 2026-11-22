package com.lyriclearn.user.model;

/**
 * 平台用户（P0 mock：内置账号，无注册登录）。
 * role: STUDENT 学员 / ADMIN 管理员。
 */
public record PlatformUser(
        String userId,
        String name,
        String role,
        String cefr,
        String motto) {
}
