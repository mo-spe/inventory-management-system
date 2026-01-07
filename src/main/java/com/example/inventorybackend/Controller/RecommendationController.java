package com.example.inventorybackend.Controller;

import com.example.inventorybackend.Service.AIDecisionService;
import com.example.inventorybackend.Service.RecommendationService;
import com.example.inventorybackend.Service.ReplenishmentService;
import com.example.inventorybackend.Service.SKUStatsService;
import com.example.inventorybackend.entity.SKUStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/recommend")
@Tag(name = "智能推荐", description = "商品推荐和补货建议")
public class RecommendationController {

    @Autowired
    private SKUStatsService statsService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ReplenishmentService replenishmentService;

    @Autowired
    private AIDecisionService aiDecisionService;
    /**
     * GET /api/recommend/top?page=0&size=10
     * 返回分页推荐结果
     */
    @Operation(summary = "获取推荐列表", description = "分页获取商品推荐列表，包含补货建议")
    @GetMapping("/top")
    public Map<String, Object> getTopKWithPaging(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "推荐商品总数") @RequestParam(defaultValue = "10") int topK,  // 新增topK参数，默认为10
            @Parameter(description = "商品分类筛选") @RequestParam(required = false) String category,
            @Parameter(description = "最低得分") @RequestParam(defaultValue = "0.0") double minScore) {
        
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

        // 根据最低得分过滤商品
        List<Map<String, Object>> filteredItems = new ArrayList<>();
        for (Map<String, Object> item : allItems) {
            if ((Double) item.get("score") >= minScore) {
                filteredItems.add(item);
            }
        }

        int totalElements = filteredItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isFirst = page <= 0;
        boolean isLast = page >= totalPages - 1 || totalPages == 0;

        int start = Math.max(page * size, 0);
        int end = Math.min(start + size, totalElements);

        List<Map<String, Object>> paginatedList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            Map<String, Object> reco = filteredItems.get(i);
            
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

    /**
     * GET /api/recommend/explain
     * 获取AI对补货建议的解释
     */
    @Operation(summary = "AI解释补货建议", description = "根据商品信息生成自然语言解释")
    @GetMapping("/explain")
    public ResponseEntity<String> explainRestock(
            @Parameter(description = "商品名称") @RequestParam String product,
            @Parameter(description = "周销量") @RequestParam int sales,
            @Parameter(description = "当前库存") @RequestParam int stock,
            @Parameter(description = "安全库存") @RequestParam(defaultValue = "30") int safety) {

        String explanation = aiDecisionService.explainRestock(product, sales, stock, safety);
        return ResponseEntity.ok(explanation);
    }

}