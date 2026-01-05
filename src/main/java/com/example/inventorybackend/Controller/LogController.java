package com.example.inventorybackend.Controller;

// LogController.java
import com.example.inventorybackend.Service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/logs")
@Tag(name = "操作日志", description = "商品操作日志管理")
public class LogController {

    @Autowired
    private ProductService productService; // 因为 log 在 ProductService 中管理

    /**
     * 获取最近 N 条日志（供前端使用）
     */
    @Operation(summary = "获取最近日志", description = "获取最近的操作日志")
    @GetMapping("/recent")
    public List<Map<String, Object>> getRecentLogs(
            @Parameter(description = "日志数量") @RequestParam(defaultValue = "10") int limit) {
        return productService.getRecentLogs(limit);
    }
}

