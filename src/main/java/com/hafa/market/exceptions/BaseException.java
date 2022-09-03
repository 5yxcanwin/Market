package com.hafa.market.exceptions;

/**
 * @author heavytiger
 * @version 1.0
 * @description BaseException异常
 * @date 2022/4/19 15:02
 */
public class BaseException extends RuntimeException{
    public BaseException() {
        super();
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }
}
