package com.example.inventorybackend.Service;

// LogService.java

import com.example.inventorybackend.entity.OperationLog;
import com.example.inventorybackend.utils.LocalDateTimeAdapter;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LogService implements InitializingBean {

    private final List<OperationLog> logs = new ArrayList<>();
    private static final String LOG_FILE_PATH = "data/logs.json";

    public LogService() {
        System.out.println("🔧【DEBUG】LogService 已被 Spring 创建！ID: " + this.hashCode());
    }

    @Override
    public void afterPropertiesSet() throws Exception {  // ✅ 统一使用 afterPropertiesSet
        init();
    }

    @PostConstruct
    public void init() {
        System.out.println("🔧 [初始化] LogService 正在加载数据...");
        loadLogsFromFile(); // ✅ 启动时加载文件
    }

    /**
     * 从 data/logs.json 加载所有日志到内存
     */
    private void loadLogsFromFile() {
        File file = new File(LOG_FILE_PATH);
        if (!file.exists()) {
            System.out.println("🔍 无历史日志文件，初始化空列表");
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<OperationLog>>(){}.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            List<OperationLog> loaded = gson.fromJson(reader, listType);

            if (loaded != null) {
                logs.addAll(loaded);
                System.out.println("✅ 成功加载 " + loaded.size() + " 条历史日志");
            } else {
                System.out.println("🟡 日志文件为空");
            }
        } catch (Exception e) {
            System.err.println("⚠️ 加载日志文件失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 添加新日志并持久化
     */
    public void addLog(String productId, String productName, String action, int quantity) {
        OperationLog log = new OperationLog(
                UUID.randomUUID().toString(),
                productId,
                productName,
                action,
                quantity,
                LocalDateTime.now()
        );
        logs.add(log);
        saveLogsToFile(); // 每次都保存
    }
    /**
     * 获取最近 N 条日志（供前端使用）
     */
    public List<Map<String, Object>> getRecentLogs(int limit) {
        int size = logs.size();
        int fromIndex = Math.max(0, size - limit);

        return logs.subList(fromIndex, size).stream().map(log -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", log.getId());
            map.put("productId", log.getProductId());
            map.put("productName", log.getProductName());
            map.put("action", log.getAction());
            map.put("quantity", log.getQuantity());
            map.put("timestamp", log.getTimestamp().toString());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 保存当前内存日志到文件
     */
    private void saveLogsToFile() {
        try (FileWriter writer = new FileWriter(LOG_FILE_PATH)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .setPrettyPrinting()
                    .create();
            gson.toJson(logs, writer);
        } catch (IOException e) {
            System.err.println("❌ 保存日志失败：" + e.getMessage());
        }
    }

    /**
     * 加载历史日志
     */
    private void loadLogs() {
        File file = new File(LOG_FILE_PATH);
        if (!file.exists()) {
            System.out.println("🔍 无历史日志文件，初始化空列表");
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<OperationLog>>(){}.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            List<OperationLog> loaded = gson.fromJson(reader, listType);
            if (loaded != null && !loaded.isEmpty()) {
                logs.addAll(loaded);
                System.out.println("✅ 成功加载 " + loaded.size() + " 条操作日志");
            } else {
                System.out.println("🟡 日志文件存在但为空");
            }
        } catch (Exception e) {
            System.err.println("⚠️ 加载日志失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}

