package com.hafa.market.document;

import com.hafa.market.pojo.Article;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "article")
public class ArticleDoc {

    @Id
    private Long articleId;   //商品id

    @Field(type = FieldType.Integer,index = false)
    private Integer articlePicNum;  //商品图片数量

    @Field(type = FieldType.Integer)
    private Integer articlePrice;  //商品价格

    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String articleTitle;  //商品标题

    @Field(type = FieldType.Keyword,copyTo = "all")
    private String articleDistribution;  //商品配送方式

    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String articleDescription; //商品描述

    @Field(type = FieldType.Keyword,copyTo = "all")
    private String articleRegion; //地区标签

    @Field(type = FieldType.Integer,index = false)
    private Integer articleViewCount; //商品浏览量

    @Field(type = FieldType.Keyword,copyTo = "all")
    private String articleAttrition; //商品磨损程度

    @Field(type = FieldType.Text,analyzer = "ik_smart")
    private String condition;

    public ArticleDoc(){}

    public ArticleDoc(Article article){
        this.articleId = article.getArticleId();
        this.articlePicNum = article.getArticlePicNum();
        this.articlePrice = article.getArticlePrice();
        this.articleDescription = article.getArticleDescription();
        this.articleAttrition = article.getArticleAttrition();
        this.articleDistribution = article.getArticleDistribution();
        this.articleRegion = article.getArticleRegion();
        this.articleViewCount = article.getArticleViewCount();
        this.articleTitle = article.getArticleTitle();
    }
}
