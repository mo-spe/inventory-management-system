package com.example.inventorybackend.entity;// OperationLog.java

import lombok.Data;

import java.time.LocalDateTime;

// OperationLog.java

import jakarta.persistence.*;

        import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "operation_logs")
public class OperationLog {

    @Id
    private String id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    private String action; // "入库"/"出库"/"上架"
    private int quantity;
    private LocalDateTime timestamp;


    // 构造函数 + Getter/Setter
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

    // getter/setter...
}

