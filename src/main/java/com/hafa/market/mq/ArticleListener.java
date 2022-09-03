package com.hafa.market.mq;

import com.hafa.market.enums.MqConstants;
import com.hafa.market.service.ArticleService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArticleListener {

    @Autowired
    private ArticleService articleService;

    @RabbitListener(queues = MqConstants.ARTCILE_INSERT_QUEUE)
    public void listenArtcileInsertOrUpdate(Long articleId){
        articleService.insertOrUpDateArticleDoc(articleId);
    }

    @RabbitListener(queues = MqConstants.ARTCILE_DELETE_QUEUE)
    public void listenArtcileDelete(Long articleId){
      articleService.deleteArticleDoc(articleId);
    }
}
