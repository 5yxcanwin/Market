package com.hafa.market.service.impl;

import com.hafa.market.pojo.User;
import com.hafa.market.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author heavytiger
 * @version 1.0
 * @description 模拟用户进行登录，用于进行功能测试
 * @date 2022/4/24 19:49
 */
@SpringBootTest
class LoginTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void userLogin() {
        // 模拟一号用户登录，用该用户的UUID进行登录
        User user = userService.getById(1);
        System.out.println(user);
        String session = UUID.randomUUID().toString().replace("-", "");
        System.out.println("UserTest的JSESSIONID为：" + session);
        String key = "user:" + session;
        redisTemplate.opsForValue().set(key, user, 30L * 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
    }
}
