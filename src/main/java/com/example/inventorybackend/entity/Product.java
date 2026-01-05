package com.example.inventorybackend.entity;

// Product.java

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "products")
@Schema(description = "商品信息")
public class Product {

    @Schema(description = "商品编号", example = "SP0001")
    @Id
    private String id;

    @Schema(description = "商品名称", example = "矿泉水")
    private String name;
    
    @Schema(description = "商品分类", example = "饮料")
    private String category;

    @Schema(description = "进价", example = "1.50")
    @Column(name = "buy_price")
    private double buyPrice;

    @Schema(description = "售价", example = "2.00")
    @Column(name = "sell_price")
    private double sellPrice;

    @Schema(description = "库存数量", example = "100")
    private int stock;

    @Schema(description = "创建时间")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Schema(description = "保质期天数", example = "365")
    @Column(name = "shelf_life_days")
    private int shelfLifeDays = 365;

    @Schema(description = "最后补货日期")
    @Column(name = "last_restock_date")
    private LocalDateTime lastRestockDate;


    // 构造函数 + Getter/Setter
    public Product() {}

    public Product(String id, String name, String category, double buyPrice, double sellPrice, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stock = stock;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // getter and setter...
}

