package com.hafa.market.annotation;

import java.lang.annotation.*;

/**
 * 只有接口加上该注解才能被我们的Aop监控到
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ArticleView {
    /**
     * 描述
     */
    String description()  default "";
}
