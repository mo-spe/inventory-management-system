package com.example.inventorybackend.Service;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AIDecisionService {

    private static final String QWEN_API = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String API_KEY = System.getenv("DASHSCOPE_API_KEY"); // 从环境变量获取API密钥
    private final OkHttpClient client = new OkHttpClient();

    public String explainRestock(String productName, int predictedSales, int currentStock, int safetyLevel) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.out.println("API密钥未设置");
            return "API密钥未设置，请联系管理员";
        }
        
        String prompt = String.format(
                "你是一个商超运营助手，请根据以下信息生成一条自然语言建议。\n" +
                        "商品：%s\n预计周销量：%d\n当前库存：%d\n安全库存：%d\n" +
                        "请用一句话说明是否需要补货及原因。不要输出额外内容。",
                productName, predictedSales, currentStock, safetyLevel
        );

        // 使用通义千问兼容OpenAI格式的请求体
        String jsonBody = String.format(
                "{\n" +
                "  \"model\": \"qwen-turbo\",\n" +
                "  \"input\": {\n" +
                "    \"messages\": [\n" +
                "      {\n" +
                "        \"role\": \"user\",\n" +
                "        \"content\": \"%s\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"parameters\": {\n" +
                "    \"result_format\": \"message\"\n" +
                "  }\n" +
                "}", 
                prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(QWEN_API)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            System.out.println("API Response: " + responseBody); // 添加调试输出
            System.out.println("API Status Code: " + response.code()); // 添加状态码输出
            
            if (response.isSuccessful()) {
                return parseResponse(responseBody); // 提取返回文本
            } else {
                System.out.println("API Request failed: " + response.code() + " - " + response.message());
                return "API请求失败: " + response.code() + " - " + responseBody;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "AI 暂时无法生成解释，请稍后再试。";
        }
    }

    private String parseResponse(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            System.out.println("Parsed JSON: " + json); // 添加调试输出
            
            // 检查是否有error字段
            if (json.has("error")) {
                String errorMsg = json.getAsJsonObject("error").get("message").getAsString();
                return "API错误: " + errorMsg;
            }
            
            // 检查响应是否符合通义千问格式
            if (json.has("output") && json.getAsJsonObject("output").has("choices")) {
                JsonArray choices = json.getAsJsonObject("output").getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject choice = choices.get(0).getAsJsonObject();
                    if (choice.has("message") && choice.getAsJsonObject("message").has("content")) {
                        String content = choice.getAsJsonObject("message").get("content").getAsString();
                        return content;
                    }
                }
            }
            
            // 如果没有找到预期格式，返回错误信息
            return "API响应格式不正确：" + responseBody;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Parse error: " + e.getMessage()); // 添加调试输出
            return "AI 解释生成失败: " + e.getMessage();
        }
    }
}