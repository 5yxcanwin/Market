package com.hafa.market.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageInfo;
import com.hafa.market.annotation.ArticleView;
import com.hafa.market.document.ArticleDoc;
import com.hafa.market.dto.ArticleDetailDto;
import com.hafa.market.dto.ArticleDto;
import com.hafa.market.dto.ResultBean;
import com.hafa.market.enums.EnumResult;
import com.hafa.market.enums.MqConstants;
import com.hafa.market.pojo.Article;
import com.hafa.market.pojo.User;
import com.hafa.market.service.ArticleService;
import com.hafa.market.service.PurchaseService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author heavytiger
 * @version 1.0
 * @description ArticleController控制器
 * @date 2022/4/23 20:50
 */

@Slf4j
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Value("${config.picUrl}")
    private String picUrl;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;




    /**
     * 上传单个图片文件，也可以用于覆盖后端的图片文件，若需覆盖图片文件，则需提供picNum
     *
     * @param file       上传的图片文件，必须提供
     * @param articleId  上传图片的文章id，必须提供
     * @param picNum     若提供，可以覆盖该图片
     * @param jSessionId 用户的JSESSIONID，必须提供，用以鉴权
     * @return 返回状态码
     */
    @PostMapping("/uploadImage")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResultBean<Object> uploadFile(@RequestParam("form-data") MultipartFile file,
                                         @RequestParam("articleId") Long articleId,
                                         @RequestParam(value = "picNum", required = false) Integer picNum,
                                         @CookieValue(value = "JSESSIONID") String jSessionId) {
        String key = "user:" + jSessionId;
        User user = (User) redisTemplate.opsForValue().get(key);
        // 使用悲观锁(SELECT FOR UPDATE)获得数据，当前事务持有X锁阻塞其他事务读，避免picNum出现提交覆盖的情况
        Article article = articleService.selectPicNumForUpdate(articleId);
        if (user != null && !article.getArticleUserId().equals(user.getUserId())) {
            return new ResultBean<>(EnumResult.ARTICLE_ERROR);
        }
        if (file.isEmpty()) {
            return new ResultBean<>(EnumResult.PICTYPE_ERROR);
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (".jpeg".equals(suffix) || ".jpg".equals(suffix)) {
            suffix = ".jpg";
        } else if (".png".equals(suffix)) {
            suffix = ".png";
        } else {
            return new ResultBean<>(EnumResult.PICTYPE_ERROR);
        }
        if(articleService.savePicFile(article, picNum, file, suffix)) {
            //发送修改消息
            rabbitTemplate.convertAndSend(MqConstants.ARTCILE_EXCHANGE,MqConstants.ARTCILE_INSERT_KEY,articleId);
            return new ResultBean<>(EnumResult.UPLOAD_SUCCESS);
        } else {
            return new ResultBean<>(EnumResult.UPLOAD_ERROR);
        }
    }

    @GetMapping(value = "/getImage/{imgUrl}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable String imgUrl) {
        File file = new File(picUrl + imgUrl);
        byte[] bytes = null;
        try (InputStream inputStream = new FileInputStream(file);) {
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, inputStream.available());
        } catch (IOException e) {
            log.error("找不到文件{}", imgUrl);
        }
        return bytes;
    }

    @DeleteMapping("/deleteImage")
    public ResultBean<Object> deleteImage(@RequestParam("articleId") Long articleId,
                                          @RequestParam(value = "picNum") Integer picNum,
                                          @CookieValue(value = "JSESSIONID") String jSessionId) {
        String key = "user:" + jSessionId;
        User user = (User) redisTemplate.opsForValue().get(key);
        // 使用悲观锁(SELECT FOR UPDATE)获得数据，当前事务持有X锁阻塞其他事务读，避免picNum出现提交覆盖的情况
        Article article = articleService.selectPicNumForUpdate(articleId);
        if (user != null && !article.getArticleUserId().equals(user.getUserId())) {
            return new ResultBean<>(EnumResult.ARTICLE_ERROR);
        }
        if(picNum < 1 || picNum > article.getArticlePicNum()) {
            return new ResultBean<>(EnumResult.DELETE_ERROR);
        }
        if(articleService.deletePicFile(article, picNum)) {
            //发送修改消息
            rabbitTemplate.convertAndSend(MqConstants.ARTCILE_EXCHANGE,MqConstants.ARTCILE_INSERT_KEY,articleId);
            return new ResultBean<>(EnumResult.DELETE_SUCCESS);
        }
        return new ResultBean<>(EnumResult.DELETE_ERROR);
    }

    /**
     * 查询商品
     * @param jSessionId
     * @param info  模糊查的关键词
     * @param articleRegion  商品地区
     * @param lowPrice   价格区间 low
     * @param highPrice  价格区间 high
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/listArticle")
    public ResultBean<Object> listArticle(@CookieValue(value = "JSESSIONID") String jSessionId,
                                            @RequestParam(value = "info",required=false) String info,
                                            @RequestParam(value = "articleRegion",required=false) String articleRegion,
                                            @RequestParam(value = "articleDistribution",required = false) String articleDistribution,
                                            @RequestParam(value = "articleAttrition",required = false) String articleAttrition,
                                            @RequestParam(value = "lowPrice",required=false) Integer lowPrice,
                                            @RequestParam(value = "highPrice",required=false) Integer highPrice,
                                            @RequestParam(defaultValue = "1", value = "pageNum") Integer pageNum,
                                            @RequestParam(defaultValue = "5", value = "pageSize") Integer pageSize){
        log.info("---------------------listArticle被调用-----------------------");
        //查询对应商品
        PageInfo<ArticleDto> articleList = articleService.queryArticle(info,articleRegion,articleDistribution,articleAttrition,lowPrice,highPrice,pageNum,pageSize);
        log.info("---------------------listArticle调用结束-----------------------");
        return new ResultBean<>(EnumResult.QUERY_SUCCESS,articleList);
    }

    /**
     * 用户上传商品信息
     * @param article
     * @return
     */
    @PostMapping("/upload")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResultBean<Object> uploadMyArticle(@CookieValue(value = "JSESSIONID") String jSessionId,
                                              @RequestBody Article article){
        String key = "user:" + jSessionId;
        User user = (User) redisTemplate.opsForValue().get(key);
        article.setArticleUserId(user.getUserId());
        article.setArticleViewCount(0);
        boolean flag = articleService.save(article);
        if(!flag){

           return new ResultBean<>(EnumResult.UPDATE_ERROR);
        }
        //发送新增消息
       rabbitTemplate.convertAndSend(MqConstants.ARTCILE_EXCHANGE,MqConstants.ARTCILE_INSERT_KEY,article.getArticleId());
       return new ResultBean<>(EnumResult.UPLOAD_SUCCESS,article.getArticleId());
    }

    /**
     * 更新商品信息
     * @param article
     * @return
     */
    @PostMapping("/update")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResultBean<Object> updateMyArticle(@RequestBody Article article){
        boolean flag = articleService.updateById(article);
        if(!flag){

            return new ResultBean<>(EnumResult.UPDATE_ERROR);
        }
        //发送修改消息
        rabbitTemplate.convertAndSend(MqConstants.ARTCILE_EXCHANGE,MqConstants.ARTCILE_INSERT_KEY,article.getArticleId());
        return new ResultBean<>(EnumResult.UPDATE_SUCCESS);
    }

    /**
     * 查看商品细节
     * @param articleId
     * @return
     */
    @ArticleView
    @GetMapping("/detail")
    public ResultBean<Object> getArticleDetail(@CookieValue(value = "JSESSIONID") String jSessionId,
                                               @RequestParam Long articleId){

        //查找用户id
        String key = "user:" + jSessionId;
        User user =(User)redisTemplate.opsForValue().get(key);
        Long userId = user.getUserId();

        //查询商品是否存在
        Article article = articleService.getById(articleId);
        if (article==null) {
            return new ResultBean<>(EnumResult.QUERY_ERROR);
        }

        // 从redis中获取问题的浏览量
        key = "articleId:" + articleId;
        Long redisViewCount = redisTemplate.opsForHyperLogLog().size(key);
        // 数据库中的浏览量
        int viewCount = redisViewCount.intValue() + article.getArticleViewCount();
        article.setArticleViewCount(viewCount);
        //查询想要购买该商品的人的数量
        Integer purchaseNum = purchaseService.getPurchaseNums(articleId);
        //使用articleDetailDto返回结果
        ArticleDetailDto articleDetailDto = new ArticleDetailDto();
        //该用户是否已经点击了想要该商品
        Boolean articlePurchase = purchaseService.getPurchase(article.getArticleId(),userId);
        articleDetailDto.setArticle(article);
        articleDetailDto.setArticlePurchase(articlePurchase);
        articleDetailDto.setPurchaseNum(purchaseNum);
        return new ResultBean<>(EnumResult.QUERY_SUCCESS,articleDetailDto);
    }

    /**
     * 删除个人商品
     *
     * @param articleId
     * @return
     */
    @PostMapping("/delete")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResultBean<Object> deleteArticle(@CookieValue(value = "JSESSIONID") String jSessionId,
                                            @RequestParam Long articleId){
        //查询商品是否存在
        Article article = articleService.getById(articleId);
        if (article==null) {
            return new ResultBean<>(EnumResult.QUERY_ERROR);
        }

        String key = "user:" + jSessionId;
        User user = (User) redisTemplate.opsForValue().get(key);

        if(user != null && !article.getArticleUserId().equals(user.getUserId())) {
            return new ResultBean<>(EnumResult.ARTICLE_ERROR);
        }

        boolean flag = articleService.removeById(articleId);
        key = "articleId:" + articleId;
        redisTemplate.delete(key);

        if(!flag){
            return new ResultBean<>(EnumResult.DELETE_ERROR);
        }
        //发送删除消息
        rabbitTemplate.convertAndSend(MqConstants.ARTCILE_EXCHANGE,MqConstants.ARTCILE_DELETE_KEY,articleId);
        return new ResultBean<>(EnumResult.DELETE_SUCCESS);
    }

    /**
     *
     * @param jSessionId
     * @param pageNum
     * @param pageSize
     * @param articleStatus 商品状态
     * @param userId 为空是查询个人，不为空是查询别人
     * @return
     */
    @GetMapping("/listSbArticle")
    public ResultBean<Object> listSbSoldArticle(@CookieValue(value = "JSESSIONID") String jSessionId,
                                                @RequestParam(defaultValue = "1", value = "pageNum") Integer pageNum,
                                                @RequestParam(defaultValue = "5", value = "pageSize") Integer pageSize,
                                                @RequestParam Integer articleStatus,
                                                @RequestParam(required = false) Long userId){
        if(userId==null){
            String key = "user:" + jSessionId;
            User user = (User) redisTemplate.opsForValue().get(key);
            userId = user.getUserId();
        }
        //查询对应商品
        PageInfo<ArticleDto> articleList = articleService.querySoldArticle(userId,articleStatus,pageNum,pageSize);
        return new ResultBean<>(EnumResult.QUERY_SUCCESS,articleList);
    }


    /**
     * 查找我想要哪些商品
     * @param jSessionId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/purchase")
    public ResultBean<Object> listWantBuyArticle(@CookieValue(value = "JSESSIONID") String jSessionId,
                                                @RequestParam(defaultValue = "1", value = "pageNum") Integer pageNum,
                                                @RequestParam(defaultValue = "5", value = "pageSize") Integer pageSize){
        String key = "user:" + jSessionId;
        User user = (User) redisTemplate.opsForValue().get(key);
        Long userId = user.getUserId();

        //查询对应商品
        PageInfo<ArticleDto> articleList = articleService.queryMyWantArticle(userId,pageNum,pageSize);
        return new ResultBean<>(EnumResult.QUERY_SUCCESS,articleList);
    }




}
