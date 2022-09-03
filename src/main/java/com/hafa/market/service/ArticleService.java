package com.hafa.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.hafa.market.dto.ArticleDto;
import com.hafa.market.pojo.Article;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author heavytiger
 * @version 1.0
 * @description ArticleService接口
 * @date 2022/4/23 16:37
 */
public interface ArticleService extends IService<Article> {

    Article selectPicNumForUpdate(Long articleId);

    Boolean savePicFile(Article article, Integer picNum, MultipartFile file, String suffix);

    Boolean deletePicFile(Article article, Integer picNum);

    PageInfo<ArticleDto> queryArticle(String info, String articleRegion,String articleDistribution,String articleAttrition,Integer lowPrice, Integer highPrice, Integer pageNum, Integer pageSize);

    PageInfo<ArticleDto> querySoldArticle(Long userId,Integer articleStatus,Integer pageNum,Integer pageSize);

    PageInfo<ArticleDto> queryMyWantArticle(Long userId, Integer pageNum, Integer pageSize);

    void deleteArticleDoc(Long id);

    void insertOrUpDateArticleDoc(Long id);
}
