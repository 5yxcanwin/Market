package com.hafa.market.service.impl;

import com.hafa.market.pojo.Article;
import com.hafa.market.pojo.User;
import com.hafa.market.service.ArticleService;
import com.hafa.market.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author heavytiger
 * @version 1.0
 * @description 测试MyBatis_plus功能
 * @date 2022/4/18 17:02
 */
@SpringBootTest
class UserServiceImplTest {
    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUserOpenid("openid test123");
        boolean res = userService.save(user);
        System.out.println("生成是否成功：" + res);
    }

    @Test
    public void testRedis(){
        User user1 = new User();
        user1.setUserId(1L);
        user1.setUserName("ll");
        user1.setUserAvatar("xxx");
        user1.setUserOpenid("xqq");
        redisTemplate.opsForValue().set("user:1e51d386b9789247a7e8b1b8e19a17fb",user1);
        Object o = redisTemplate.opsForValue().get("user:1e51d386b9789247a7e8b1b8e19a17fb");
        User user = (User) o;
        System.out.println(user);
    }

    @Test
    public void testArticleInsert() {
        Article article = new Article();
        article.setArticleViewCount(10);
        boolean save = articleService.save(article);
        System.out.println(article.getArticleId());
    }
}