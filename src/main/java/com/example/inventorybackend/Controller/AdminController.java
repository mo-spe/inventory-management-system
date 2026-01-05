package com.example.inventorybackend.Controller;

import com.example.inventorybackend.Service.RecommendationService;
import com.example.inventorybackend.Service.SKUStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private SKUStatsService skuStatsService;

    /**
     * 手动触发缓存刷新
     * POST /api/admin/refresh-recommend-cache
     */
    @PostMapping("/refresh-recommend-cache")
    public String refreshRecommendCache() {
        try {
            skuStatsService.rebuildCacheAsync(); // 异步执行缓存重建
            return "{\"status\":\"success\",\"message\":\"✅ 推荐缓存刷新任务已启动\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\",\"message\":\"❌ 刷新失败：" + e.getMessage() + "\"}";
        }
    }
}
