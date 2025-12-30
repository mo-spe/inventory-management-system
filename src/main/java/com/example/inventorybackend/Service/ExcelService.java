package com.example.inventorybackend.Service;

// ExcelService.java
import com.example.inventorybackend.entity.Product;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelService {

    /**
     * 将商品列表导出为 Excel 工作簿
     */
    public Workbook exportProductsToExcel(List<Product> products) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("商品列表");

        // 表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"编号", "名称", "分类", "进价", "售价", "库存"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // 数据行
        int rowNum = 1;
        for (Product p : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getName());
            row.createCell(2).setCellValue(p.getCategory());
            row.createCell(3).setCellValue(p.getBuyPrice());
            row.createCell(4).setCellValue(p.getSellPrice());
            row.createCell(5).setCellValue(p.getStock());
        }

        // 自动列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    /**
     * 从上传的 Excel 文件中读取商品数据
     */
    public List<Product> importProductsFromExcel(MultipartFile file) throws IOException {
        List<Product> products = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next(); // 跳过表头

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Product product = new Product();
                product.setId(getCellValue(row, 0));
                product.setName(getCellValue(row, 1));
                product.setCategory(getCellValue(row, 2));

                try {
                    product.setBuyPrice(Double.parseDouble(getCellValue(row, 3)));
                    product.setSellPrice(Double.parseDouble(getCellValue(row, 4)));
                    product.setStock((int) Double.parseDouble(getCellValue(row, 5)));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("第" + (row.getRowNum() + 1) + "行价格或库存格式错误");
                }

                products.add(product);
            }
        }
        return products;
    }

    /**
     * 安全读取单元格值（避免空指针）
     */
    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue()); // 避免 .0
            default:
                return "";
        }
    }
}

