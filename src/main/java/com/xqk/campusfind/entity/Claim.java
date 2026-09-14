package com.xqk.campusfind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("claim")
public class Claim {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;
    private Long userId;
    private String description;
    private Integer status; // 0 = 审核中, 1 = 已通过, 2 = 已驳回

    @TableField(exist = false)
    private String itemName;

    @TableField(exist = false)
    private String username; // 审核页面展示用
}