package com.hafa.market.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 5yxcanwin
 * @version 1.0
 * @description 出售求购商品实体类
 * @date 2022/4/22 22:04
 */
@Data
@TableName("sale")
public class Sale {

    @TableId(type = IdType.AUTO)
    private Long saleId;   //主键id

    private Long saleAcquisitionId;  //求购商品表的主键id

    private Long saleUserId; //想要出售这件商品的用户id
}
