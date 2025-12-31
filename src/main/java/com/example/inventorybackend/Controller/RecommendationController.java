package com.example.inventorybackend.Controller;

import com.example.inventorybackend.Service.RecommendationService;
import com.example.inventorybackend.Service.ReplenishmentService;
import com.example.inventorybackend.Service.SKUStatsService;
import com.example.inventorybackend.entity.SKUStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

// RecommendationController.java
// RecommendationController.java
import com.example.inventorybackend.Service.RecommendationService;
import com.example.inventorybackend.Service.ReplenishmentService;
import com.example.inventorybackend.Service.SKUStatsService;
import com.example.inventorybackend.entity.SKUStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// RecommendationController.java
// RecommendationController.java
// RecommendationController.java
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
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0.0") double minScore) {

        List<Map<String, Object>> allItems = recommendationService.getTopK(100, category);

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
            if (stats == null) continue;

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



