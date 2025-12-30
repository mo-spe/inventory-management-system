package com.example.inventorybackend.entity;

// Product.java

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    private String id;

    private String name;
    private String category;

    @Column(name = "buy_price")
    private double buyPrice;

    @Column(name = "sell_price")
    private double sellPrice;

    private int stock;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "shelf_life_days")
    private int shelfLifeDays = 365;

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

