package com.thiru.investment_tracker.service.export.writer.model;

import com.thiru.investment_tracker.dto.enums.ExcelDataType;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractExcelWorkbookWriter<Entity> implements ExcelWorkbookWriter<Entity> {

    private final String sheetName;
    private final List<String> columnFields;
    private final Map<String, Integer> columnIndexMap;

    public AbstractExcelWorkbookWriter(String sheetName, List<String> columnFields) {
        this.sheetName = sheetName;
        this.columnFields = columnFields.isEmpty() ? this.orderedColumns() : columnFields;
        this.columnIndexMap = this.sanitizeAndGetAsColumnIndexMap(this.columnFields);
    }

    protected abstract List<String> orderedColumns();

    protected abstract Map<String, String> simpleColumnHeaders();

    protected abstract Map<String, Function<Entity, ExcelDataTypePair>> simpleColumnValueMap();

    @Override
    public InputStreamResource process(List<Entity> entities) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = ExcelSheetHelper.createNewSheet(workbook, this.headers(), sheetName);
            sheetWriter(sheet, entities);
            workbook.write(outputStream);
            return new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sheetWriter(Sheet sheet, List<Entity> entities) {

        for (int rowIndex = 0; rowIndex < entities.size(); rowIndex++) {
            Entity entity = entities.get(rowIndex);
            Row row = sheet.createRow(rowIndex + 1);
            for (String columnField : columnFields) {
                cellPopulator(row.createCell(columnIndexMap.get(columnField)), columnField, entity);
            }
        }
    }

    private void cellPopulator(Cell cell, String columnField, Entity entity) {

        ExcelDataTypePair pair = this.simpleColumnValueMap().get(columnField).apply(entity);
        if (pair.data() == null) {
            return;
        }

        switch (pair.excelDataType()) {
            case STRING:
                cell.setCellValue((String) pair.data());
                break;
            case DOUBLE:
                cell.setCellValue((Double) pair.data());
                break;
            case LOCAL_DATE:
                setDateField(cell, (LocalDate) pair.data());
                break;
            default:
                throw new IllegalArgumentException("Cell value conversion not supported: " + pair.excelDataType());
        }
    }

    private static void setDateField(Cell cell, LocalDate date) {
        CellStyle dateStyle = cell.getSheet().getWorkbook().createCellStyle();
        CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(TCollectionUtil.DATE_FORMAT));
        cell.setCellStyle(dateStyle);
        cell.setCellValue(date);
    }

    private Map<String, Integer> sanitizeAndGetAsColumnIndexMap(List<String> columnFields) {

        List<String> sanitizedColumnNames = TCollectionUtil.filter(columnFields, this.orderedColumns()::contains);

        Map<String, Integer> sanitizedColumnIndexMap = new HashMap<>(sanitizedColumnNames.size());
        for (int i = 0; i < sanitizedColumnNames.size(); i++) {
            sanitizedColumnIndexMap.put(sanitizedColumnNames.get(i), i);
        }
        return sanitizedColumnIndexMap;
    }

    private List<String> headers() {
        return TCollectionUtil.map(orderedColumns(), column -> simpleColumnHeaders().get(column).toUpperCase());
    }

    protected static ExcelDataTypePair dataTypePair(Object data, ExcelDataType excelDataType) {
        return new ExcelDataTypePair(data, excelDataType);
    }
}
