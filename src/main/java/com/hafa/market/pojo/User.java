package com.hafa.market.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author heavytiger
 * @version 1.0
 * @description 用户实体类，包含用户的信息
 * @date 2022/4/17 17:19
 */
@Data
@TableName("user")
public class User {
    // 主键自增
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String userOpenid;

    private String userName;

    private String userAvatar;

    private String userPhone;

    private String userEmail;

    private String userAddress;
}
