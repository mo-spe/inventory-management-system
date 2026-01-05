package com.example.inventorybackend.projection;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "销售汇总投影")
public interface SalesSummaryProjection {
    
    @Schema(description = "商品ID", example = "SP0001")
    String getProductId();
    
    @Schema(description = "商品名称", example = "矿泉水")
    String getProductName();
    
    @Schema(description = "销售数量", example = "50")
    Integer getQuantity();
}