package com.hafa.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hafa.market.exceptions.LoginFailedException;
import com.hafa.market.mapper.UserMapper;
import com.hafa.market.pojo.User;
import com.hafa.market.service.UserService;
import com.hafa.market.util.WxApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author heavytiger
 * @version 1.0
 * @description UserService实现类
 * @date 2022/4/18 16:59
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${config.wechat.appid}")
    private String appid;

    @Value("${config.wechat.secret}")
    private String secret;

    @Value("${config.expire-time}")
    private long expireTime;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String login(String code) throws LoginFailedException{
        String sessionInfo = WxApiUtil.code2session(appid, secret, code, "authorization_code");
        if(sessionInfo.length() == 0) {
            // 登录失败，存在问题
            log.error("UserServiceImpl: 登录失败，服务器可能异常");
            throw new LoginFailedException("登录失败，服务器可能异常");
        }
        JsonNode jsonNode = null;
        try {
            jsonNode = new ObjectMapper().readTree(sessionInfo);
        } catch (JsonProcessingException e) {
            log.error("UserServiceImpl: 解析JSON数据异常");
            throw new LoginFailedException("解析JSON数据异常!");
        }
        if(jsonNode.has("errcode")) {
            throw new LoginFailedException("ErrorCode: " + jsonNode.get("errcode").asInt() +
                    "  ErrorMsg: " + jsonNode.get("errmsg").asText());
        }
        // 此时可以得到用户的openid，使用openid查找数据库，若没有查到，则新建用户信息
        User user = new User();
        user.setUserOpenid(jsonNode.get("openid").asText());
        if(!userMapper.exists(new QueryWrapper<User>().eq("user_openid", user.getUserOpenid()))) {
            // 若不存在当前用户，则新建一条用户数据，通过该用户数据生成JSESSIONID
            userMapper.insert(user);
        } else {
            // 存在当前用户，直接获取
            user = userMapper.selectOne(new QueryWrapper<User>()
                    .eq("user_openid", user.getUserOpenid()));
        }
        String session = UUID.randomUUID().toString().replace("-", "");
        String key = "user:" + session;
        redisTemplate.opsForValue().set(key, user, expireTime, TimeUnit.MILLISECONDS);
        return session;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public User updateUser(User user, User oldUser) {
        user.setUserId(oldUser.getUserId());
        user.setUserOpenid(oldUser.getUserOpenid());
        user.setUserAvatar(oldUser.getUserAvatar());
        user.setUserName(oldUser.getUserName());
        userMapper.updateById(user);
        return user;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public User uploadUser(User user, User oldUser) {
        user.setUserId(oldUser.getUserId());
        user.setUserAddress(oldUser.getUserAddress());
        user.setUserPhone(oldUser.getUserPhone());
        user.setUserEmail(oldUser.getUserEmail());
        user.setUserOpenid(oldUser.getUserOpenid());
        userMapper.updateById(user);
        return user;
    }
}
