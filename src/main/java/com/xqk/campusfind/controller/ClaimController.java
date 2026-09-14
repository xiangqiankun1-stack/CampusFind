package com.xqk.campusfind.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xqk.campusfind.common.Result;
import com.xqk.campusfind.entity.Claim;
import com.xqk.campusfind.entity.Item;
import com.xqk.campusfind.entity.User;
import com.xqk.campusfind.mapper.ClaimMapper;
import com.xqk.campusfind.mapper.ItemMapper;
import com.xqk.campusfind.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/claim")
public class ClaimController {

    @Resource
    private ClaimMapper claimMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 管理员获取所有认领申请（用于兼容，虽然不再使用）
     */
    @GetMapping("/list")
    public Result<List<Claim>> getClaimList() {
        List<Claim> list = claimMapper.selectClaimWithDetails();
        return Result.success(list);
    }
    /**
     * 发起认领申请
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Claim claim) {
        if (claim.getItemId() == null) {
            return Result.error("非法请求：认领目标物品不存在");
        }

        Item currentItem = itemMapper.selectById(claim.getItemId());
        if (currentItem == null) {
            return Result.error("该物品不存在");
        }

        if (currentItem.getStatus() == 2) {
            return Result.error("该物品正在被其他同学认领");
        }

        if (currentItem.getStatus() == 3) {
            return Result.error("该物品已归还");
        }

        claim.setStatus(0);
        claimMapper.insert(claim);

        currentItem.setStatus(2);
        itemMapper.updateById(currentItem);

        return Result.success("申请成功，等待发布者确认");
    }

    /**
     * 获取当前用户的个人认领记录
     */
    @GetMapping("/my")
    public Result<List<Claim>> getMyClaims(@RequestParam("userId") Long userId) {
        List<Claim> list = claimMapper.selectMyClaimsWithDetails(userId);
        return Result.success(list);
    }

    /**
     * 🆕 拾获者确认是否同意联系（交换物品中的联系方式）
     */
    @PostMapping("/confirm")
    public Result<Map<String, Object>> confirmClaim(
            @RequestParam Long claimId,
            @RequestParam Integer action,
            @RequestParam Long userId
    ) {
        System.out.println("========== 拾获者确认认领 ==========");
        System.out.println("认领记录ID: " + claimId);
        System.out.println("操作: " + (action == 1 ? "同意" : "拒绝"));
        System.out.println("用户ID: " + userId);

        Claim claim = claimMapper.selectById(claimId);
        if (claim == null) {
            return Result.error("认领记录不存在");
        }

        Item item = itemMapper.selectById(claim.getItemId());
        if (item == null) {
            return Result.error("物品不存在");
        }

        if (!item.getUserId().equals(userId)) {
            return Result.error("无权操作，只有发布者可以确认");
        }

        if (action == 1) {
            // ✅ 同意联系
            claim.setStatus(1);
            claimMapper.updateById(claim);

            // 获取发布者信息
            User publisher = userMapper.selectById(item.getUserId());

            // 获取申请者信息
            User applicant = userMapper.selectById(claim.getUserId());

            // ✅ 返回物品中的联系方式（发布者的）和申请者用户名
            Map<String, Object> result = new HashMap<>();
            result.put("publisherContact", item.getContactInfo() != null ? item.getContactInfo() : "未设置");
            result.put("publisherName", publisher != null ? publisher.getUsername() : "未知");
            result.put("applicantName", applicant != null ? applicant.getUsername() : "未知");
            result.put("applicantId", claim.getUserId());
            result.put("message", "已同意！双方可以联系了");

            System.out.println("✅ 已同意联系，交换联系方式");
            return Result.success(result);
        } else {
            // ❌ 拒绝
            claim.setStatus(2);
            claimMapper.updateById(claim);

            item.setStatus(1);
            itemMapper.updateById(item);

            System.out.println("❌ 已拒绝该认领申请");
            return Result.success(null);
        }
    }

    /**
     * 🆕 获取待我确认的认领申请（拾获者视角）
     */
    @GetMapping("/pending-confirm")
    public Result<List<Claim>> getPendingConfirmations(@RequestParam Long userId) {
        System.out.println("========== 查询待确认认领 ==========");
        System.out.println("用户ID: " + userId);

        // ✅ 修改：查询 status=0（待确认）和 status=1（已同意）的记录
        List<Claim> claims = claimMapper.selectList(
                new LambdaQueryWrapper<Claim>()
                        .in(Claim::getStatus, 0, 1)  // ✅ 改为 in (0, 1)
                        .orderByDesc(Claim::getId)
        );

        List<Claim> result = new ArrayList<>();
        for (Claim claim : claims) {
            Item item = itemMapper.selectById(claim.getItemId());
            if (item != null && item.getUserId().equals(userId)) {
                claim.setItemName(item.getName());
                result.add(claim);
            }
        }

        System.out.println("找到记录数: " + result.size());
        System.out.println("==========================================");

        return Result.success(result);
    }

    /**
     * 🆕 任意一方确认已归还（自动归档）
     */
    @PostMapping("/complete")
    public Result<String> completeClaim(
            @RequestParam Long claimId,
            @RequestParam Long userId
    ) {
        System.out.println("========== 确认归还 ==========");
        System.out.println("认领记录ID: " + claimId);
        System.out.println("用户ID: " + userId);

        Claim claim = claimMapper.selectById(claimId);
        if (claim == null) {
            return Result.error("认领记录不存在");
        }

        Item item = itemMapper.selectById(claim.getItemId());
        if (item == null) {
            return Result.error("物品不存在");
        }

        // 验证权限：发布者或申请者都可以确认归还
        if (!item.getUserId().equals(userId) && !claim.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }

        // 验证状态：必须是"已同意"的状态
        if (claim.getStatus() != 1) {
            return Result.error("当前状态不允许确认归还");
        }

        // ✅ 更新状态为已完成
        claim.setStatus(5);
        claimMapper.updateById(claim);

        // ✅ 物品状态改为已归还
        item.setStatus(3);
        itemMapper.updateById(item);

        System.out.println("✅ 已确认归还，自动归档");
        return Result.success("感谢！物品已标记为已归还");
    }

    /**
     * 🆕 申请者查看发布者的联系方式
     */
    @GetMapping("/contact/{claimId}")
    public Result<Map<String, Object>> getContactInfo(
            @PathVariable Long claimId,
            @RequestParam Long userId
    ) {
        Claim claim = claimMapper.selectById(claimId);
        if (claim == null) {
            return Result.error("认领记录不存在");
        }

        // 验证权限：只有申请者本人可以查看
        if (!claim.getUserId().equals(userId)) {
            return Result.error("无权查看");
        }

        // 验证状态：必须是"已同意"才能查看
        if (claim.getStatus() != 1) {
            return Result.error("对方尚未同意联系");
        }

        Item item = itemMapper.selectById(claim.getItemId());
        User publisher = userMapper.selectById(item.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("publisherContact", item.getContactInfo() != null ? item.getContactInfo() : "未设置");
        result.put("publisherName", publisher != null ? publisher.getUsername() : "未知");

        return Result.success(result);
    }

}
