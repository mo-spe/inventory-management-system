// RecommendationService.java
package com.example.inventorybackend.Service;

import com.example.inventorybackend.entity.SKUStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Value("${recommendation.weights.sales:0.4}")
    private double salesWeight;

    @Value("${recommendation.weights.freshness:0.3}")
    private double freshnessWeight;

    @Value("${recommendation.weights.profit:0.2}")
    private double profitWeight;

    @Value("${recommendation.weights.time:0.1}")
    private double timeWeight;

    @Autowired
    private SKUStatsService skuStatsService;

    public List<Map<String, Object>> getTopK(int k, String category) {
        if (k <= 0) {
            return new ArrayList<>();
        }
        
        // 使用最小堆优化Top-K算法，时间复杂度O(n log k)
        PriorityQueue<Map<String, Object>> minHeap = new PriorityQueue<>(
                (a, b) -> ((Double) a.get("score")).compareTo((Double) b.get("score"))
        );

        for (SKUStats stats : skuStatsService.getAllStats()) {
            Map<String, Object> scoredItem = toScoredItem(stats);
            
            // 如果指定了类别且不匹配，则跳过
            if (category != null && !category.isEmpty() && 
                !scoredItem.get("category").equals(category)) {
                continue;
            }
            
            if (minHeap.size() < k) {
                minHeap.offer(scoredItem);
            } else if ((Double) scoredItem.get("score") > (Double) minHeap.peek().get("score")) {
                minHeap.poll();
                minHeap.offer(scoredItem);
            }
        }

        // 将堆中元素取出并按分数降序排列
        List<Map<String, Object>> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            result.add(minHeap.poll());
        }

        // 反转列表以获得降序排列（从最高分到最低分）
        Collections.reverse(result);
        return result;
    }

    private Map<String, Object> toScoredItem(SKUStats s) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", s.productId);
        item.put("name", s.name);
        item.put("category", s.category);
        item.put("currentStock", s.stock);

        double wSales = Math.log(1 + s.avgSalesPerDay) / Math.log(1 + 50);
        double wFresh = calculateFreshnessWeight(s);
        double wProfit = s.profitMargin / 0.5;
        double wTime = s.timeWeight;

        double score = salesWeight * wSales +
                freshnessWeight * wFresh +
                profitWeight * wProfit +
                timeWeight * wTime;

        item.put("score", Math.round(score * 100.0) / 100.0);
        return item;
    }

    private double calculateFreshnessWeight(SKUStats s) {
        if (s.lastRestockDate == null || s.shelfLifeDays <= 0) return 0.6;
        long days = ChronoUnit.DAYS.between(s.lastRestockDate, LocalDateTime.now());
        return Math.max(0, 1 - ((double)days / s.shelfLifeDays));
    }
}