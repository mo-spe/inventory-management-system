package com.example.inventorybackend.Controller;

import com.example.inventorybackend.Service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// RecommendationController.java
@RestController
@CrossOrigin
@RequestMapping("/api/recommend")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * GET /api/recommend/restock
     * 返回补货建议
     */
    @GetMapping("/restock")
    public List<Map<String, Object>> getRestockSuggestions() {
        return recommendationService.getRestockSuggestions();
    }
}
