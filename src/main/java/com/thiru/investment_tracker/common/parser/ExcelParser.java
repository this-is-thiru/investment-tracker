package com.thiru.investment_tracker.common.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import com.thiru.investment_tracker.exception.BadRequestException;
import com.thiru.investment_tracker.dto.InputRecord;
import com.thiru.investment_tracker.dto.InputRecords;

public class ExcelParser {

	private static final String EXCEL_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	public static boolean isValidExcelFile(MultipartFile file) {
		return Objects.equals(file.getContentType(), EXCEL_TYPE);
	}

	public InputRecords getRecordsFromExcel(InputStream inputStream) {

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
					Map<String, String> record = extractRows(row, inputRecords.getHeaders());
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

	private static Map<String, String> extractRows(Row row, List<String> fileHeaders) {

		Iterator<Cell> cellIterator = row.cellIterator();
		Map<String, String> record = new HashMap<>();

		int columnIndex = 0;
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			String cellHeader = fileHeaders.get(columnIndex);
			record.put(cellHeader, cell.getStringCellValue());
			columnIndex++;
		}
		return record;
	}
}
