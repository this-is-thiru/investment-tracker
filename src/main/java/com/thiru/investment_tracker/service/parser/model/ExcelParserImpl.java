package com.thiru.investment_tracker.service.parser.model;

import com.thiru.investment_tracker.dto.InputRecord;
import com.thiru.investment_tracker.dto.InputRecords;
import com.thiru.investment_tracker.dto.enums.ExcelDataType;
import com.thiru.investment_tracker.exception.BadRequestException;
import com.thiru.investment_tracker.util.collection.TLocaleDate;
import com.thiru.investment_tracker.util.collection.TOptional;
import com.thiru.investment_tracker.util.parser.CellDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class ExcelParserImpl implements ExcelParser {

    @Override
    public InputRecords parse(MultipartFile file, Map<String, ExcelDataType> dataTypeMap) {

        try {
            XSSFWorkbook excelWorkbook = new XSSFWorkbook(file.getInputStream());
            XSSFSheet transactionsSheet = excelWorkbook.getSheetAt(0);

            InputRecords inputRecords = new InputRecords();

            int rowIndex = 0;
            for (Row row : transactionsSheet) {
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

    private static Map<String, CellDetail> extractRows(Row row, List<String> fileHeaders, Map<String, ExcelDataType> dataTypeMap) {

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

    private static CellDetail getCellDetail(Cell cell, String cellHeader, Map<String, ExcelDataType> dataTypeMap) {

        try {
            ExcelDataType excelDataType = dataTypeMap.getOrDefault(cellHeader, ExcelDataType.NULL);

            return switch (excelDataType) {
                case BOOLEAN -> CellDetail.of(ExcelDataType.BOOLEAN, cell.getBooleanCellValue());
                case INTEGER, LONG -> CellDetail.of(ExcelDataType.LONG, (long) cell.getNumericCellValue());
                case DOUBLE -> CellDetail.of(ExcelDataType.DOUBLE, cell.getNumericCellValue());
                case STRING -> CellDetail.of(ExcelDataType.STRING, cell.getStringCellValue());
                case LOCAL_DATE_TIME ->
                        CellDetail.of(ExcelDataType.LOCAL_DATE_TIME, TLocaleDate.convertToDateTime(cell.getStringCellValue()));
                case LOCAL_DATE ->
                        CellDetail.of(ExcelDataType.LOCAL_DATE, TOptional.map1(cell.getLocalDateTimeCellValue(), LocalDateTime::toLocalDate));
                case ERROR -> CellDetail.of(ExcelDataType.ERROR, cell.getErrorCellValue());
                case NULL -> CellDetail.of(ExcelDataType.NULL, null);
            };
        } catch (Exception e) {
            log.error("Error while parsing excel file error: {}, cellHeader: {}", e, cellHeader);
            throw new BadRequestException(e.getMessage());
        }
    }
}
