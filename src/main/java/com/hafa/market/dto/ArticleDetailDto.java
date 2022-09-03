package com.hafa.market.dto;

import com.hafa.market.pojo.Article;
import lombok.Data;

@Data
public class ArticleDetailDto {
    private Boolean articlePurchase; //是否想要购买 1-想购买 0-不想购买

    private Article article;

    private Integer purchaseNum;
}
