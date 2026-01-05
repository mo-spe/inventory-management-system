package com.example.inventorybackend.Controller;

import com.example.inventorybackend.Service.RecommendationService;
import com.example.inventorybackend.Service.SKUStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
@Tag(name = "管理接口", description = "系统管理相关接口")
public class AdminController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private SKUStatsService skuStatsService;

    /**
     * 手动触发缓存刷新
     * POST /api/admin/refresh-recommend-cache
     */
    @Operation(summary = "手动刷新推荐缓存", description = "手动触发推荐缓存重建")
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
