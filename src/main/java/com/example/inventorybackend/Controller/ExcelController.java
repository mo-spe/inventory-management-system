package com.example.inventorybackend.Controller;

// ExcelController.java
import com.example.inventorybackend.Service.ExcelService;
import com.example.inventorybackend.Service.ProductService;
import com.example.inventorybackend.entity.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin
public class ExcelController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ExcelService excelService;

    /**
     * 导出所有商品到 Excel
     */
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
    @PostMapping("/import")
    public ResponseEntity<String> handleImport(@RequestParam("file") MultipartFile file) {
        try {
            List<Product> imported = excelService.importProductsFromExcel(file);

            int successCount = 0;
            for (Product p : imported) {
                boolean isUpdate = productService.findById(p.getId()) != null;

                if (isUpdate) {
                    // 是更新：比较库存变化 → 记录入库/出库
                    Product old = productService.findById(p.getId());
                    int diff = p.getStock() - old.getStock();

                    // 执行更新
                    productService.updateProduct(p.getId(), p);

                    if (diff > 0) {
                        productService.logStockChange(p.getId(), p.getName(), "入库", diff);
                    } else if (diff < 0) {
                        productService.logStockChange(p.getId(), p.getName(), "出库", Math.abs(diff));
                    }
                } else {
                    // 是新增商品
                    productService.addProduct(p);
                    productService.logStockChange(p.getId(), p.getName(), "上架", p.getStock());
                }

                successCount++;
            }

            return ResponseEntity.ok("✅ 成功处理 " + successCount + " 条商品");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ 导入失败：" + e.getMessage());
        }
    }

}

