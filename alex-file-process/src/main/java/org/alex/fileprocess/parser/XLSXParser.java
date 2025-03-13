package org.alex.fileprocess.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class XLSXParser extends AbstractFileParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize) {
        List<String> chunks = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis)) {

            List<Map<String, Object>> structuredData = new ArrayList<>();

            for (Sheet sheet : workbook) {
                List<Map<String, String>> sheetData = new ArrayList<>();
                List<String> headers = new ArrayList<>();

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        // 处理表头
                        for (Cell cell : row) {
                            headers.add(cell.getStringCellValue());
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

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
