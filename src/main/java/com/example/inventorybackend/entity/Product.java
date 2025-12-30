package com.example.inventorybackend.entity;

public class Product {
    private String id;
    private String name;
    private String category;
    private double buyPrice;
    private double sellPrice;
    private int stock;

    // 构造函数
    public Product() {}

    public Product(String id, String name, String category,
                   double buyPrice, double sellPrice, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stock = stock;
    }

    // Getter 和 Setter（IDEA 可自动生成）
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }

    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
