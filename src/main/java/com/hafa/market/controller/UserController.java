package com.hafa.market.controller;

import com.hafa.market.dto.ResultBean;
import com.hafa.market.dto.UserDto;
import com.hafa.market.enums.EnumResult;
import com.hafa.market.exceptions.LoginFailedException;
import com.hafa.market.pojo.Purchase;
import com.hafa.market.pojo.User;
import com.hafa.market.service.PurchaseService;
import com.hafa.market.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author heavytiger
 * @version 1.0
 * @description UserController处理微信用户登录
 * @date 2022/4/18 17:20
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PurchaseService purchaseService;

    @Value("${config.expire-time}")
    private long expireTime;

    /**
     * 登录接口，提供openId进行登录
     *
     * @param code 前端body体中传递的参数js_code
     * @return 若成功登录，返回JSESSIONID，下次登陆时附带登录，若登录失败
     */
    @PostMapping("/login")
    public ResultBean<Object> doLogin(@CookieValue(value = "JSESSIONID", required = false) String jSessionId,
                                      @RequestBody String code) {
        log.info("---------------------doLogin被调用-----------------------");
        // 首先判断Redis中JSESSIONID没有过期
        if (redisTemplate.hasKey("user:" + jSessionId)) {
            // 再设置为EXPIRE_TIME
            redisTemplate.expire("user:" + jSessionId, expireTime, TimeUnit.MILLISECONDS);
            return new ResultBean<>(EnumResult.SESSION_RESET);
        }
        String session;
        try {
            session = userService.login(code);
        } catch (LoginFailedException e) {
            log.error("doLogin失败！");
            return new ResultBean<>(EnumResult.LOGIN_ERROR, e.getMessage());
        }
        log.info("--------------------doLogin调用结束-----------------------");
        return new ResultBean<>(EnumResult.SESSION_SUCCESS, session);
    }

    /**
     * 用户更新手机号、地址、邮箱
     *
     * @param jSessionId
     * @param user
     * @return
     */
    @PostMapping("/update")
    public ResultBean<Object> doUpdate(@CookieValue(value = "JSESSIONID", required = true) String jSessionId,
                                       @RequestBody User user) {
        log.info("---------------------doUpdate被调用-----------------------");
        //获取需要更新的用户
        String key = "user:" + jSessionId;
        User oldUser = (User) redisTemplate.opsForValue().get(key);

        //更新用户信息并返回修改之后的user
        User newUser = userService.updateUser(user, oldUser);
        Long userIdkey = newUser.getUserId();

        //key为useId value为user存入redis，方便后续查找其他用户
        redisTemplate.opsForValue().set("userId:" + userIdkey, newUser, expireTime, TimeUnit.MILLISECONDS);
        //更新key为user:sessionid value为user的redis缓存
        redisTemplate.opsForValue().set("user:" + jSessionId, newUser, expireTime, TimeUnit.MILLISECONDS);
        log.info("--------------------doUpdate调用结束-----------------------");
        return new ResultBean<>(EnumResult.UPDATE_SUCCESS);
    }

    /**
     * 用户上传头像、昵称
     *
     * @param jSessionId
     * @param user
     * @return
     */
    @PostMapping("/upload")
    public ResultBean<Object> doUpload(@CookieValue(value = "JSESSIONID", required = true) String jSessionId,
                                       @RequestBody User user) {
        log.info("---------------------doUpload被调用-----------------------");
        //获取需要更新的用户
        String key = "user:" + jSessionId;
        User oldUser = (User) redisTemplate.opsForValue().get(key);

        //更新用户信息并返回修改之后的user
        User newUser = userService.uploadUser(user, oldUser);
        Long userIdkey = newUser.getUserId();

        //key为useId value为user存入redis，方便后续查找其他用户
        redisTemplate.opsForValue().set("userId:" + userIdkey, newUser, expireTime, TimeUnit.MILLISECONDS);
        //更新key为user:sessionid value为user的redis缓存
        redisTemplate.opsForValue().set("user:" + jSessionId, newUser, expireTime, TimeUnit.MILLISECONDS);
        log.info("--------------------doUpload调用结束-----------------------");
        return new ResultBean<>(EnumResult.UPLOAD_SUCCESS);
    }

    /**
     * 查找用户信息
     *
     * @param articleUserId
     * @return
     */
    @GetMapping("/listUser")
    public ResultBean<Object> queryUser(@CookieValue(value = "JSESSIONID") String jSessionId,
                                        @RequestParam(required = false) Long articleUserId) {
        String key;
        if (articleUserId == null) {
            key = "user:" + jSessionId;
        } else {
            key = "userId:" + articleUserId;
        }

        User user = (User) redisTemplate.opsForValue().get(key);

        if (user == null) {
            user = userService.getById(articleUserId);
        }
        UserDto userDto = new UserDto();
        userDto.setUserId(articleUserId);
        userDto.setUserAvatar(user.getUserAvatar());
        userDto.setUserEmail(user.getUserEmail());
        userDto.setUserName(user.getUserName());
        userDto.setUserPhone(user.getUserPhone());
        userDto.setUserAddress(user.getUserAddress());

        return new ResultBean<>(EnumResult.QUERY_SUCCESS, userDto);
    }

    @PostMapping("/wantBuy/{articleId}/{wantBuy}")
    public ResultBean<Object> wantBuyArticle(@CookieValue(value = "JSESSIONID") String jSessionId,
                                             @PathVariable Long articleId, @PathVariable Boolean wantBuy) {
        //查找用户id
        String key = "user:" + jSessionId;
        User user = (User) redisTemplate.opsForValue().get(key);
        Long userId = user.getUserId();
        Purchase purchase = new Purchase();
        purchase.setPurchaseUserId(userId);
        purchase.setPurchaseArticleId(articleId);

        Boolean flag = purchaseService.getPurchase(articleId, userId);
        //未点击过想要购买但想要购买
        if (!flag && wantBuy) {
            purchaseService.save(purchase);
            return new ResultBean<>(EnumResult.UPLOAD_SUCCESS);
        }//点击过想要购买但想取消购买
        else if (flag && !wantBuy) {
            purchaseService.removePurchase(articleId, userId);
            return new ResultBean<>(EnumResult.DELETE_SUCCESS);
        }

        return new ResultBean<>(EnumResult.WANTBUY_ERROR);
    }


    /**
     * 根据jSessinId得到用户id
     * @param jSessionId
     * @return
     */
    @GetMapping("/getId")
    public ResultBean<Object> getUserId(@CookieValue(value = "JSESSIONID") String jSessionId){
        String key = "user:" + jSessionId;
        User user = (User)redisTemplate.opsForValue().get(key);
        return new ResultBean<>(EnumResult.QUERY_SUCCESS,user.getUserId());
    }


    /**
     * 查询想要购买该商品的用户
     * @param jSessionId
     * @param articleId
     * @return
     */
    @GetMapping("/purchase")
    public ResultBean<Object> getWantBuy(@CookieValue(value = "JSESSIONID") String jSessionId,
                                         @RequestParam Long articleId) {
        List<Long> userList = purchaseService.listWantBuyUser(articleId);
        return new ResultBean<>(EnumResult.QUERY_SUCCESS, userList);
    }

}
