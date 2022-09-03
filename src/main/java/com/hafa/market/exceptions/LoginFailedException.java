package com.hafa.market.exceptions;

/**
 * @author heavytiger
 * @version 1.0
 * @description 登录失败异常
 * @date 2022/4/19 15:05
 */
public class LoginFailedException extends BaseException{
    public LoginFailedException(String msg) {
        super(msg);
    }
}
