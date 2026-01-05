package com.example.inventorybackend.entity;// OperationLog.java

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// OperationLog.java

import jakarta.persistence.*;

        import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "operation_logs")
@Schema(description = "操作日志")
public class OperationLog {

    @Schema(description = "日志ID", example = "log001")
    @Id
    private String id;

    @Schema(description = "商品ID", example = "SP0001")
    @Column(name = "product_id")
    private String productId;

    @Schema(description = "商品名称", example = "矿泉水")
    @Column(name = "product_name")
    private String productName;

    @Schema(description = "操作类型", example = "入库")
    private String action; // "入库"/"出库"/"上架"
    
    @Schema(description = "操作数量", example = "10")
    private int quantity;
    
    @Schema(description = "操作时间")
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

