package com.hafa.market.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hafa.market.mapper.PurchaseMapper;
import com.hafa.market.pojo.Purchase;
import com.hafa.market.service.PurchaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PurchaseServiceImpl extends ServiceImpl<PurchaseMapper, Purchase> implements PurchaseService {

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Override
    public Boolean getPurchase(Long articleId, Long userId) {
        QueryWrapper<Purchase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("purchase_article_id",articleId)
                .eq("purchase_user_id",userId);

        Purchase purchase = purchaseMapper.selectOne(queryWrapper);
        return purchase != null;
    }

    @Override
    public void removePurchase(Long articleId, Long userId) {
        QueryWrapper<Purchase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("purchase_article_id",articleId)
                .eq("purchase_user_id",userId);
        purchaseMapper.delete(queryWrapper);
    }

    @Override
    public List<Long> listWantBuyUser(Long articleId) {
        List<Long> list = new ArrayList<>();
        QueryWrapper<Purchase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("purchase_article_id",articleId);
        List<Purchase> purchaseList = purchaseMapper.selectList(queryWrapper);
        for(Purchase purchase:purchaseList){
            list.add(purchase.getPurchaseUserId());
        }
        return list;
    }

    @Override
    public Integer getPurchaseNums(Long articleId) {
        QueryWrapper<Purchase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("purchase_article_id",articleId);
        List<Purchase> purchaseList = purchaseMapper.selectList(queryWrapper);
        return purchaseList.size();
    }

}
