package com.hafa.market.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hafa.market.annotation.ArticleView;
import com.hafa.market.document.ArticleDoc;
import com.hafa.market.dto.ArticleDto;
import com.hafa.market.mapper.ArticleMapper;
import com.hafa.market.pojo.Article;
import com.hafa.market.service.ArticleService;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class EsTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleMapper articleMapper;


    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;



    @Test
    public void testEs1() throws IOException {

        PageRequest pageRequest = PageRequest.of(0, 5);
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.must(QueryBuilders.termQuery("articleRegion", "上海虹桥"));

         queryBuilder.withQuery(boolQueryBuilder).withPageable(pageRequest).withSorts(SortBuilders.fieldSort("articleId").order(SortOrder.DESC));
        SearchHits<ArticleDoc> search = elasticsearchRestTemplate.search(queryBuilder.build(), ArticleDoc.class);

        List<ArticleDto> articleList = new ArrayList<>();
        for (SearchHit<ArticleDoc> searchHit : search) {
            ArticleDoc articleDoc = searchHit.getContent();
            ArticleDto articleDto = new ArticleDto(articleDoc);
            articleList.add(articleDto);
        }

        System.out.println(articleList);
    }


    @Test
    public void  testEs2(){
         String info = "";
         String articleRegion = "上海虹桥";
         String articleDistribution = "";
         String articleAttrition = "";
         Integer lowPrice = null;
         Integer highPrice = null;
         Integer pageNum = 1;
         Integer pageSize = 5;
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("articlePrice");
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);

//        if (StringUtils.isNotBlank(info)) {
//            boolQueryBuilder.must(new BoolQueryBuilder()
//                    .should(QueryBuilders.matchQuery("articleTitle",info))
//                    .should(QueryBuilders.matchQuery("articleDescription",info)));
//        }

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


        // boolQueryBuilder.filter(rangeQueryBuilder);

        queryBuilder.withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                .withSorts(SortBuilders.fieldSort("articleId").order(SortOrder.DESC));

        SearchHits<ArticleDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), ArticleDoc.class);

        List<ArticleDto> articleList = new ArrayList<>();
        for (SearchHit<ArticleDoc> searchHit : searchHits) {
            ArticleDoc articleDoc = searchHit.getContent();
            ArticleDto articleDto = new ArticleDto(articleDoc);
            articleList.add(articleDto);
        }

        System.out.println(articleList);
    }

    @Test
    void testEsDelete(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.termQuery("articleId",3));
//        SearchHits<ArticleDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), ArticleDoc.class);
//
//        for(SearchHit<ArticleDoc> searchHit : searchHits){
//            System.out.println(searchHit.getContent());
//        }


      //  ByQueryResponse response = elasticsearchRestTemplate.delete(queryBuilder.build());

        //System.out.println(response.getDeleted());
//        ArticleDoc articleDoc = new ArticleDoc();
//        articleDoc.setArticleId(5L);
//        IndexQueryBuilder builder = new IndexQueryBuilder().withId(articleDoc.getArticleId().toString()).withObject(articleDoc);
//        elasticsearchRestTemplate.index(builder.build(),IndexCoordinates.of("article"));
//          NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//          queryBuilder.withQuery(QueryBuilders.termQuery("articleId",5));
//        SearchHit<ArticleDoc> searchHit = elasticsearchRestTemplate.searchOne(queryBuilder.build(), ArticleDoc.class);
//            System.out.println(searchHit.getContent());

    }

    @Test
    void testEsInsert(){
        //MyBatisPlus查询数据库
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id", 2);
        Article article = articleMapper.selectOne(queryWrapper);
        System.out.println(article);
        ArticleDoc articleDoc = new ArticleDoc(article);
        articleDoc.setArticleId(5L);
        //新增或更新文档
        IndexQueryBuilder indexQueryBuilder = new IndexQueryBuilder().withId("2").withObject(articleDoc);
        elasticsearchRestTemplate.index(indexQueryBuilder.build(), IndexCoordinates.of("article"));
    }
}
