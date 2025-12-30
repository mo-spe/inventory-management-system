package com.example.inventorybackend.Controller;

// ProductController.java

import com.example.inventorybackend.Service.LogService;
import com.example.inventorybackend.Service.ProductService;
import com.example.inventorybackend.entity.OperationLog;
import com.example.inventorybackend.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private LogService logService; // 👈 注入日志服务

    // GET /api/products - 获取全部商品
    @GetMapping("/products")
    public List<Product> getAll() {
        System.out.println("📍 productService 实例地址：" + productService.hashCode());
        System.out.println("🟢 [API] GET /api/products 被调用");

        List<Product> result = productService.getAllProducts();
        System.out.println("📊 返回 " + result.size() + " 条数据");

        return result;
    }



    // 添加商品
    @PostMapping("/products")
    public boolean addProduct(@RequestBody Product product) {
        boolean success = productService.addProduct(product);
        if (success) {
            logService.addLog(product.getId(), product.getName(), "上架", product.getStock());
        }
        return success;
    }

    // 删除商品
    @DeleteMapping("/products/{id}")
    public boolean deleteById(@PathVariable String id) {
        Product p = productService.findById(id);
        if (p != null) {
            logService.addLog(p.getId(), p.getName(), "下架", p.getStock());
        }
        return productService.deleteById(id);
    }

    // PUT /api/products/{id} - 更新商品（如库存）
    /*
    @PutMapping("/products/{id}")
    public boolean updateProduct(@PathVariable String id, @RequestBody Product updated) {
        // 先查出旧商品信息
        Product oldProduct = productService.findById(id);
        if (oldProduct == null) return false;

        // 执行更新
        boolean success = productService.updateProduct(id, updated);
        if (!success) return false;


        return true;
    }

     */
    // 更新库存（入库/出库）
    @PutMapping("/products/{id}")
    public boolean updateProduct(@PathVariable String id, @RequestBody Product updated) {
        Product old = productService.findById(id);
        if (old == null) return false;

        int diff = updated.getStock() - old.getStock();
        boolean success = productService.updateProduct(id, updated);

        if (success) {
            if (diff > 0) {
                logService.addLog(updated.getId(), updated.getName(), "入库", diff);
            } else if (diff < 0) {
                logService.addLog(updated.getId(), updated.getName(), "出库", Math.abs(diff));
            }
        }
        return success;
    }



    // GET /api/stats/category - 分类统计
    @GetMapping("/stats/category")
    public Map<String, Integer> getCategoryStats() {
        return productService.getCategoryStats();
    }

    // GET /api/alerts/low-stock - 低库存预警
    @GetMapping("/alerts/low-stock")
    public List<Product> getLowStockAlerts() {
        return productService.getLowStockProducts();
    }

    //商品编号自动生成
    @GetMapping("/products/generate-id")
    public String generateId() {
        return productService.generateNextId();
    }

    @GetMapping("/logs/recent")
    public List<Map<String, Object>> getRecentLogs(
            @RequestParam(defaultValue = "10") int limit) {
        return logService.getRecentLogs(limit);
    }

}


