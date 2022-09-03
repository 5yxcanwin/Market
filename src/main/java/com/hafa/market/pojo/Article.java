package com.hafa.market.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author 5yxcanwin
 * @version 1.0
 * @description 商品实体类，包含商品的信息
 * @date 2022/4/22 22:04
 */
@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long articleId;   //商品id

    private Long articleUserId;  //发布该商品的用户id

    private Boolean articleStatus;  //商品状态 1-已售出 0-未售出

    private Integer articlePicNum;  //商品图片数量

    private Integer articlePrice;  //商品价格

    private String articleTitle;  //商品标题

    private String articleDistribution;  //商品配送方式

    private String articleDescription; //商品描述

    private String articleRegion; //地区标签

    private Integer articleViewCount; //商品浏览量

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Timestamp articleCreateTime; //商品创建时间

    private String articleAttrition; //商品磨损程度
}
