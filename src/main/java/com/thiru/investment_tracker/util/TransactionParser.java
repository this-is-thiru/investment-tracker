package com.thiru.investment_tracker.util;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.thiru.investment_tracker.common.TCommonUtil;
import com.thiru.investment_tracker.common.parser.CellDetail;
import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.InputRecord;
import com.thiru.investment_tracker.dto.InputRecords;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.TransactionType;

public class TransactionParser {

	public static List<AssetRequest> getTransactionRecords(InputRecords records) {
		return TCommonUtil.map(sanitizeRecords(records), TransactionParser::toAssetRequest);
	}

	public static List<InputRecord> sanitizeRecords(InputRecords records) {
		return records.getRecords().stream().filter(Objects::nonNull)
				.filter(inputRecord -> inputRecord.getRecord() != null).collect(Collectors.toList());
	}

	private static AssetRequest toAssetRequest(InputRecord inputRecord) {

		Map<String, CellDetail> record = inputRecord.getRecord();

		AssetRequest assetRequest = new AssetRequest();

		setStockCode(assetRequest, record);
		setStockName(assetRequest, record);
		setExchangeName(assetRequest, record);
		setBrokerName(assetRequest, record);
		setActor(assetRequest, record);
		setAssetType(assetRequest, record);
		setMaturityDate(assetRequest, record);
		setPrice(assetRequest, record);
		setQuantity(assetRequest, record);
		setTransactionType(assetRequest, record);
		setTransactionDate(assetRequest, record);
		setBrokerCharges(assetRequest, record);
		setMiscCharges(assetRequest, record);
		setComment(assetRequest, record);

		return assetRequest;
	}

	private static void setStockCode(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.STOCK_CODE);
		assetRequest.setStockCode((String) cellDetail.getCellValue());
	}

	private static void setStockName(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.STOCK_NAME);
		assetRequest.setStockName((String) cellDetail.getCellValue());
	}

	private static void setExchangeName(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.EXCHANGE_NAME);
		assetRequest.setExchangeName((String) cellDetail.getCellValue());
	}

	private static void setBrokerName(AssetRequest assetRequest, Map<String, CellDetail> record) {
		CellDetail cellDetail = record.get(TransactionHeaders.BROKER_NAME);
		assetRequest.setBrokerName((String) cellDetail.getCellValue());
	}

	private static void setActor(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.ACTOR);
		assetRequest.setActor((String) cellDetail.getCellValue());
	}

	private static void setAssetType(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.ASSET_TYPE);
		String assetType = (String) cellDetail.getCellValue();

		switch (assetType) {
			case "EQUITY" :
				assetRequest.setAssetType(AssetType.EQUITY);
				break;
			case "MUTUAL_FUND" :
				assetRequest.setAssetType(AssetType.MUTUAL_FUND);
				break;
			case "BOND" :
				assetRequest.setAssetType(AssetType.BOND);
				break;
			case "FD" :
				assetRequest.setAssetType(AssetType.FD);
				break;
			case "INSURANCE" :
				assetRequest.setAssetType(AssetType.INSURANCE);
				break;
			default :
				break;
		}
	}

	private static void setMaturityDate(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.getOrDefault(TransactionHeaders.MATURITY_DATE, CellDetail.def());
		assetRequest.setMaturityDate((LocalDate) cellDetail.getCellValue());
	}

	private static void setPrice(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.PRICE);
		assetRequest.setPrice((Double) cellDetail.getCellValue());
	}

	private static void setQuantity(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.QUANTITY);
		assetRequest.setQuantity((Long) cellDetail.getCellValue());
	}

	private static void setTransactionDate(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.TRANSACTION_DATE);
		assetRequest.setTransactionDate((LocalDate) cellDetail.getCellValue());
	}

	private static void setTransactionType(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.TRANSACTION_TYPE);
		String transactionType = (String) cellDetail.getCellValue();

		switch (transactionType) {
			case "BUY" :
				assetRequest.setTransactionType(TransactionType.BUY);
				break;
			case "SELL" :
				assetRequest.setTransactionType(TransactionType.SELL);
				break;
			default :
				break;
		}
	}

	private static void setBrokerCharges(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.BROKER_CHARGES);
		assetRequest.setBrokerCharges((Double) cellDetail.getCellValue());
	}

	private static void setMiscCharges(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.MISC_CHARGES);
		assetRequest.setMiscCharges((Double) cellDetail.getCellValue());
	}

	private static void setComment(AssetRequest assetRequest, Map<String, CellDetail> record) {

		CellDetail cellDetail = record.get(TransactionHeaders.COMMENT);
		assetRequest.setComment((String) cellDetail.getCellValue());
	}
}
