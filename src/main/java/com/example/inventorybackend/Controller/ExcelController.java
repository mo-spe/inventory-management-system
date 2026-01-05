package com.example.inventorybackend.Controller;

import com.example.inventorybackend.Service.ExcelService;
import com.example.inventorybackend.Service.ProductService;
import com.example.inventorybackend.entity.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin
@Tag(name = "Excel管理", description = "商品Excel导入导出功能")
public class ExcelController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ExcelService excelService;

    /**
     * 导出所有商品到 Excel
     */
    @Operation(summary = "导出商品到Excel", description = "将所有商品导出为Excel文件")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        List<Product> products = productService.getAllProducts();

        Workbook workbook = excelService.exportProductsToExcel(products);

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=inventories.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * 导入商品（从 Excel 文件）
     */
    // ExcelController.java
    @Operation(summary = "从Excel导入商品", description = "从Excel文件导入商品信息")
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<String> handleImport(@Parameter(description = "Excel文件",
                                                 required = true)
                                                   @RequestParam("file") MultipartFile file)
    {
        try {
            List<Product> imported = excelService.importProductsFromExcel(file);

            int successCount = 0;
            for (Product p : imported) {
                boolean exists = productService.findById(p.getId()) != null;

                if (exists) {
                    // 是已有商品 → 比较库存变化
                    Product old = productService.findById(p.getId());
                    int diff = p.getStock() - old.getStock();

                    // 执行更新（会自动判断是否为入库）
                    boolean updated = productService.updateProduct(p.getId(), p);

                    if (updated) {
                        // 记录出入库日志
                        if (diff > 0) {
                            productService.logStockChange(p.getId(), p.getName(), "入库", diff);
                        } else if (diff < 0) {
                            productService.logStockChange(p.getId(), p.getName(), "出库", Math.abs(diff));
                        }
                        successCount++;
                    }
                } else {
                    // 新商品 → 添加 + 上架日志
                    boolean added = productService.addProduct(p);
                    if (added) {
                        productService.logStockChange(p.getId(), p.getName(), "上架", p.getStock());
                        successCount++;
                    }
                }
            }

            return ResponseEntity.ok("✅ 成功处理 " + successCount + " 条商品");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ 导入失败：" + e.getMessage());
        }
    }


}