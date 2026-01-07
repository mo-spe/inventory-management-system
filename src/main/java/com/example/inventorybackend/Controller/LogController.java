package com.example.inventorybackend.Controller;

// LogController.java
import com.example.inventorybackend.Service.LogCacheService;
import com.example.inventorybackend.Service.ProductService;
import com.example.inventorybackend.entity.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    
    @Autowired
    private LogCacheService logCacheService;

    /**
     * 获取最近 N 条日志（供前端使用）
     */
    @Operation(summary = "获取最近日志", description = "获取最近的操作日志")
    @GetMapping("/recent")
    public List<OperationLog> getRecentLogs(
            @Parameter(description = "日志数量") @RequestParam(defaultValue = "10") int limit) {
        return logCacheService.getLogsByLimit(limit);
    }
    
    /**
     * 获取分页日志（支持自定义数量和分页）
     */
    @Operation(summary = "获取分页日志", description = "获取分页的操作日志")
    @GetMapping("/paged")
    public Page<OperationLog> getPagedLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return logCacheService.getPagedLogs(page, size);
    }
    
    /**
     * 获取日志总数
     */
    @Operation(summary = "获取日志总数", description = "获取操作日志总数")
    @GetMapping("/count")
    public long getLogsCount() {
        return logCacheService.getTotalLogsCount();
    }
}

