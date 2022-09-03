package com.hafa.market.interceptor;

import com.hafa.market.dto.ResultBean;
import com.hafa.market.enums.EnumResult;
import com.hafa.market.pojo.User;
import com.hafa.market.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author heavytiger
 * @version 1.0
 * @description 实现验证用户的headers中是否存在JSESSIONID且是否通过验证，若不存在，返回状态码要求登录，存在放行
 * @date 2021/12/23 19:42
 */
@Slf4j
@Component
public class CheckInterceptor implements HandlerInterceptor {

    @Value("${config.expire-time}")
    private long expireTime;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if("JSESSIONID".equals(cookie.getName())) {
                    String jSessionId = cookie.getValue();
                    String key = "user:" + jSessionId;
                    Object object = redisTemplate.opsForValue().get(key);
                    if(object!=null) {
                        // 说明验证成功，允许访问资源，同时更新缓存时间
                        User user = (User) object;
                        redisTemplate.opsForValue().set(key, user, expireTime, TimeUnit.MILLISECONDS);
                        return true;
                    }
                }
            }
        }
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(JsonUtil.objToJson(new ResultBean<>(EnumResult.SESSION_ERROR)));
        return false;
    }
}
