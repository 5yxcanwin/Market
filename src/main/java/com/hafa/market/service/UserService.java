package com.hafa.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hafa.market.exceptions.LoginFailedException;
import com.hafa.market.pojo.User;

/**
 * @author heavytiger
 * @version 1.0
 * @description UserService接口
 * @date 2022/4/18 16:58
 */
public interface UserService extends IService<User> {
    String login(String code) throws LoginFailedException;

    User updateUser(User user, User oldUser);

    User uploadUser(User user, User oldUser);
}
