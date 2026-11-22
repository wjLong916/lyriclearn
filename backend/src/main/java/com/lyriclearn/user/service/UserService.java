package com.lyriclearn.user.service;

import com.lyriclearn.common.BizException;
import com.lyriclearn.user.model.PlatformUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务（P0 mock）：内置学员/管理员账号，供前端切换身份；不做注册登录与密码。
 */
@Service
public class UserService {

    private final List<PlatformUser> users = List.of(
            new PlatformUser("u-demo", "小迪（学员）", "STUDENT", "A2", "用歌曲和短文把英语学起来"),
            new PlatformUser("u-lily", "Lily（学员）", "STUDENT", "B1", "喜欢新闻短文与名人演讲"),
            new PlatformUser("u-admin", "管理员", "ADMIN", "B2", "负责资源与路径编排"));

    public List<PlatformUser> all() {
        return users;
    }

    public PlatformUser byId(String userId) {
        Optional<PlatformUser> found = users.stream().filter(u -> u.userId().equals(userId)).findFirst();
        return found.orElseThrow(() -> new BizException(404, "用户不存在：" + userId));
    }
}
