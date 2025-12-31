// SKUStatsService.java
package com.example.inventorybackend.Service;

import com.example.inventorybackend.Repository.OperationLogRepository;
import com.example.inventorybackend.Repository.ProductRepository;
import com.example.inventorybackend.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SKUStatsService {

    private static final Logger log = LoggerFactory.getLogger(SKUStatsService.class);

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private OperationLogRepository logRepo;

    private final Map<String, SKUStats> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        rebuildCache();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledRebuildCache() {
        log.info("⏰【定时任务】重建商品统计缓存...");
        rebuildCache();
    }

    public void rebuildCache() {
        log.debug("🔄 开始重建商品统计缓存...");
        cache.clear();

        List<Product> products = productRepo.findAll();
        if (products.isEmpty()) return;

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<OperationLog> logs = logRepo.findOutboundSince(thirtyDaysAgo);

        Map<String, Integer> sales30Map = logs.stream()
                .collect(Collectors.groupingBy(
                        OperationLog::getProductId,
                        Collectors.summingInt(OperationLog::getQuantity)
                ));

        for (Product p : products) {
            SKUStats stats = buildStats(p, sales30Map, logs);
            cache.put(p.getId(), stats);
        }

        log.info("✅ 缓存重建完成，共加载 {} 条商品", cache.size());
    }

    private SKUStats buildStats(Product p, Map<String, Integer> sales30Map,
                                List<OperationLog> allLogs) {
        SKUStats stats = new SKUStats();
        stats.productId = p.getId();
        stats.name = p.getName();
        stats.category = p.getCategory();
        stats.buyPrice = p.getBuyPrice();
        stats.sellPrice = p.getSellPrice();
        stats.stock = p.getStock();
        stats.lastRestockDate = p.getLastRestockDate();
        stats.shelfLifeDays = p.getShelfLifeDays();

        int sales30 = sales30Map.getOrDefault(p.getId(), 0);
        stats.sales30Days = sales30;
        stats.avgSalesPerDay = Math.max(sales30 / 30.0, 0);

        if (p.getBuyPrice() > 0) {
            stats.profitMargin = (p.getSellPrice() - p.getBuyPrice()) / p.getBuyPrice();
        } else {
            stats.profitMargin = 0;
        }

        Optional<LocalDateTime> lastSaleOpt = allLogs.stream()
                .filter(log -> log.getProductId().equals(p.getId()))
                .map(OperationLog::getTimestamp)
                .max(LocalDateTime::compareTo);
        stats.lastSaleDate = lastSaleOpt.orElse(null);

        if (stats.lastSaleDate != null) {
            long days = ChronoUnit.DAYS.between(stats.lastSaleDate, LocalDateTime.now());
            stats.timeWeight = Math.exp(-0.1 * days);
        } else {
            stats.timeWeight = 0.5;
        }

        return stats;
    }

    public Collection<SKUStats> getAllStats() {
        return cache.values();
    }

    public SKUStats getStats(String productId) {
        return cache.get(productId);
    }
}
