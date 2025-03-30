package com.thiru.investment_tracker.util.transaction;

import java.util.HashMap;
import java.util.Map;

import com.thiru.investment_tracker.dto.enums.ParserDataType;
import com.thiru.investment_tracker.entity.Transaction;

public class ExcelHeaders {

	public static final String EMAIL = "Email";
	public static final String STOCK_CODE = "Stock Code";
	public static final String STOCK_NAME = "Stock Name";
	public static final String ORDER_ID = "Order ID";
	public static final String ORDER_EXECUTION_TIME = "Order Execution Time";
	public static final String TIME_ZONE = "Time Zone";
	public static final String EXCHANGE_NAME = "Exchange Name";
	public static final String BROKER_NAME = "Broker Name";
	public static final String ASSET_TYPE = "Asset Type";
	public static final String MATURITY_DATE = "Maturity Date";
	public static final String PRICE = "Price";
	public static final String TOTAL_VALUE = "Total Value";
	public static final String QUANTITY = "Quantity";
	public static final String TRANSACTION_TYPE = "Transaction Type";
	public static final String ACTOR = "Actor";
	public static final String TRANSACTION_DATE = "Transaction Date";
	public static final String BROKER_CHARGES = "Broker Charges";
	public static final String MISC_CHARGES = "Misc. Charges";
	public static final String COMMENT = "Comment";

	public static Map<String, ParserDataType> getDataTypeMap() {

		Map<String, ParserDataType> dataTypeMap = new HashMap<>();

		dataTypeMap.put(ExcelHeaders.STOCK_CODE, ParserDataType.STRING);
		dataTypeMap.put(ExcelHeaders.STOCK_NAME, ParserDataType.STRING);
		dataTypeMap.put(ExcelHeaders.EXCHANGE_NAME, ParserDataType.STRING);
		dataTypeMap.put(ExcelHeaders.BROKER_NAME, ParserDataType.STRING);
		dataTypeMap.put(ExcelHeaders.ACTOR, ParserDataType.STRING);
		dataTypeMap.put(ExcelHeaders.ASSET_TYPE, ParserDataType.STRING);
		dataTypeMap.put(ExcelHeaders.MATURITY_DATE, ParserDataType.LOCAL_DATE);
		dataTypeMap.put(ExcelHeaders.PRICE, ParserDataType.DOUBLE);
		dataTypeMap.put(ExcelHeaders.QUANTITY, ParserDataType.DOUBLE);
		dataTypeMap.put(ExcelHeaders.TRANSACTION_TYPE, ParserDataType.STRING);
		dataTypeMap.put(ExcelHeaders.TRANSACTION_DATE, ParserDataType.LOCAL_DATE);
		dataTypeMap.put(ExcelHeaders.BROKER_CHARGES, ParserDataType.DOUBLE);
		dataTypeMap.put(ExcelHeaders.MISC_CHARGES, ParserDataType.DOUBLE);
		dataTypeMap.put(ExcelHeaders.ORDER_ID, ParserDataType.STRING);
		dataTypeMap.put(ExcelHeaders.ORDER_EXECUTION_TIME, ParserDataType.LOCAL_DATE_TIME);
		dataTypeMap.put(ExcelHeaders.TIME_ZONE, ParserDataType.STRING);
		dataTypeMap.put(ExcelHeaders.COMMENT, ParserDataType.STRING);

		return dataTypeMap;
	}

	public static String[] getTransactionHeaders() {
		return new String[]{EMAIL, STOCK_CODE, STOCK_NAME, EXCHANGE_NAME, BROKER_NAME, ASSET_TYPE, MATURITY_DATE, PRICE,
				QUANTITY, TRANSACTION_TYPE, ACTOR, TRANSACTION_DATE, BROKER_CHARGES, MISC_CHARGES, COMMENT};
	}

	public static String[] getPortfolioHeaders() {
		return new String[]{ExcelHeaders.EMAIL, ExcelHeaders.STOCK_NAME, ExcelHeaders.STOCK_CODE,
				ExcelHeaders.QUANTITY, "Total Quantity", ExcelHeaders.PRICE, ExcelHeaders.TOTAL_VALUE,
				ExcelHeaders.EXCHANGE_NAME, ExcelHeaders.BROKER_NAME, ExcelHeaders.ASSET_TYPE,
				ExcelHeaders.MATURITY_DATE, ExcelHeaders.BROKER_CHARGES, ExcelHeaders.MISC_CHARGES};
	}

	public static String[] getTransactionQuantityHeaders() {
		return new String[]{ExcelHeaders.STOCK_CODE, ExcelHeaders.BROKER_NAME, "Transaction Date", ExcelHeaders.QUANTITY};
	}
}
