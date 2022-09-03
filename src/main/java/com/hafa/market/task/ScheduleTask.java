package com.hafa.market.task;

import com.hafa.market.pojo.Article;
import com.hafa.market.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Configuration
@EnableScheduling
@Slf4j
public class ScheduleTask {


    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private ArticleService articleService;
    /**
     * 定时更新问题浏览量到数据库中
     * 每天凌晨两点跑一次
     */

//    @Scheduled(cron = "0/5 0/1 * * * ?")
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void updateArticleView() {
        // 获取全部的key
        String pattern = "articleId:*";
        Set<String> keys = this.redisTemplate.keys(pattern);
        for (String key : keys) {
            Long redisViewCount = redisTemplate.opsForHyperLogLog().size(key);
            // 将key拆分
            String[] split = key.split(":");
            // 根据问题id获取
            Article article = articleService.getById(split[1]);
            // 更改浏览量
            article.setArticleViewCount(redisViewCount.intValue() + article.getArticleViewCount());
            articleService.updateById(article);
            // 删除key
            redisTemplate.delete(key);
        }
    }
}
