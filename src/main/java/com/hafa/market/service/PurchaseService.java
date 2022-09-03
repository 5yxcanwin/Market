package com.hafa.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hafa.market.pojo.Purchase;

import java.util.List;

public interface PurchaseService extends IService<Purchase> {
    Boolean getPurchase(Long articleId, Long userId);

    void removePurchase(Long articleId, Long userId);

    List<Long> listWantBuyUser(Long articleId);

    Integer getPurchaseNums(Long articleId);

}
