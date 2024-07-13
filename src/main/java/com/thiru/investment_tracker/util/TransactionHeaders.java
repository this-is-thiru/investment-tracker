package com.thiru.investment_tracker.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thiru.investment_tracker.dto.enums.ParserDataType;

public class TransactionHeaders {

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

	public static List<String> getHeadersList() {
		return List.of(EMAIL, STOCK_CODE, STOCK_NAME, EXCHANGE_NAME, BROKER_NAME, ASSET_TYPE, MATURITY_DATE, PRICE,
				QUANTITY, TRANSACTION_TYPE, ACTOR, TRANSACTION_DATE, BROKER_CHARGES, MISC_CHARGES, COMMENT);
	}
}
