package com.xqk.campusfind.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xqk.campusfind.common.Result;
import com.xqk.campusfind.entity.Claim;
import com.xqk.campusfind.entity.Item;
import com.xqk.campusfind.mapper.ClaimMapper;
import com.xqk.campusfind.mapper.ItemMapper;
import com.xqk.campusfind.service.ClaimService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ClaimServiceImpl implements ClaimService {

    @Resource
    private ClaimMapper claimMapper;

    @Resource
    private ItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> applyClaim(Claim claim) {
        Item item = itemMapper.selectById(claim.getItemId());
        if (item == null) {
            return Result.error("该物品可能已被删除");
        }

        // ✅ 核心修复：禁止发布者自己认领（后端防御）
        // 假设前端传入的 claim 对象中已经封装了当前登录用户的 userId
        if (item.getUserId() != null && item.getUserId().equals(claim.getUserId())) {
            return Result.error("操作失败：您不能对自己的发布进行此操作");
        }

        // 检查物品状态
        if (item.getStatus() == 2) {
            return Result.error("该物品已被他人申请认领中");
        }
        if (item.getStatus() == 3) {
            return Result.error("该物品已归还，无法进行认领");
        }

        claim.setStatus(0);
        claimMapper.insert(claim);

        // 更新物品状态
        item.setStatus(2);
        itemMapper.updateById(item);

        return Result.success("申请已提交");
    }

    @Override
    public List<Claim> getAllClaimsWithDetails() {
        // 🌟 核心改进：直接调用底层高效连接 SQL，消除 For 循环 N+1 漏洞
        return claimMapper.selectClaimWithDetails();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> reviewClaim(Long id, Integer status) {
        Claim claim = claimMapper.selectById(id);
        if (claim == null) {
            return Result.error("该认领申请记录不存在");
        }

        // 1. 更新申请单状态
        claim.setStatus(status);
        claimMapper.updateById(claim);

        // 2. 根据审批结果流转失物生命周期状态
        Item item = itemMapper.selectById(claim.getItemId());
        if (item != null) {
            if (status == 1) {
                item.setStatus(3); // 认领成功 -> 物品变为：3=已归还
            } else if (status == 2) {
                item.setStatus(1); // 申请驳回 -> 物品回滚释放回公海：1=可认领
            }
            itemMapper.updateById(item);
        }
        return Result.success("处理成功");
    }

    @Override
    public List<Claim> getMyClaims(Long userId) {
        // 🌟 核心改进：调用 XML 多表查询，自动补齐对应的物品 itemName 视图层渲染
        return claimMapper.selectMyClaimsWithDetails(userId);
    }
}