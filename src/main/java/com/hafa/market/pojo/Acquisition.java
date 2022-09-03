package com.hafa.market.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 5yxcanwin
 * @version 1.0
 * @description 求购商品实体类
 * @date 2022/4/22 22:04
 */
@Data
@TableName("acquisition")
public class Acquisition {

    @TableId(type = IdType.AUTO)
    private Long acquisitionId;  //求购

    private Long acquisitionUserId;  //求购人id

    private String acquisitionDescription; //求购商品描述

    private Integer acquisitionPrice; //预期价格
}
