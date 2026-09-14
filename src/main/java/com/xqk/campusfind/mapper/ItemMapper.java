package com.xqk.campusfind.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xqk.campusfind.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
    
    /**
     * 高级多表联查：根据状态获取物品列表，并自动注入发布者名称、当前认领人名称
     */
    List<Item> selectItemWithUserByStatus(@Param("status") Integer status);

    /**
     * 高级多表联查：获取大厅公开展示的物品列表（包含状态1和状态2），并包含用户名称
     */
    List<Item> selectPublicItemList();
}