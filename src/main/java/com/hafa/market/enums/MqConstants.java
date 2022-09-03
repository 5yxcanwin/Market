package com.hafa.market.enums;

public class MqConstants {

    //交换机
    public final static String ARTCILE_EXCHANGE = "article.topic";

    //监听新增和修改队列
    public final static String ARTCILE_INSERT_QUEUE = "article.insert.queue";

    //监听删除队列
    public final static String ARTCILE_DELETE_QUEUE = "article.delete.queue";

    //新增或修改的RoutingKey
    public final static String ARTCILE_INSERT_KEY = "article.insert";

    //删除的RoutingKey
    public final static String ARTCILE_DELETE_KEY = "article.delete";
}
