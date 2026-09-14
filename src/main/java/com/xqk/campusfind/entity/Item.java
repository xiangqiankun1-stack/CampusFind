package com.xqk.campusfind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("item")
public class Item {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String image;
    private String name;
    private String type;
    private String description;
    private String location;
    private String time;
    private Integer status; // 0=待审核, 1=已发布, 2=认领中, 3=已归还

    // 🆕 新增：发布类型
    private Integer publishType; // 1=招领启事(捡到), 2=寻物启事(丢失)

    // 🆕 新增：联系方式（隐私保护，仅在确认后显示）
    private String contactInfo;

    private Long userId;

    // 🌟 重点优化：MyBatis-Plus 使用 @TableField(exist = false) 声明视图展示字段
    @TableField(exist = false)
    private String claimerName;

    @TableField(exist = false)
    private String publisherName;
}
