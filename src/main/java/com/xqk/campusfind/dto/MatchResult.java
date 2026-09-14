package com.xqk.campusfind.dto;

import com.xqk.campusfind.entity.Item;
import lombok.Data;

@Data
public class MatchResult {
    private Item item;           // 匹配的物品
    private Double similarity;   // 相似度分数 (0-100)
    private String matchReason;  // 匹配原因说明



}
