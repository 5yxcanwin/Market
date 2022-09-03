package com.hafa.market.config;

import com.hafa.market.enums.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(MqConstants.ARTCILE_EXCHANGE,true,false);
    }

    @Bean
    public Queue insertQueue(){
        return new Queue(MqConstants.ARTCILE_INSERT_QUEUE,true);
    }

    @Bean
    public Queue deleteQueue(){
        return new Queue(MqConstants.ARTCILE_DELETE_QUEUE,true);
    }

    @Bean
    public Binding insertQueueBinding(){
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(MqConstants.ARTCILE_INSERT_KEY);
    }

    @Bean
    public Binding deleteQueueBinding(){
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(MqConstants.ARTCILE_DELETE_KEY);
    }
}
