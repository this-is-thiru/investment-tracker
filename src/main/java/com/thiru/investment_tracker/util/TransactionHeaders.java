package com.thiru.investment_tracker.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.thiru.investment_tracker.common.parser.ExcelParser;
import com.thiru.investment_tracker.dto.enums.ParserDataType;

public class TransactionHeaders {

	public static final String MAIN_SHEET = "main_sheet";
	public static final String HELPER_SHEET = "helper_sheet";

	public static final String EMAIL = "Email";
	public static final String STOCK_CODE = "Stock Code";
	public static final String STOCK_NAME = "Stock Name";
	public static final String EXCHANGE_NAME = "Exchange Name";
	public static final String BROKER_NAME = "Broker Name";
	public static final String ASSET_TYPE = "Asset Type";
	public static final String MATURITY_DATE = "Maturity Date";
	public static final String PRICE = "Price";
	public static final String QUANTITY = "Quantity";
	public static final String TRANSACTION_TYPE = "Transaction Type";
	public static final String ACTOR = "Actor";
	public static final String TRANSACTION_DATE = "Transaction Date";
	public static final String BROKER_CHARGES = "Broker Charges";
	public static final String MISC_CHARGES = "Misc. Charges";
	public static final String COMMENT = "Comment";

	public static Map<String, ParserDataType> getDataTypeMap() {

		Map<String, ParserDataType> dataTypeMap = new HashMap<>();

		dataTypeMap.put(TransactionHeaders.STOCK_CODE, ParserDataType.STRING);
		dataTypeMap.put(TransactionHeaders.STOCK_NAME, ParserDataType.STRING);
		dataTypeMap.put(TransactionHeaders.EXCHANGE_NAME, ParserDataType.STRING);
		dataTypeMap.put(TransactionHeaders.BROKER_NAME, ParserDataType.STRING);
		dataTypeMap.put(TransactionHeaders.ACTOR, ParserDataType.STRING);
		dataTypeMap.put(TransactionHeaders.ASSET_TYPE, ParserDataType.STRING);
		dataTypeMap.put(TransactionHeaders.MATURITY_DATE, ParserDataType.LOCAL_DATE);
		dataTypeMap.put(TransactionHeaders.PRICE, ParserDataType.DOUBLE);
		dataTypeMap.put(TransactionHeaders.QUANTITY, ParserDataType.INTEGER);
		dataTypeMap.put(TransactionHeaders.TRANSACTION_TYPE, ParserDataType.STRING);
		dataTypeMap.put(TransactionHeaders.TRANSACTION_DATE, ParserDataType.LOCAL_DATE);
		dataTypeMap.put(TransactionHeaders.BROKER_CHARGES, ParserDataType.DOUBLE);
		dataTypeMap.put(TransactionHeaders.MISC_CHARGES, ParserDataType.DOUBLE);
		dataTypeMap.put(TransactionHeaders.COMMENT, ParserDataType.STRING);

		return dataTypeMap;
	}

	public static String[] getHeaders() {
		return new String[]{EMAIL, STOCK_CODE, STOCK_NAME, EXCHANGE_NAME, BROKER_NAME, ASSET_TYPE, MATURITY_DATE, PRICE,
				QUANTITY, TRANSACTION_TYPE, ACTOR, TRANSACTION_DATE, BROKER_CHARGES, MISC_CHARGES, COMMENT};
	}

	public static ByteArrayInputStream downloadTemplate() {

		// XSSFWorkbook workbook = new XSSFWorkbook();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			ExcelParser.dataToExcel(workbook);
			Sheet sheet = workbook.getSheetAt(0);

			Row dataRow = sheet.createRow(1);
			dataRow.createCell(0).setCellValue("email@gmail.com");
			dataRow.createCell(1).setCellValue("STOCK");
			dataRow.createCell(2).setCellValue("Stock Name");
			dataRow.createCell(3).setCellValue("NSE");
			dataRow.createCell(4).setCellValue("Fyers");
			dataRow.createCell(5).setCellValue("Equity");
			setDateField(dataRow.createCell(6), LocalDate.now());
			dataRow.createCell(7).setCellValue(0);
			dataRow.createCell(8).setCellValue(0);
			dataRow.createCell(9).setCellValue("BUY");
			dataRow.createCell(10).setCellValue("actor@gmail.com");
			setDateField(dataRow.createCell(11), LocalDate.now());
			dataRow.createCell(12).setCellValue(0);
			dataRow.createCell(13).setCellValue(0);
			dataRow.createCell(14).setCellValue("comment");

			workbook.write(outputStream);
			return new ByteArrayInputStream(outputStream.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void setDateField(Cell cell, LocalDate date) {
		CellStyle dateStyle = cell.getSheet().getWorkbook().createCellStyle();
		CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
		dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
		cell.setCellStyle(dateStyle);
		cell.setCellValue(date);
	}
}
