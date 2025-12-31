// RecommendationService.java
package com.example.inventorybackend.Service;

import com.example.inventorybackend.entity.SKUStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        return getAllRanked().stream()
                .filter(item -> category == null || category.isEmpty() ||
                        item.get("category").equals(category))
                .limit(k)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getAllRanked() {
        return skuStatsService.getAllStats().stream()
                .map(this::toScoredItem)
                .sorted((a, b) -> {
                    Double scoreA = (Double) a.get("score");
                    Double scoreB = (Double) b.get("score");
                    return scoreB.compareTo(scoreA); // 降序：高分在前
                })
                .collect(Collectors.toList());
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
