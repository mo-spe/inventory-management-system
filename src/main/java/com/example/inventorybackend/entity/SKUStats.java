// src/main/java/com/example/inventorybackend/entity/SKUStats.java
package com.example.inventorybackend.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Schema(description = "商品统计信息")
public class SKUStats {
    @Schema(description = "商品ID", example = "SP0001")
    public String productId;
    
    @Schema(description = "商品名称", example = "矿泉水")
    public String name;
    
    @Schema(description = "商品分类", example = "饮料")
    public String category;
    
    @Schema(description = "进价", example = "1.50")
    public double buyPrice;
    
    @Schema(description = "售价", example = "2.00")
    public double sellPrice;
    
    @Schema(description = "当前库存", example = "100")
    public int stock;
    
    @Schema(description = "最后补货日期")
    public LocalDateTime lastRestockDate;
    
    @Schema(description = "保质期天数", example = "365")
    public int shelfLifeDays;

    // 销售指标
    @Schema(description = "30天销量", example = "50")
    public int sales30Days;
    
    @Schema(description = "平均日销量", example = "1.67")
    public double avgSalesPerDay;

    // 收益
    @Schema(description = "利润率", example = "0.33")
    public double profitMargin;

    // 时间权重
    @Schema(description = "最后销售日期")
    public LocalDateTime lastSaleDate;
    
    @Schema(description = "时间权重", example = "0.85")
    public double timeWeight; // e^(-λt)

    public SKUStats() {}
}
