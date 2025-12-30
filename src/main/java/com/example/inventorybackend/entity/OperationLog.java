package com.example.inventorybackend.entity;// OperationLog.java

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class OperationLog {
    private String id;
    private String productId;
    private String productName;
    private String action;     // "上架" / "入库" / "出库" / "下架"
    private int quantity;      // 数量（出库为负？这里用正数+类型区分）
    private LocalDateTime timestamp;

    // 构造函数
    public OperationLog() {}

    public OperationLog(String id, String productId, String productName,
                        String action, int quantity, LocalDateTime timestamp) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.action = action;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }


}
