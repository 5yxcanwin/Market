package com.hafa.market.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Aspect
@Slf4j
@Configuration
public class ArticleViewAspect {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;


    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.hafa.market.annotation.ArticleView)")
    public void questionViewPointCut(){}


    /**
     * 切入处理，环绕通知
     * @param joinPoint
     * @return
     */
    @Around("questionViewPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String jSessionId = (String) args[0];
        Object articleId = args[1];
        Object obj = null;
        try {
            // 设置存入的key
            String key = "articleId:" + articleId;
            // 将存入到缓存中
            Long count = redisTemplate.opsForHyperLogLog().add(key, jSessionId);
            if (count == 0){
                log.info("该用户访问量已经访问过了");
            }
            obj = joinPoint.proceed();
        }catch (Exception e){
            e.fillInStackTrace();
        }
        return obj;
    }
}
