package com.xqk.campusfind.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xqk.campusfind.entity.Item;
import com.xqk.campusfind.mapper.ItemMapper;
import com.xqk.campusfind.service.ItemService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.xqk.campusfind.dto.MatchResult;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Resource
    private ItemMapper itemMapper;

    @Override
    public Item addItem(Item item) {
        item.setStatus(0);
        itemMapper.insert(item);
        return item;
    }

    @Override
    public List<Item> getPublicItemList() {
        return itemMapper.selectPublicItemList();
    }

    @Override
    public List<Item> getPendingList() {
        return itemMapper.selectItemWithUserByStatus(0);
    }

    @Override
    public boolean reviewItem(Long id, Integer status) {
        Item item = new Item();
        item.setId(id);
        item.setStatus(status);
        return itemMapper.updateById(item) > 0;
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        System.out.println("========== 开始计算仪表盘统计数据 ==========");

        long total = itemMapper.selectCount(null);
        System.out.println("总记录数: " + total);

        long pending = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 0));
        System.out.println("待审核 (status=0): " + pending);

        long published = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 1));
        System.out.println("已发布 (status=1): " + published);

        long claiming = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 2));
        System.out.println("认领中 (status=2): " + claiming);

        long returned = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getStatus, 3));
        System.out.println("已归还 (status=3): " + returned);

        long available = published + claiming;
        System.out.println("寻物/招领中 (available): " + available);

        long successRate = total > 0 ? Math.round(((double) returned / total) * 100) : 0;
        System.out.println("结案率: " + successRate + "%");

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("available", available);
        stats.put("claiming", claiming);
        stats.put("returned", returned);
        stats.put("successRate", successRate);

        System.out.println("返回给前端的统计数据: " + stats);
        System.out.println("==========================================");

        return stats;
    }



    @Override
    public List<MatchResult> smartMatch(Item newItem) {
        System.out.println("========== 开始智能匹配 ==========");
        System.out.println("新物品信息: " + newItem);

        // 🔒 空指针保护
        if (newItem.getPublishType() == null) {
            System.out.println("警告：发布类型为空，默认为招领启事");
            newItem.setPublishType(1);
        }

        System.out.println("发布类型: " + (newItem.getPublishType() == 1 ? "招领启事" : "寻物启事"));

        // 🆕 关键：根据发布类型，匹配相反类型的物品
        // 如果我发布的是"招领启事"(1)，就匹配"寻物启事"(2)
        // 如果我发布的是"寻物启事"(2)，就匹配"招领启事"(1)
        Integer targetType = newItem.getPublishType() == 1 ? 2 : 1;

        // 获取所有状态为 1（可认领）且类型相反的Items
        List<Item> candidates = itemMapper.selectList(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getPublishType, targetType) // 🆕 只匹配相反类型
                        .in(Item::getStatus, 1, 2) // 可认领或认领中的物品
                        .orderByDesc(Item::getId)
        );

        System.out.println("候选物品数量: " + candidates.size());

        // 计算每个候选物品的相似度
        List<MatchResult> results = new ArrayList<>();
        for (Item candidate : candidates) {
            double similarity = calculateSimilarity(newItem, candidate);

            // 只返回相似度 >= 30% 的物品
            if (similarity >= 30.0) {
                MatchResult result = new MatchResult();
                result.setItem(candidate);
                result.setSimilarity(similarity);
                result.setMatchReason(generateMatchReason(newItem, candidate, similarity));
                results.add(result);
            }
        }

        // 按相似度降序排序
        results.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

        // 只返回前 10 个最匹配的结果
        List<MatchResult> topResults = results.stream()
                .limit(10)
                .collect(Collectors.toList());

        System.out.println("匹配结果数量: " + topResults.size());
        System.out.println("==========================================");

        return topResults;
    }



    /**
     * 计算两个物品的相似度（0-100分）
     */
    private double calculateSimilarity(Item item1, Item item2) {
        double score = 0.0;

        // 1. 类型匹配（权重：30分）
        if (item1.getType() != null && item1.getType().equals(item2.getType())) {
            score += 30.0;
        }

        // 2. 名称相似度（权重：40分）
        if (item1.getName() != null && item2.getName() != null) {
            double nameSimilarity = calculateTextSimilarity(item1.getName(), item2.getName());
            score += nameSimilarity * 0.4;
        }

        // 3. 地点相关性（权重：15分）
        if (item1.getLocation() != null && item2.getLocation() != null) {
            if (item1.getLocation().contains(item2.getLocation()) ||
                    item2.getLocation().contains(item1.getLocation())) {
                score += 15.0;
            } else if (hasCommonKeyword(item1.getLocation(), item2.getLocation())) {
                score += 8.0;
            }
        }

        // 4. 时间接近度（权重：15分）
        if (item1.getTime() != null && item2.getTime() != null) {
            double timeSimilarity = calculateTimeSimilarity(item1.getTime(), item2.getTime());
            score += timeSimilarity * 0.15;
        }

        return Math.min(score, 100.0);
    }

    /**
     * 计算文本相似度（简单的关键词重叠算法）
     */
    private double calculateTextSimilarity(String text1, String text2) {
        // 转换为小写
        String t1 = text1.toLowerCase();
        String t2 = text2.toLowerCase();

        // 完全匹配
        if (t1.equals(t2)) {
            return 100.0;
        }

        // 包含关系
        if (t1.contains(t2) || t2.contains(t1)) {
            return 70.0;
        }

        // 分词后计算重叠度（简单按空格和常见分隔符分割）
        Set<String> words1 = new HashSet<>(Arrays.asList(t1.split("[\\s,，、]+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(t2.split("[\\s,，、]+")));

        // 计算交集
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        // 计算并集
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        if (union.isEmpty()) {
            return 0.0;
        }

        // Jaccard 相似度
        double jaccard = (double) intersection.size() / union.size();
        return jaccard * 100.0;
    }

    /**
     * 检查两个文本是否有共同关键词
     */
    private boolean hasCommonKeyword(String text1, String text2) {
        String[] keywords = {"图书馆", "食堂", "教学楼", "宿舍", "操场", "实验室", "办公室"};

        for (String keyword : keywords) {
            if (text1.contains(keyword) && text2.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算时间相似度
     */
    private double calculateTimeSimilarity(String time1, String time2) {
        try {
            // 简单比较：如果日期相同，得满分
            if (time1.equals(time2)) {
                return 100.0;
            }

            // 提取日期部分（假设格式为 YYYY-MM-DD HH:mm:ss）
            String date1 = time1.substring(0, 10);
            String date2 = time2.substring(0, 10);

            if (date1.equals(date2)) {
                return 80.0; // 同一天
            }

            // 可以尝试更复杂的时间差计算
            return 20.0; // 默认给一点分数
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * 生成匹配原因说明
     */
    private String generateMatchReason(Item item1, Item item2, double similarity) {
        List<String> reasons = new ArrayList<>();

        // 类型匹配
        if (item1.getType() != null && item1.getType().equals(item2.getType())) {
            reasons.add("物品类型相同（" + item1.getType() + "）");
        }

        // 名称相似
        if (item1.getName() != null && item2.getName() != null) {
            if (item1.getName().equals(item2.getName())) {
                reasons.add("物品名称完全一致");
            } else if (item1.getName().contains(item2.getName()) || item2.getName().contains(item1.getName())) {
                reasons.add("物品名称高度相似");
            }
        }

        // 地点相关
        if (item1.getLocation() != null && item2.getLocation() != null) {
            if (item1.getLocation().contains(item2.getLocation()) ||
                    item2.getLocation().contains(item1.getLocation())) {
                reasons.add("地点相近");
            }
        }

        // 时间接近
        if (item1.getTime() != null && item2.getTime() != null &&
                item1.getTime().length() >= 10 && item2.getTime().length() >= 10) {
            String date1 = item1.getTime().substring(0, 10);
            String date2 = item2.getTime().substring(0, 10);
            if (date1.equals(date2)) {
                reasons.add("时间相同");
            }
        }

        return String.join("，", reasons);
    }
    @Override
    public List<Item> getMyItems(Long userId) {
        System.out.println("========== 查询用户发布的物品 ==========");
        System.out.println("用户ID: " + userId);

        List<Item> items = itemMapper.selectList(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getUserId, userId)
                        .orderByDesc(Item::getId)
        );

        System.out.println("找到物品数量: " + items.size());
        System.out.println("==========================================");

        return items;
    }

    @Override
    public boolean deleteItem(Long id, Long userId) {
        System.out.println("========== 删除物品 ==========");
        System.out.println("物品ID: " + id);
        System.out.println("用户ID: " + userId);

        // 1. 查询物品
        Item item = itemMapper.selectById(id);
        if (item == null) {
            System.out.println("物品不存在");
            return false;
        }

        // 2. 验证权限：只有发布者本人可以删除
        if (!item.getUserId().equals(userId)) {
            System.out.println("权限不足：不是发布者");
            return false;
        }

        // 3. 检查状态：已归还的物品不能删除（保留历史记录）
        if (item.getStatus() == 3) {
            System.out.println("已归还的物品不能删除");
            return false;
        }

        // 4. 执行删除
        int result = itemMapper.deleteById(id);
        System.out.println("删除结果: " + (result > 0 ? "成功" : "失败"));
        System.out.println("==========================================");

        return result > 0;
    }

}





