package com.hafa.market.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hafa.market.document.ArticleDoc;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleDto{

    private Long articleId;   //商品id

    private Integer articlePicNum;  //商品图片数量

    private Integer articlePrice;  //商品价格

    private String articleTitle;  //商品标题

    private String articleDescription; //商品描述

    private Integer articleViewCount; //商品浏览量

    public ArticleDto(){}

    public ArticleDto(ArticleDoc articleDoc){
        articleId = articleDoc.getArticleId();
        articlePicNum = articleDoc.getArticlePicNum();
        articlePrice = articleDoc.getArticlePrice();
        articleTitle = articleDoc.getArticleTitle();
        articleDescription = articleDoc.getArticleDescription();
        articleViewCount = articleDoc.getArticleViewCount();
    }

}
