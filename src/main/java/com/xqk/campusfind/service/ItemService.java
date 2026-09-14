package com.xqk.campusfind.service;

import com.xqk.campusfind.dto.MatchResult;
import com.xqk.campusfind.entity.Item;
import java.util.List;
import java.util.Map;

public interface ItemService {
    Item addItem(Item item);
    List<Item> getPublicItemList();
    List<Item> getPendingList();
    boolean reviewItem(Long id, Integer status);
    Map<String, Object> getDashboardStats();
    /**
     * 智能匹配相似物品
     * @param newItem 新发布的物品
     * @return 按相似度排序的匹配结果列表
     */
    List<MatchResult> smartMatch(Item newItem);
    /**
     * 获取用户发布的物品列表
     */
    List<Item> getMyItems(Long userId);

    /**
     * 删除物品（验证权限）
     */
    boolean deleteItem(Long id, Long userId);
}