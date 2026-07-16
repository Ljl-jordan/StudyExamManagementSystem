// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/utils/ExcelUtil.java
package com.ljl.studyexammanagementsystem.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    /**
     * 导出Excel（.xlsx格式）
     * @param filePath  输出文件路径
     * @param sheetName 工作表名称
     * @param headers   表头列名列表
     * @param dataList  数据行，每行是一个List<String>
     */
    public static void export(String filePath, String sheetName, List<String> headers, List<List<String>> dataList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // 写入表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // 写入数据行
        for (int i = 0; i < dataList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            List<String> rowData = dataList.get(i);
            for (int j = 0; j < rowData.size(); j++) {
                row.createCell(j).setCellValue(rowData.get(j));
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }
        workbook.close();
    }

    /**
     * 读取Excel第一Sheet的所有数据（不含表头）
     * @param filePath Excel文件路径
     * @return 每行数据为 List<String>
     */
    public static List<List<String>> read(String filePath) throws IOException {
        List<List<String>> result = new java.util.ArrayList<>();
        try (FileInputStream fis = new java.io.FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                List<String> rowData = new java.util.ArrayList<>();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.add(cell != null ? cell.toString() : "");
                }
                result.add(rowData);
            }
        }
        return result;
    }
}
