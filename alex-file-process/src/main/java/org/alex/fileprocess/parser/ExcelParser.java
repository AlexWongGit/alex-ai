package org.alex.fileprocess.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alex.common.enums.FileTypeEnum;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.*;

/**
 * @description Excel文件解析器
 * @Author wangzf
 * @Date 2025/3/12
 */
public class ExcelParser extends AbstractFileParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<String> split2Chunks(File file, int maxChunkSize, FileTypeEnum fileType) {
        List<String> chunks = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = createWorkbook(fis, fileType)) {
            List<Map<String, Object>> structuredData = new ArrayList<>();
            for (Sheet sheet : workbook) {
                structuredData.add(parseSheet(sheet));
            }
            String jsonOutput = OBJECT_MAPPER.writeValueAsString(structuredData);
            chunks.addAll(splitText(jsonOutput, maxChunkSize));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chunks;
    }

    /**
     * @description 根据文件类型创建工作簿
     * @return org.apache.poi.ss.usermodel.Workbook
     * @param fis 文件输入流
     * @param fileType 文件类型
     */
    private Workbook createWorkbook(FileInputStream fis, FileTypeEnum fileType) throws IOException {
        if (fileType == FileTypeEnum.XLS) {
            return new HSSFWorkbook(fis);
        } else if (fileType == FileTypeEnum.XLSX) {
            return new XSSFWorkbook(fis);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
    }

    /**
     * @description 解析工作表
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @param sheet 工作表
     */
    private Map<String, Object> parseSheet(Sheet sheet) {
        Map<String, Object> sheetInfo = new HashMap<>();
        List<Map<String, String>> sheetData = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        Map<String, String> mergedCellMap = extractMergedCells(sheet);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                for (Cell cell : row) {
                    headers.add(getCellValue(cell));
                }
            } else {
                Map<String, String> rowData = new HashMap<>();
                // 遍历每一行单元格
                for (int i = 0; i < headers.size(); i++) {
                    String cellValue = getCellValue(row.getCell(i));
                    if (cellValue.isEmpty()) {
                        cellValue = mergedCellMap.get(row.getRowNum() + "," + i);
                    }
                    rowData.put(headers.get(i), cellValue);
                }
                sheetData.add(rowData);
            }
        }
        sheetInfo.put("SheetName", sheet.getSheetName());
        sheetInfo.put("Data", sheetData);
        return sheetInfo;
    }

    /**
     * @description 提取合并单元格
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @param sheet 工作表
     */
    private Map<String, String> extractMergedCells(Sheet sheet) {
        Map<String, String> mergedCellMap = new HashMap<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            Cell primaryCell = sheet.getRow(mergedRegion.getFirstRow()).getCell(mergedRegion.getFirstColumn());
            String mergedValue = getCellValue(primaryCell);
            for (int row = mergedRegion.getFirstRow(); row <= mergedRegion.getLastRow(); row++) {
                for (int col = mergedRegion.getFirstColumn(); col <= mergedRegion.getLastColumn(); col++) {
                    mergedCellMap.put(row + "," + col, mergedValue);
                }
            }
        }
        return mergedCellMap;
    }


    /**
     * @description 获取单元格的值
     * @return java.lang.String
     * @param cell 单元格
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