package com.hafa.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hafa.market.dto.ArticleDto;
import com.hafa.market.pojo.Article;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author heavytiger
 * @version 1.0
 * @description 商品相关的Mapper层
 * @date 2022/4/23 16:35
 */
@Repository
public interface ArticleMapper extends BaseMapper<Article> {

    Article queryPicNum(Long articleId);

    List<ArticleDto> queryAllArticleList(@Param("info") String info, @Param("articleRegion") String articleRegion, @Param("articleDistribution")String articleDistribution, @Param("articleAttrition")String articleAttrition, @Param("lowPrice") Integer lowPrice, @Param("highPrice") Integer highPrice);

    List<ArticleDto> selectSoldArticle(@Param("userId")Long userId,@Param("articleStatus") Integer articleStatus);

    List<ArticleDto> selectMyWantArticle(Long userId);
}
