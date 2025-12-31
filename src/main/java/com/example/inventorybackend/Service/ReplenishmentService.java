// ReplenishmentService.java
package com.example.inventorybackend.Service;

import com.example.inventorybackend.entity.SKUStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReplenishmentService {

    @Value("${replenishment.period-days:14}")
    private int periodDays;

    @Value("${replenishment.safety-stock-days:7}")
    private int safetyStockDays;

    @Value("${replenishment.max-order-quantity:500}")
    private int maxOrderQuantity;

    @Value("${replenishment.max-stock:600}")
    private int maxStock;

    public Map<String, Object> advise(SKUStats s) {

        int weeklyDemand = (int)(s.avgSalesPerDay * 7);
        int twoWeekDemand = (int)(s.avgSalesPerDay * periodDays);
        int safetyStock = (int)(s.avgSalesPerDay * safetyStockDays);

        int suggestedOrder = 0;

        // 🚨 核心修复：库存已经超过上限，禁止补货
        if (s.stock >= maxStock) {
            suggestedOrder = 0;
        }
        else if (s.stock < twoWeekDemand + safetyStock) {
            suggestedOrder = twoWeekDemand + safetyStock - s.stock;

            // 上限保护
            suggestedOrder = Math.min(suggestedOrder, maxOrderQuantity);
            suggestedOrder = Math.min(suggestedOrder, maxStock - s.stock);
        }

        Map<String, Object> advice = new HashMap<>();
        advice.put("weeklySales", weeklyDemand);
        advice.put("suggestedOrder", Math.max(suggestedOrder, 0)); // 🔒 最终兜底
        return advice;
    }

}
