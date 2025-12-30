package com.example.inventorybackend.Controller;

// LogController.java
import com.example.inventorybackend.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private ProductService productService; // 因为 log 在 ProductService 中管理

    /**
     * 获取最近 N 条日志（供前端使用）
     */
    @GetMapping("/recent")
    public List<Map<String, Object>> getRecentLogs(
            @RequestParam(defaultValue = "10") int limit) {
        return productService.getRecentLogs(limit);
    }
}

