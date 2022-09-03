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
 * @description ArticleService实现类
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
     * 删除策略有以下几种，1删除末尾，直接数据库totalNum减一，2删除中间，picNum之后到totalNum前移一位，totalNum减一
     * 如果只有一张图应该禁止用户做删除操作，只能替换，否则将出现无图片的情况
     *
     * @param article 物品帖类
     * @param picNum  需要删除的图片编号
     * @return 删除成功返回true，否则抛出运行时异常回滚
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Boolean deletePicFile(Article article, Integer picNum) {
        int totalNum = article.getArticlePicNum();
        String curUrl = picUrl + article.getArticleId() + "_";
        File image = new File(curUrl + picNum + ".jpg");
        File imageThumb = new File(curUrl + picNum + "_thumb.jpg");
        if (!image.delete() || !imageThumb.delete()) {
            log.error("图片" + image.getAbsolutePath() + "删除失败！");
            throw new RuntimeException("删除图片异常！");
        }
        if (!picNum.equals(totalNum)) {
            // picNum + 1至totalNum需要reName至前一位
            for (int i = picNum + 1; i <= totalNum; i++) {
                File curImage = new File(curUrl + i + ".jpg");
                File curImageThumb = new File(curUrl + i + "_thumb.jpg");
                if (!curImage.renameTo(image) || !curImageThumb.renameTo(imageThumb)) {
                    log.error("图片" + curImage.getAbsolutePath() + "迭代修改名称错误！");
                    throw new RuntimeException("迭代修改名称错误！");
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
     * 上传图片到服务器
     *
     * @param article 添加的产品
     * @param picNum  若为null，则为附加提交，否则修改图片
     * @param file    上传的文件
     * @param suffix  文件后缀
     * @return 若上传成功，返回true，否则返回false
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
            // 修改article
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
            // 检测是否存在父目录，若不存在，创建父目录
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
                log.error("图片写入异常，路径为：" + image.getAbsolutePath());
                throw new RuntimeException("图片写入异常！");
            }
        } else {
            try (OutputStream out = new FileOutputStream(image)) {
                out.write(file.getBytes());
            } catch (IOException e) {
                log.error("图片写入异常，路径为：" + image.getAbsolutePath());
                // 抛出异常回滚
                throw new RuntimeException("图片写入异常！");
            }
        }
        // 压缩图片为缩略图，保证宽为300，高保持纵横比
        try {
            Thumbnails.of(image)
                    .width(300)
                    .keepAspectRatio(true)
                    .toFile(imageThumb);
        } catch (IOException e) {
            log.error("生成压缩图片异常，路径为：" + imageThumb.getAbsolutePath());
            // 抛出异常回滚
            throw new RuntimeException("生成压缩图片异常！");
        }
        return true;
    }

    /**
     * 查询商品
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
            // 查询全部用户
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
            //处理高亮
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
        //MyBatisPlus查询数据库
        Article article = getById(id);
        ArticleDoc articleDoc = new ArticleDoc(article);

        //新增或更新文档
        IndexQueryBuilder indexQueryBuilder = new IndexQueryBuilder().withId(id.toString()).withObject(articleDoc);
        elasticsearchRestTemplate.index(indexQueryBuilder.build(), IndexCoordinates.of("article"));
    }
}
