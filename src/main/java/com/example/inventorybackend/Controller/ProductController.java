package com.example.inventorybackend.Controller;

// ProductController.java

import com.example.inventorybackend.Service.ProductService;
import com.example.inventorybackend.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ProductController.java
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

@RestController
@CrossOrigin  // 允许前端跨域访问
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ==================== 商品管理接口 ====================

    /**
     * GET /api/products
     * 分页查询所有商品
     */
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.getAllProducts(pageable);
        return ResponseEntity.ok(productPage);
    }

    /**
     * GET /api/products/{id}
     * 根据 ID 查找商品
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {
        Product product = productService.findById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.status(404).body("❌ 商品不存在");
        }
    }

    /**
     * POST /api/products
     * 添加新商品
     */
    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> addProduct(@RequestBody Product product) {
        Map<String, Object> result = new HashMap<>();

        boolean success = productService.addProduct(product);
        if (success) {
            result.put("success", true);
            result.put("message", "✅ 成功添加商品：" + product.getName());
        } else {
            result.put("success", false);
            result.put("message", "❌ 编号已存在：" + product.getId());
        }

        return ResponseEntity.status(success ? 200 : 400).body(result);
    }

    /**
     * PUT /api/products/{id}
     * 更新商品信息（用于入库/出库）
     */
    @PutMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable String id,
            @RequestBody Product updated) {

        Map<String, Object> result = new HashMap<>();
        Product old = productService.findById(id);

        if (old == null) {
            result.put("success", false);
            result.put("message", "❌ 商品不存在");
            return ResponseEntity.status(404).body(result);
        }

        int diff = updated.getStock() - old.getStock();

        boolean success = productService.updateProduct(id, updated);
        if (success) {
            // 记录出入库日志
            if (diff > 0) {
                productService.logStockChange(updated.getId(), updated.getName(), "入库", diff);
            } else if (diff < 0) {
                productService.logStockChange(updated.getId(), updated.getName(), "出库", Math.abs(diff));
            }

            result.put("success", true);
            result.put("message", "✅ 库存更新成功");
        } else {
            result.put("success", false);
            result.put("message", "❌ 更新失败，请重试");
        }

        return ResponseEntity.status(success ? 200 : 500).body(result);
    }

    /**
     * DELETE /api/products/{id}
     * 删除商品
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> deleteById(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();

        Product p = productService.findById(id);
        if (p == null) {
            result.put("success", false);
            result.put("message", "❌ 商品不存在");
            return ResponseEntity.status(404).body(result);
        }

        boolean success = productService.deleteById(id);
        if (success) {
            result.put("success", true);
            result.put("message", "✅ 成功删除商品：" + p.getName());
        } else {
            result.put("success", false);
            result.put("message", "❌ 删除失败");
        }

        return ResponseEntity.status(success ? 200 : 500).body(result);
    }

    // ==================== 辅助功能接口 ====================

    /**
     * GET /api/generate-id
     * 自动生成下一个商品编号（如 SP0003）
     */
    @GetMapping("/products/generate-id")
    public String generateNextId() {
        return productService.generateNextId();
    }

    /**
     * GET /api/stats/category
     * 统计各分类数量（用于图表）
     */
    @GetMapping("/stats/category")
    public Map<String, Integer> getCategoryStats() {
        return productService.getCategoryStats();
    }

    /**
     * GET /api/alerts/low-stock
     * 获取低库存商品列表（<10）
     */
    @GetMapping("/alerts/low-stock")
    public List<Product> getLowStockProducts() {
        return productService.getLowStockProducts();
    }
}



