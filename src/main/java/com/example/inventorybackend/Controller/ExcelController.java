package com.example.inventorybackend.Controller;

// ExcelController.java
import com.example.inventorybackend.Service.ExcelService;
import com.example.inventorybackend.Service.ProductService;
import com.example.inventorybackend.entity.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String handleImport(@RequestParam("file") MultipartFile file) {
        try {
            List<Product> imported = excelService.importProductsFromExcel(file);

            int successCount = 0;
            for (Product p : imported) {
                boolean added = productService.addProduct(p); // 注意：重复 ID 不会覆盖
                if (added) successCount++;
            }

            return "✅ 成功导入 " + successCount + " 条商品（共 " + imported.size() + " 条）";
        } catch (Exception e) {
            return "❌ 导入失败：" + e.getMessage();
        }
    }
}

