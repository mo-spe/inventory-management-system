package com.example.inventorybackend.Service;
// ProductService.java

import com.example.inventorybackend.entity.OperationLog;
import com.example.inventorybackend.entity.Product;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService implements InitializingBean {

    private final List<Product> products = new ArrayList<>();
    private static final String DATA_FILE = "data/inventory.json";

    private final List<OperationLog> logs = new ArrayList<>();
    private static final String LOG_FILE_PATH = "data/logs.json";

    public ProductService() {
        System.out.println("🔧【DEBUG】ProductService 已被 Spring 创建！ID: " + this.hashCode());
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
    /**
     * 系统启动时加载数据
     */
    public void init() {
        System.out.println("🔧 [初始化] ProductService 正在加载数据...");
        loadData();

        if (products.isEmpty()) {
            System.out.println("⚠️ 数据为空，正在添加默认测试商品...");
            products.add(new Product("SP0001", "矿泉水", "饮料", 1.5, 2.0, 100));
            products.add(new Product("SP0002", "薯片", "零食", 3.0, 5.0, 50));
            saveData();
            System.out.println("✅ 已添加2条测试数据并保存");
        }else {
            System.out.println("🎉 成功从文件加载了 " + products.size() + " 条数据");
        }
    }

    /**
     * 获取所有商品
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    /**
     * 根据 ID 查找商品
     */
    public Product findById(String id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * 添加商品（校验重复）
     */
    public boolean addProduct(Product product) {
        if (findById(product.getId()) != null) {
            return false; // 已存在
        }
        products.add(product);
        saveData(); // 添加后立即保存
        return true;
    }

    /**
     * 删除商品
     */
    public boolean deleteById(String id) {
        boolean removed = products.removeIf(p -> p.getId().equals(id));
        if (removed) {
            saveData();
        }
        return removed;
    }

    /**
     * 更新商品信息（用于修改库存等）
     */
    public boolean updateProduct(String id, Product updated) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(id)) {
                products.set(i, updated);
                saveData();
                return true;
            }
        }
        return false;
    }

    /**
     * 统计各分类数量
     */
    public java.util.Map<String, Integer> getCategoryStats() {
        return products.stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        Collectors.summingInt(p -> 1)
                ));
    }

    /**
     * 获取低库存商品（<10）
     */
    public List<Product> getLowStockProducts() {
        return products.stream()
                .filter(p -> p.getStock() < 10)
                .collect(Collectors.toList());
    }

    /**
     * 保存数据到 JSON 文件
     */
    private void saveData() {
        System.out.println("🔧 正在准备保存数据...");

        File dir = new File("data");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("✅ 成功创建 data 目录！");
            } else {
                System.err.println("❌ 创建 data 目录失败！请检查权限或路径。");
                return;
            }
        } else {
            System.out.println("📁 data 目录已存在。");
        }

        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(products, writer);
        } catch (IOException e) {
            System.err.println("保存数据失败：" + e.getMessage());
        }
    }




    /**
     * 从 JSON 文件加载数据
     */
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("无历史数据，初始化空列表");
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Product>>(){}.getType();
            List<Product> loaded = new Gson().fromJson(reader, listType);
            if (loaded != null) {
                products.addAll(loaded);
                System.out.println("✅ 成功加载 " + loaded.size() + " 条商品数据");
            }
        } catch (Exception e) {
            System.err.println("读取数据失败：" + e.getMessage());
        }
    }

    /**
     * 系统启动时加载已有日志
     */


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
        logs.add(log);
    }

    /**
     * 获取最近 N 条日志（用于前端显示）
     */
    public List<OperationLog> getRecentLogs(int limit) {
        int size = logs.size();
        int fromIndex = Math.max(0, size - limit);
        return new ArrayList<>(logs.subList(fromIndex, size));
    }



    /**
     * 商品编号自动生成
     */
    public String generateNextId() {
        return products.stream()
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


    // 需要添加依赖：Gson

}

