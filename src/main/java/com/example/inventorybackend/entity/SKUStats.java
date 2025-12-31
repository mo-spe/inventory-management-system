// src/main/java/com/example/inventorybackend/entity/SKUStats.java
package com.example.inventorybackend.entity;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class SKUStats {
    public String productId;
    public String name;
    public String category;
    public double buyPrice;
    public double sellPrice;
    public int stock;
    public LocalDateTime lastRestockDate;
    public int shelfLifeDays;

    // 销售指标
    public int sales30Days;
    public double avgSalesPerDay;

    // 收益
    public double profitMargin;

    // 时间权重
    public LocalDateTime lastSaleDate;
    public double timeWeight; // e^(-λt)

    public SKUStats() {}
}
