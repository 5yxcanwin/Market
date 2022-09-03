package com.hafa.market.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hafa.market.dto.ArticleDto;
import com.hafa.market.mapper.ArticleMapper;
import com.hafa.market.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ArticleServiceImolTest {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleMapper articleMapper;

    @Test
    public void testArticleMapper(){


        List<ArticleDto> articleList5 = articleMapper.queryAllArticleList(null,"9","1","上门自提" ,null,null);

        System.out.println(articleList5);
        System.out.println(articleList5.size());
    }


    @Test
    public void testPageHelper(){
        PageHelper.startPage(1,2);
        List<ArticleDto> articleList = articleMapper.queryAllArticleList(null,"9","1","上门自提" ,null,null);
        PageInfo<ArticleDto> pageInfo = new PageInfo<>(articleList);
        System.out.println(articleList.size());
    }
}
