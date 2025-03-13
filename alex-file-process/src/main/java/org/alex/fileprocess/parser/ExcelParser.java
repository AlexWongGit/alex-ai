package org.alex.fileprocess.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alex.common.enums.FileTypeEnum;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.*;

/**
 *
 * @Author wangzf
 * @Date 2025/3/12
 */
public class ExcelParser extends AbstractFileParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize, FileTypeEnum fileType) {
        List<String> chunks = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = createWorkbook(fis, fileType)) {  // 根据 fileType 创建 Workbook

            List<Map<String, Object>> structuredData = new ArrayList<>();

            for (Sheet sheet : workbook) {
                List<Map<String, String>> sheetData = new ArrayList<>();
                List<String> headers = new ArrayList<>();

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        // 处理表头
                        for (Cell cell : row) {
                            headers.add(getCellValue(cell));
                        }
                    } else {
                        // 处理数据行
                        Map<String, String> rowData = new HashMap<>();
                        for (int i = 0; i < headers.size(); i++) {
                            rowData.put(headers.get(i), getCellValue(row.getCell(i)));
                        }
                        sheetData.add(rowData);
                    }
                }

                Map<String, Object> sheetInfo = new HashMap<>();
                sheetInfo.put("SheetName", sheet.getSheetName());
                sheetInfo.put("Data", sheetData);
                structuredData.add(sheetInfo);
            }

            // 转换为 JSON 字符串
            String jsonOutput = OBJECT_MAPPER.writeValueAsString(structuredData);

            // 分块存储
            chunks.addAll(splitText(jsonOutput, maxChunkSize));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunks;
    }

    /**
     * 根据文件类型创建 Workbook
     */
    private Workbook createWorkbook(FileInputStream fis, FileTypeEnum fileType) throws IOException {
        if (fileType == FileTypeEnum.XLS) {
            // 解析 .xls（HSSF）
            return new HSSFWorkbook(fis);
        } else if (fileType == FileTypeEnum.XLSX) {
            // 解析 .xlsx（XSSF）
            return new XSSFWorkbook(fis);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
    }

    /**
     * 获取 Excel 单元格的值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK, ERROR, default -> "";
        };
    }

}
