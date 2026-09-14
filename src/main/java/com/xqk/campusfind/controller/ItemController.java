package com.xqk.campusfind.controller;

import com.xqk.campusfind.common.Result;
import com.xqk.campusfind.dto.MatchResult;
import com.xqk.campusfind.entity.Item;
import com.xqk.campusfind.service.ItemService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Resource
    private ItemService itemService;

    @PostMapping("/add")
    public Result<Item> add(@RequestBody Item item) {
        Item savedItem = itemService.addItem(item);
        return Result.success(savedItem);
    }

    @GetMapping("/list")
    public Result<List<Item>> list() {
        return Result.success(itemService.getPublicItemList());
    }

    @GetMapping("/pending")
    public Result<List<Item>> pending() {
        return Result.success(itemService.getPendingList());
    }

    @PostMapping("/review")
    public Result<String> review(@RequestParam Long id, @RequestParam Integer status) {
        boolean success = itemService.reviewItem(id, status);
        return success ? Result.success("审核处理成功") : Result.error("操作失败，该物品不存在");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(itemService.getDashboardStats());
    }

    // 🌟 1. 在类内部上方，注入 yml 里的磁盘路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    // ✅ 修改后的图片上传接口
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, jakarta.servlet.http.HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 生成唯一文件名防止覆盖
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String newFileName = UUID.randomUUID().toString() + ext;

        // 🌟 2. 使用从 yml 动态注入的磁盘路径创建目录和文件对象
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(dir, newFileName));

            // 🌟 3. 高级进阶：通过 request 动态获取当前后端的 协议://域名:端口
            // 这样无论你是 localhost、127.0.0.1 还是上线的公网IP，都能完美动态生成，绝不硬编码！
            String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String fileUrl = basePath + "/upload/" + newFileName;

            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("图片上传失败，服务器磁盘IO异常");
        }
    }

    @PostMapping("/match")
    public Result<List<MatchResult>> smartMatch(@RequestBody Item item) {
        List<MatchResult> matches = itemService.smartMatch(item);
        return Result.success(matches);
    }
    /**
     * 获取当前用户发布的物品列表
     */
    @GetMapping("/my")
    public Result<List<Item>> getMyItems(@RequestParam Long userId) {
        List<Item> items = itemService.getMyItems(userId);
        return Result.success(items);
    }

    /**
     * 删除物品（只有发布者本人可以删除）
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteItem(@PathVariable Long id, @RequestParam Long userId) {
        boolean success = itemService.deleteItem(id, userId);
        return success ? Result.success("删除成功") : Result.error("删除失败，您无权操作此物品");
    }

}