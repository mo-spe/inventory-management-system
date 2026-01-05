package com.example.inventorybackend.Controller;

import com.example.inventorybackend.Service.RecommendationService;
import com.example.inventorybackend.Service.ReplenishmentService;
import com.example.inventorybackend.Service.SKUStatsService;
import com.example.inventorybackend.entity.SKUStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/recommend")
public class RecommendationController {

    @Autowired
    private SKUStatsService statsService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ReplenishmentService replenishmentService;

    /**
     * GET /api/recommend/top?page=0&size=10
     * 返回分页推荐结果
     */
    @GetMapping("/top")
    public Map<String, Object> getTopKWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "10") int topK,  // 新增topK参数，默认为10
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0.0") double minScore) {
        
        // 参数验证
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {  // 限制每页最大数量
            size = 10;
        }
        if (topK <= 0 || topK > 1000) {  // 限制topK最大值
            topK = 10;
        }

        List<Map<String, Object>> allItems = recommendationService.getTopK(topK, category);  // 使用topK参数而不是硬编码的100

        int totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isFirst = page <= 0;
        boolean isLast = page >= totalPages - 1 || totalPages == 0;

        int start = Math.max(page * size, 0);
        int end = Math.min(start + size, totalElements);

        List<Map<String, Object>> paginatedList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            Map<String, Object> reco = allItems.get(i);
            SKUStats stats = statsService.getStats((String) reco.get("id"));
            if (stats == null) {
                // 添加日志记录缺失的统计信息
                System.out.println("警告: 商品ID " + reco.get("id") + " 未找到统计信息");
                continue;
            }

            Map<String, Object> advice = replenishmentService.advise(stats);
            Map<String, Object> merged = new LinkedHashMap<>(reco);
            merged.putAll(advice);

            paginatedList.add(merged);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", paginatedList);
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);
        result.put("number", page);
        result.put("size", size);
        result.put("first", isFirst);
        result.put("last", isLast);
        return result;
    }
}