package com.example.inventorybackend.Service;

import com.example.inventorybackend.entity.OperationLog;
import com.example.inventorybackend.Repository.OperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LogCacheService {

    @Autowired
    private OperationLogRepository logRepo;

    private final ConcurrentHashMap<String, List<OperationLog>> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> cacheTimestamps = new ConcurrentHashMap<>();
    private volatile List<OperationLog> allLogs = null;
    private volatile LocalDateTime allLogsTimestamp = null;

    @PostConstruct
    public void init() {
        updateAllLogsCache();
    }

    /**
     * 获取分页日志（带缓存）
     */
    public Page<OperationLog> getPagedLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return logRepo.findAll(pageable);
    }

    /**
     * 获取指定数量的日志（带缓存）
     */
    public List<OperationLog> getLogsByLimit(int limit) {
        String cacheKey = "limit_" + limit;
        LocalDateTime now = LocalDateTime.now();
        
        // 检查缓存是否存在且未过期（5分钟过期）
        if (cache.containsKey(cacheKey) && 
            allLogsTimestamp != null && 
            now.isBefore(allLogsTimestamp.plusMinutes(5))) {
            return cache.get(cacheKey);
        }
        
        // 从数据库获取并更新缓存
        List<OperationLog> logs = logRepo.findTopByOrderByTimestampDesc(limit);
        cache.put(cacheKey, logs);
        cacheTimestamps.put(cacheKey, now);
        return logs;
    }

    /**
     * 获取所有日志（带缓存）
     */
    public List<OperationLog> getAllLogs() {
        LocalDateTime now = LocalDateTime.now();
        
        // 检查缓存是否过期（5分钟过期）
        if (allLogs != null && allLogsTimestamp != null && 
            now.isBefore(allLogsTimestamp.plusMinutes(5))) {
            return allLogs;
        }
        
        updateAllLogsCache();
        return allLogs;
    }

    /**
     * 更新所有日志缓存
     */
    @Async("cacheUpdateExecutor")
    public void updateAllLogsCache() {
        List<OperationLog> logs = logRepo.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
        this.allLogs = logs;
        this.allLogsTimestamp = LocalDateTime.now();
    }

    /**
     * 定时更新缓存（每5分钟）
     */
    @Scheduled(fixedRate = 300000) // 5分钟更新一次
    public void scheduledCacheUpdate() {
        updateAllLogsCache();
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        cache.clear();
        cacheTimestamps.clear();
        allLogs = null;
        allLogsTimestamp = null;
    }

    /**
     * 获取日志总数
     */
    public long getTotalLogsCount() {
        return logRepo.count();
    }
}