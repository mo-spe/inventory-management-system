package com.example.inventorybackend.Service;

import com.example.inventorybackend.Repository.OperationLogRepository;
import com.example.inventorybackend.Repository.ProductRepository;
import com.example.inventorybackend.entity.Product;
import com.example.inventorybackend.projection.SalesSummaryProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// RecommendationService.java
@Service
public class RecommendationService {

    @Autowired
    private OperationLogRepository logRepo;

    @Autowired
    private ProductRepository productRepo;

    /**
     * 获取智能补货建议列表
     */
    public List<Map<String, Object>> getRestockSuggestions() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 1. 获取过去7天销量
        List<SalesSummaryProjection> salesList = logRepo.getRecentSales(sevenDaysAgo);

        // 2. 转换为 Map 易处理
        Map<String, Integer> weeklySalesMap = salesList.stream()
                .collect(Collectors.toMap(
                        SalesSummaryProjection::getProductId,
                        SalesSummaryProjection::getQuantity
                ));

        // 3. 获取当前库存信息
        List<Product> allProducts = productRepo.findAll();
        List<Map<String, Object>> suggestions = new ArrayList<>();

        for (Product p : allProducts) {
            String pid = p.getId();
            int weeklySold = weeklySalesMap.getOrDefault(pid, 0);
            double dailyAvg = weeklySold / 7.0;
            int currentStock = p.getStock();

            // 预测下周需求（简单线性预测）
            int predictedDemand = Math.max((int)(dailyAvg * 7), 5); // 至少建议5件

            // 库存健康度评估
            String status;
            if (currentStock == 0) {
                status = "🛑 缺货";
            } else if (currentStock < predictedDemand * 0.8) {
                status = "⚠️ 紧急";
            } else if (currentStock < predictedDemand) {
                status = "🟡 警告";
            } else {
                status = "✅ 健康";
            }

            // 计算补货建议
            int suggestedOrder = Math.max(predictedDemand - currentStock, 0);

            // 综合评分（用于排序）
            double score =
                    0.5 * normalize(dailyAvg, 0, 50) +           // 日均销量
                            0.4 * (status.startsWith("⚠️") ? 1.0 : 0.0) + // 是否紧急
                            0.1 * (suggestedOrder > 0 ? 1.0 : 0.0);      // 是否建议进货

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", pid);
            item.put("name", p.getName());
            item.put("category", p.getCategory());
            item.put("currentStock", currentStock);
            item.put("weeklySold", weeklySold);
            item.put("predictedDemand", predictedDemand);
            item.put("suggestedOrder", suggestedOrder);
            item.put("status", status);
            item.put("score", Math.round(score * 100));

            suggestions.add(item);
        }

        // 按得分降序排列
        // 按得分降序排列
        return suggestions.stream()
                .sorted((a, b) -> Double.compare(
                        ((Number) b.get("score")).doubleValue(),
                        ((Number) a.get("score")).doubleValue()
                ))
                .collect(Collectors.toList());

    }

    // 归一化函数 [min, max] → [0, 1]
    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
}

