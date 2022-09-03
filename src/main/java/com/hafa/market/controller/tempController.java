package com.hafa.market.controller;

import com.hafa.market.dto.ResultBean;
import com.hafa.market.enums.EnumResult;
import com.hafa.market.pojo.User;
import com.hafa.market.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author heavytiger
 * @version 1.0
 * @description 该类便于前端测试接口，在项目正式上线后将删除该类
 * @date 2022/5/22 21:38
 */
@Slf4j
@RestController
@RequestMapping("/temp")
public class tempController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/getSessionId")
    public ResultBean<Object> getSessionId(@RequestParam("userId") Long userId,
                                           @RequestParam(value = "expireTime", defaultValue = "7200000") Long expireTime) {
        User user = userService.getById(userId);
        if(user == null) {
            return new ResultBean<>(EnumResult.FAIL);
        }
        String session = UUID.randomUUID().toString().replace("-", "");
        String key = "user:" + session;
        redisTemplate.opsForValue().set(key, user, expireTime, TimeUnit.MILLISECONDS);
        return new ResultBean<>(EnumResult.SESSION_SUCCESS, session);
    }
}
