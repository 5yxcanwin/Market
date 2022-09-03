package com.hafa.market.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 5yxcanwin
 * @version 1.0
 * @description 想要购买商品实体类
 * @date 2022/4/22 22:04
 */
@Data
@TableName("purchase")
public class Purchase {

    @TableId(type = IdType.AUTO)
    private Long purchaseId; //主键

    private Long purchaseArticleId; //想购买的商品id

    private Long purchaseUserId; //想购买的用户id
}
