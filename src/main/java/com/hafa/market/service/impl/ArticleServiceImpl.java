package com.hafa.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hafa.market.document.ArticleDoc;
import com.hafa.market.dto.ArticleDto;
import com.hafa.market.mapper.ArticleMapper;
import com.hafa.market.pojo.Article;
import com.hafa.market.service.ArticleService;
import com.hafa.market.util.ImgUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author heavytiger
 * @version 1.0
 * @description ArticleService?????????
 * @date 2022/4/23 16:38
 */
@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Value("${config.picUrl}")
    private String picUrl;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Article selectPicNumForUpdate(Long articleId) {
        return articleMapper.queryPicNum(articleId);
    }

    /**
     * ??????????????????????????????1??????????????????????????????totalNum?????????2???????????????picNum?????????totalNum???????????????totalNum??????
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param article ????????????
     * @param picNum  ???????????????????????????
     * @return ??????????????????true????????????????????????????????????
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Boolean deletePicFile(Article article, Integer picNum) {
        int totalNum = article.getArticlePicNum();
        String curUrl = picUrl + article.getArticleId() + "_";
        File image = new File(curUrl + picNum + ".jpg");
        File imageThumb = new File(curUrl + picNum + "_thumb.jpg");
        if (!image.delete() || !imageThumb.delete()) {
            log.error("??????" + image.getAbsolutePath() + "???????????????");
            throw new RuntimeException("?????????????????????");
        }
        if (!picNum.equals(totalNum)) {
            // picNum + 1???totalNum??????reName????????????
            for (int i = picNum + 1; i <= totalNum; i++) {
                File curImage = new File(curUrl + i + ".jpg");
                File curImageThumb = new File(curUrl + i + "_thumb.jpg");
                if (!curImage.renameTo(image) || !curImageThumb.renameTo(imageThumb)) {
                    log.error("??????" + curImage.getAbsolutePath() + "???????????????????????????");
                    throw new RuntimeException("???????????????????????????");
                } else {
                    image = curImage;
                    imageThumb = curImageThumb;
                }
            }
        }
        article.setArticlePicNum(totalNum - 1);
        articleMapper.updateById(article);
        return true;
    }

    /**
     * ????????????????????????
     *
     * @param article ???????????????
     * @param picNum  ??????null??????????????????????????????????????????
     * @param file    ???????????????
     * @param suffix  ????????????
     * @return ????????????????????????true???????????????false
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Boolean savePicFile(Article article, Integer picNum, MultipartFile file, String suffix) {
        int curIndex;
        String curUrl;
        File image, imageThumb;

        if (picNum == null) {
            curIndex = article.getArticlePicNum() + 1;
            article.setArticlePicNum(curIndex);
            curUrl = picUrl + article.getArticleId() + "_" + curIndex;
            image = new File(curUrl + ".jpg");
            imageThumb = new File(curUrl + "_thumb" + ".jpg");
            // ??????article
            articleMapper.updateById(article);
        } else if (picNum <= 0 || picNum > article.getArticlePicNum()) {
            return false;
        } else {
            curIndex = picNum;
            curUrl = picUrl + article.getArticleId() + "_" + curIndex;
            image = new File(curUrl + ".jpg");
            imageThumb = new File(curUrl + "_thumb" + ".jpg");
        }
        if (!image.getParentFile().exists()) {
            // ????????????????????????????????????????????????????????????
            image.getParentFile().mkdirs();
        }
        if (image.exists()) {
            image.delete();
            imageThumb.delete();
        }
        if (".png".equals(suffix)) {
            try (InputStream in = new ByteArrayInputStream(file.getBytes())) {
                ImgUtil.png2jpg(in, image);
            } catch (IOException e) {
                log.error("?????????????????????????????????" + image.getAbsolutePath());
                throw new RuntimeException("?????????????????????");
            }
        } else {
            try (OutputStream out = new FileOutputStream(image)) {
                out.write(file.getBytes());
            } catch (IOException e) {
                log.error("?????????????????????????????????" + image.getAbsolutePath());
                // ??????????????????
                throw new RuntimeException("?????????????????????");
            }
        }
        // ???????????????????????????????????????300?????????????????????
        try {
            Thumbnails.of(image)
                    .width(300)
                    .keepAspectRatio(true)
                    .toFile(imageThumb);
        } catch (IOException e) {
            log.error("???????????????????????????????????????" + imageThumb.getAbsolutePath());
            // ??????????????????
            throw new RuntimeException("???????????????????????????");
        }
        return true;
    }

    /**
     * ????????????
     * @param info
     * @param articleRegion
     * @param lowPrice
     * @param highPrice
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<ArticleDto> queryArticle(String info, String articleRegion, String articleDistribution, String articleAttrition, Integer lowPrice, Integer highPrice, Integer pageNum, Integer pageSize) {
            // ??????????????????
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("articlePrice");
        PageRequest pageRequest = PageRequest.of(pageNum-1, pageSize);
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("articleTitle").field("articleDescription");

        if (StringUtils.isNotBlank(info)) {
            boolQueryBuilder.must(new BoolQueryBuilder()
                    .should(QueryBuilders.matchQuery("articleTitle",info))
                    .should(QueryBuilders.matchQuery("articleDescription",info)));
        }

        if (StringUtils.isNotBlank(articleRegion)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("articleRegion", articleRegion));
        }

        if (StringUtils.isNotBlank(articleDistribution)) {
            boolQueryBuilder.must(QueryBuilders.termQuery("articleDistribution", articleDistribution));
        }

        if(StringUtils.isNotBlank(articleAttrition)){
            boolQueryBuilder.must(QueryBuilders.termQuery("articleAttrition",articleAttrition));
        }

        if(lowPrice!=null){
            rangeQueryBuilder.gte(lowPrice);
        }

        if(highPrice!=null){
            rangeQueryBuilder.lte(highPrice);
        }

        boolQueryBuilder.filter(rangeQueryBuilder);

        queryBuilder.withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withHighlightBuilder(highlightBuilder)
                .withSorts(SortBuilders.fieldSort("articleId").order(SortOrder.DESC));

        SearchHits<ArticleDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), ArticleDoc.class);


        List<ArticleDto> articleList = new ArrayList<>();
        for (SearchHit<ArticleDoc> searchHit : searchHits) {
            ArticleDoc articleDoc = searchHit.getContent();
            //????????????
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            for(Map.Entry<String,List<String>> entry :highlightFields.entrySet()){
                String key = entry.getKey();
                if (StringUtils.equals(key, "articleTitle")) {
                    List<String> fragments = entry.getValue();
                    StringBuilder sb = new StringBuilder();
                    for (String fragment : fragments) {
                        sb.append(fragment);
                    }
                    articleDoc.setArticleTitle(sb.toString());
                }
                if (StringUtils.equals(key, "articleDescription")) {
                    List<String> fragments = entry.getValue();
                    StringBuilder sb = new StringBuilder();
                    for (String fragment : fragments) {
                        sb.append(fragment);
                    }
                    articleDoc.setArticleDescription(sb.toString());
                }
            }
            ArticleDto articleDto = new ArticleDto(articleDoc);
            articleList.add(articleDto);
        }

        return new PageInfo<>(articleList);
    }

    @Override
    public PageInfo<ArticleDto> querySoldArticle(Long userId,Integer articleStatus,Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<ArticleDto> articles = articleMapper.selectSoldArticle(userId,articleStatus);
        return new PageInfo<>(articles);
    }

    @Override
    public PageInfo<ArticleDto> queryMyWantArticle(Long userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<ArticleDto> articles = articleMapper.selectMyWantArticle(userId);
        return new PageInfo<>(articles);
    }

    @Override
    public void deleteArticleDoc(Long id) {
        elasticsearchRestTemplate.delete(id.toString(), ArticleDoc.class);
    }

    @Override
    public void insertOrUpDateArticleDoc(Long id) {
        //MyBatisPlus???????????????
        Article article = getById(id);
        ArticleDoc articleDoc = new ArticleDoc(article);

        //?????????????????????
        IndexQueryBuilder indexQueryBuilder = new IndexQueryBuilder().withId(id.toString()).withObject(articleDoc);
        elasticsearchRestTemplate.index(indexQueryBuilder.build(), IndexCoordinates.of("article"));
    }
}
