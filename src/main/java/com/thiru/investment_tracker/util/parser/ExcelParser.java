package com.thiru.investment_tracker.util.parser;

import com.thiru.investment_tracker.dto.InputRecord;
import com.thiru.investment_tracker.dto.InputRecords;
import com.thiru.investment_tracker.dto.enums.ParserDataType;
import com.thiru.investment_tracker.exception.BadRequestException;
import com.thiru.investment_tracker.util.collection.TLocaleDate;
import com.thiru.investment_tracker.util.collection.TOptional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class ExcelParser {

    private static final String EXCEL_TYPE = "text/xls";

    public static final String ASSETS = "ASSETS";
    public static final String TRANSACTIONS = "TRANSACTIONS";
    public static final String PORTFOLIO_FILE_NAME = "portfolio.xlsx";
    public static final String TRANSACTION_FILE_NAME = "transactions.xlsx";
    public static final String HOLDINGS_FILE_NAME = "holdings.xlsx";

    public static boolean isValidExcelFile(MultipartFile file) {
        return EXCEL_TYPE.equals(file.getContentType());
    }

    public static InputRecords getRecordsFromExcel(InputStream inputStream, Map<String, ParserDataType> dataTypeMap) {

        try {
            XSSFWorkbook excelWorkbook = new XSSFWorkbook(inputStream);
            XSSFSheet tradeSheet = excelWorkbook.getSheetAt(0);
            InputRecords inputRecords = new InputRecords();

            int rowIndex = 0;
            for (Row row : tradeSheet) {
                if (rowIndex == 0) {
                    List<String> fileHeaders = extractHeaders(row);
                    inputRecords.setHeaders(fileHeaders);
                } else {

                    InputRecord inputRecord = new InputRecord();
                    Map<String, CellDetail> record = extractRows(row, inputRecords.getHeaders(), dataTypeMap);
                    inputRecord.setRecord(record);
                    inputRecord.setRecordNumber(rowIndex);

                    inputRecords.getRecords().add(inputRecord);
                }
                rowIndex++;
            }

            return inputRecords;
        } catch (IOException e) {
            throw new BadRequestException("Data is not in valid format");
        }
    }

    private static List<String> extractHeaders(Row row) {
        Iterator<Cell> cellIterator = row.cellIterator();

        List<String> fileHeaders = new ArrayList<>();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            fileHeaders.add(cell.getStringCellValue());
        }
        return fileHeaders;
    }

    private static Map<String, CellDetail> extractRows(Row row, List<String> fileHeaders,
                                                       Map<String, ParserDataType> dataTypeMap) {

        Iterator<Cell> cellIterator = row.cellIterator();
        Map<String, CellDetail> record = new HashMap<>();

        int columnIndex = 0;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            String cellHeader = fileHeaders.get(columnIndex);
            CellDetail cellDetail = getCellDetail(cell, cellHeader, dataTypeMap);
            record.put(cellHeader, cellDetail);
            columnIndex++;
        }
        if (record.size() != fileHeaders.size()) {
            return null;
        }

        log.info("Parsed Record: {}", record);
        return record;
    }

    private static CellDetail getCellDetail(Cell cell, String cellHeader, Map<String, ParserDataType> dataTypeMap) {

        try {
            ParserDataType parserDataType = dataTypeMap.getOrDefault(cellHeader, ParserDataType.NULL);

            return switch (parserDataType) {
                case BOOLEAN -> CellDetail.of(ParserDataType.BOOLEAN, cell.getBooleanCellValue());
                case INTEGER, LONG -> CellDetail.of(ParserDataType.LONG, (long) cell.getNumericCellValue());
                case DOUBLE -> CellDetail.of(ParserDataType.DOUBLE, cell.getNumericCellValue());
                case STRING -> CellDetail.of(ParserDataType.STRING, cell.getStringCellValue());
                case LOCAL_DATE_TIME -> CellDetail.of(ParserDataType.LOCAL_DATE_TIME,
                        TLocaleDate.convertToDateTime(cell.getStringCellValue()));
                case LOCAL_DATE -> CellDetail.of(ParserDataType.LOCAL_DATE,
                        TOptional.map1(cell.getLocalDateTimeCellValue(), LocalDateTime::toLocalDate));
                case ERROR -> CellDetail.of(ParserDataType.ERROR, cell.getErrorCellValue());
                case NULL -> CellDetail.of(ParserDataType.NULL, null);
            };
        } catch (Exception e) {
            log.error("Error while parsing excel file error: {}, cellHeader: {}", e, cellHeader);
            throw new BadRequestException(e.getMessage());
        }
    }

    public static void initialiseExcelSheet(XSSFWorkbook workbook, String[] headers, String sheetName) {

        XSSFSheet sheet = workbook.createSheet(sheetName);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);

            // Create cell style with bold font
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            // Apply style to header cells
            headerCell.setCellStyle(headerCellStyle);
        }
    }
}
