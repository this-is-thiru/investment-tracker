package com.thiru.investment_tracker.util.transaction;

import java.util.HashMap;
import java.util.Map;

import com.thiru.investment_tracker.dto.enums.ExcelDataType;

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
	public static final String TRANSACTION_DATE = "Transaction Date";
	public static final String BROKER_CHARGES = "Broker Charges";
	public static final String MISC_CHARGES = "Misc. Charges";
	public static final String COMMENT = "Comment";

	public static Map<String, ExcelDataType> getDataTypeMap() {

		Map<String, ExcelDataType> dataTypeMap = new HashMap<>();

		dataTypeMap.put(ExcelHeaders.STOCK_CODE, ExcelDataType.STRING);
		dataTypeMap.put(ExcelHeaders.STOCK_NAME, ExcelDataType.STRING);
		dataTypeMap.put(ExcelHeaders.EXCHANGE_NAME, ExcelDataType.STRING);
		dataTypeMap.put(ExcelHeaders.BROKER_NAME, ExcelDataType.STRING);
		dataTypeMap.put(ExcelHeaders.ASSET_TYPE, ExcelDataType.STRING);
		dataTypeMap.put(ExcelHeaders.MATURITY_DATE, ExcelDataType.LOCAL_DATE);
		dataTypeMap.put(ExcelHeaders.PRICE, ExcelDataType.DOUBLE);
		dataTypeMap.put(ExcelHeaders.QUANTITY, ExcelDataType.DOUBLE);
		dataTypeMap.put(ExcelHeaders.TRANSACTION_TYPE, ExcelDataType.STRING);
		dataTypeMap.put(ExcelHeaders.TRANSACTION_DATE, ExcelDataType.LOCAL_DATE);
		dataTypeMap.put(ExcelHeaders.BROKER_CHARGES, ExcelDataType.DOUBLE);
		dataTypeMap.put(ExcelHeaders.MISC_CHARGES, ExcelDataType.DOUBLE);
		dataTypeMap.put(ExcelHeaders.ORDER_ID, ExcelDataType.STRING);
		dataTypeMap.put(ExcelHeaders.ORDER_EXECUTION_TIME, ExcelDataType.LOCAL_DATE_TIME);
		dataTypeMap.put(ExcelHeaders.TIME_ZONE, ExcelDataType.STRING);
		dataTypeMap.put(ExcelHeaders.COMMENT, ExcelDataType.STRING);

		return dataTypeMap;
	}

	public static String[] getTransactionHeaders() {
		return new String[]{EMAIL, STOCK_CODE, STOCK_NAME, EXCHANGE_NAME, BROKER_NAME, ASSET_TYPE, MATURITY_DATE, PRICE,
				QUANTITY, TRANSACTION_TYPE, TRANSACTION_DATE, BROKER_CHARGES, MISC_CHARGES, COMMENT};
	}

	public static String[] getPortfolioHeaders() {
		return new String[]{ExcelHeaders.EMAIL, ExcelHeaders.STOCK_NAME, ExcelHeaders.STOCK_CODE,
				ExcelHeaders.QUANTITY, "Total Quantity", ExcelHeaders.PRICE, ExcelHeaders.TOTAL_VALUE,
				ExcelHeaders.EXCHANGE_NAME, ExcelHeaders.BROKER_NAME, ExcelHeaders.ASSET_TYPE,
				ExcelHeaders.MATURITY_DATE, ExcelHeaders.BROKER_CHARGES, ExcelHeaders.MISC_CHARGES, "hiii"};
	}

	public static String[] getTransactionQuantityHeaders() {
		return new String[]{ExcelHeaders.STOCK_CODE, ExcelHeaders.BROKER_NAME, "Transaction Date", ExcelHeaders.QUANTITY};
	}
}
