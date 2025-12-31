package com.example.inventorybackend.Service;
// ProductService.java

import com.example.inventorybackend.Repository.OperationLogRepository;
import com.example.inventorybackend.Repository.ProductRepository;
import com.example.inventorybackend.entity.OperationLog;
import com.example.inventorybackend.entity.Product;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// ProductService.java

import com.example.inventorybackend.entity.OperationLog;
import com.example.inventorybackend.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional  // 确保增删改查在事务中执行
public class ProductService {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private OperationLogRepository logRepo;

    // ================= [新增] 自动迁移 JSON 数据 ==================
    @PostConstruct
    public void migrateDataFromJson() {
        // 检查数据库是否已有数据
        if (productRepo.count() > 0) {
            System.out.println("✅ 数据库已有数据，跳过 JSON 迁移");
            return;
        }

        File jsonFile = new File("data/inventory.json");
        if (!jsonFile.exists()) {
            System.out.println("🔍 无历史数据文件 data/inventory.json，跳过导入");
            return;
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            Type listType = new TypeToken<List<Product>>(){}.getType();
            Gson gson = new Gson();
            List<Product> products = gson.fromJson(reader, listType);

            if (products != null && !products.isEmpty()) {
                System.out.println("📦 正在从 inventory.json 导入 " + products.size() + " 条商品数据...");
                productRepo.saveAll(products);
                System.out.println("✅ 成功导入所有商品数据！");
            } else {
                System.out.println("🟡 文件为空，未导入任何数据");
            }
        } catch (Exception e) {
            System.err.println("❌ 导入失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取所有商品
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepo.findAll(pageable);
    }

    /**
     * 获取所有商品（用于 Excel 导出）
     */
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    /**
     * 根据 ID 查找商品
     */
    public Product findById(String id) {
        return productRepo.findById(id).orElse(null);
    }

    /**
     * 添加商品（校验重复）
     */
    public boolean addProduct(Product product) {
        if (productRepo.existsById(product.getId())) {
            return false; // 已存在
        }
        productRepo.save(product);
        logStockChange(product.getId(), product.getName(), "上架", product.getStock());
        return true;
    }

    /**
     * 删除商品
     */
    public boolean deleteById(String id) {
        Product p = findById(id);
        if (p == null) return false;

        // ✅ 只需记录一次“下架”操作即可
        logStockChange(p.getId(), p.getName(), "下架", p.getStock());

        // ✅ 直接删除商品，数据库会自动处理日志关联
        try {
            productRepo.deleteById(id);
            return true;
        } catch (Exception e) {
            System.err.println("删除失败：" + e.getMessage());
            return false;
        }
    }



    /**
     * 更新商品信息（用于入库/出库）
     */
    public boolean updateProduct(String id, Product updated) {
        Optional<Product> optionalOld = productRepo.findById(id);
        if (optionalOld.isEmpty()) {
            return false;
        }

        Product old = optionalOld.get();
        int oldStock = old.getStock();
        int newStock = updated.getStock();

        // 判断是否为“入库”操作（库存增加了）
        if (newStock > oldStock) {
            // 是入库 → 更新最后入库时间
            updated.setLastRestockDate(LocalDateTime.now());
        }
        // 出库或不变 → 不更新该字段

        // 保留其他字段一致性
        updated.setCreatedAt(old.getCreatedAt()); // 防止创建时间被修改
        updated.setBuyPrice(old.getBuyPrice());   // 进价不能通过此接口修改
        updated.setSellPrice(old.getSellPrice());

        try {
            productRepo.save(updated);
            return true;
        } catch (Exception e) {
            System.err.println("更新商品失败：" + e.getMessage());
            return false;
        }
    }




    /**
     * 统计各分类数量
     */
    public java.util.Map<String, Integer> getCategoryStats() {
        return productRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.summingInt(p -> 1)
                ));
    }

    /**
     * 获取低库存商品（<10）
     */
    public List<Product> getLowStockProducts() {
        return productRepo.findAll().stream()
                .filter(p -> p.getStock() < 10)
                .collect(Collectors.toList());
    }

    /**
     * 记录一次库存变更操作
     */
    public void logStockChange(String pid, String name, String action, int qty) {
        OperationLog log = new OperationLog(
                UUID.randomUUID().toString(),
                pid,
                name,
                action,
                qty,
                LocalDateTime.now()
        );
        logRepo.save(log);
    }

    /**
     * 获取最近 N 条日志（用于前端显示）
     */
    /**
     * 获取最近 N 条日志（用于前端显示）
     */
    /**
     * 获取最近 N 条日志（用于前端显示）
     */
    public List<Map<String, Object>> getRecentLogs(int limit) {
        // 从数据库获取最新的日志
        List<OperationLog> logs = logRepo.findTop10ByOrderByTimestampDesc();

        return logs.stream().map(log -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", log.getId());
            map.put("productId", log.getProductId());
            map.put("productName", log.getProductName());
            map.put("action", log.getAction());
            map.put("quantity", log.getQuantity());
            map.put("timestamp", log.getTimestamp().toString()); // 转为字符串避免 JSON 问题
            return map;
        }).collect(Collectors.toList());
    }



    /**
     * 商品编号自动生成
     */
    public String generateNextId() {
        return productRepo.findAll().stream()
                .map(Product::getId)
                .filter(id -> id.startsWith("SP"))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.substring(2));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .map(next -> "SP" + String.format("%04d", next + 1))
                .orElse("SP0001");
    }
}


