package com.thiru.investment_tracker.util.transaction;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.InputRecord;
import com.thiru.investment_tracker.dto.InputRecords;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.parser.CellDetail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransactionParser {

    public static List<AssetRequest> getTransactionRecords(InputRecords records) {
        return TCollectionUtil.map(sanitizeRecords(records), TransactionParser::toAssetRequest);
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
        setPrice(assetRequest, record);
        setQuantity(assetRequest, record);
        setTransactionType(assetRequest, record);
        setTransactionDate(assetRequest, record);
        setMaturityDate(assetRequest, record);
        setBrokerCharges(assetRequest, record);
        setMiscCharges(assetRequest, record);
        setBrokerOrderId(assetRequest, record);
        setBrokerOrderTime(assetRequest, record);
        setTimezoneId(assetRequest, record);
        setComment(assetRequest, record);

        return assetRequest;
    }

    private static void setStockCode(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.STOCK_CODE);
        assetRequest.setStockCode((String) cellDetail.getCellValue());
    }

    private static void setStockName(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.STOCK_NAME);
        assetRequest.setStockName((String) cellDetail.getCellValue());
    }

    private static void setExchangeName(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.EXCHANGE_NAME);
        assetRequest.setExchangeName((String) cellDetail.getCellValue());
    }

    private static void setBrokerName(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.BROKER_NAME);
        String brokerName = (String) cellDetail.getCellValue();

        switch (brokerName) {
            case "UPSTOX":
                assetRequest.setBrokerName(BrokerName.UPSTOX);
                break;
            case "FYERS":
                assetRequest.setBrokerName(BrokerName.FYERS);
                break;
            case "ZERODHA":
                assetRequest.setBrokerName(BrokerName.ZERODHA);
                break;
            default:
                throw new IllegalArgumentException("Invalid broker name" + brokerName);
        }
    }

    private static void setActor(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.ACTOR);
        assetRequest.setActor((String) cellDetail.getCellValue());
    }

    private static void setAssetType(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.ASSET_TYPE);
        String assetType = (String) cellDetail.getCellValue();

        switch (assetType) {
            case "EQUITY":
                assetRequest.setAssetType(AssetType.EQUITY);
                break;
            case "MUTUAL_FUND":
                assetRequest.setAssetType(AssetType.MUTUAL_FUND);
                break;
            case "BOND":
                assetRequest.setAssetType(AssetType.BOND);
                break;
            case "GOLD_BOND":
                assetRequest.setAssetType(AssetType.GOLD_BOND);
                break;
            case "FD":
                assetRequest.setAssetType(AssetType.FD);
                break;
            case "INSURANCE":
                assetRequest.setAssetType(AssetType.INSURANCE);
                break;
            default:
                throw new IllegalArgumentException("Invalid asset type" + assetType);
        }
    }

    private static void setMaturityDate(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.getOrDefault(ExcelHeaders.MATURITY_DATE, CellDetail.def());
        LocalDate maturityDate = getDefaultMaturity(assetRequest.getAssetType(), assetRequest.getTransactionDate(), (LocalDate) cellDetail.getCellValue());
        assetRequest.setMaturityDate(maturityDate);
    }

    private static void setPrice(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.PRICE);
        assetRequest.setPrice((Double) cellDetail.getCellValue());
    }

    private static void setQuantity(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.QUANTITY);
        assetRequest.setQuantity((Double) cellDetail.getCellValue());
    }

    private static void setTransactionDate(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.TRANSACTION_DATE);
        assetRequest.setTransactionDate((LocalDate) cellDetail.getCellValue());
    }

    private static void setTransactionType(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.TRANSACTION_TYPE);
        String transactionType = (String) cellDetail.getCellValue();

        switch (transactionType) {
            case "BUY":
                assetRequest.setTransactionType(TransactionType.BUY);
                break;
            case "SELL":
                assetRequest.setTransactionType(TransactionType.SELL);
                break;
            default:
                throw new IllegalArgumentException("Invalid transaction type" + transactionType);
        }
    }

    private static void setBrokerCharges(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.BROKER_CHARGES);
        assetRequest.setBrokerCharges((Double) cellDetail.getCellValue());
    }

    private static void setMiscCharges(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.MISC_CHARGES);
        assetRequest.setMiscCharges((Double) cellDetail.getCellValue());
    }

    private static void setBrokerOrderId(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.getOrDefault(ExcelHeaders.ORDER_ID, CellDetail.def());
        assetRequest.setOrderId((String) cellDetail.getCellValue());
    }

    private static void setBrokerOrderTime(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.getOrDefault(ExcelHeaders.ORDER_EXECUTION_TIME, CellDetail.def());
        assetRequest.setOrderDateTime((LocalDateTime) cellDetail.getCellValue());
    }

    private static void setTimezoneId(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.getOrDefault(ExcelHeaders.TIME_ZONE, CellDetail.def());
        assetRequest.setTimezoneId((String) cellDetail.getCellValue());
    }

    private static void setComment(AssetRequest assetRequest, Map<String, CellDetail> record) {

        CellDetail cellDetail = record.get(ExcelHeaders.COMMENT);
        assetRequest.setComment((String) cellDetail.getCellValue());
    }

    private static LocalDate getDefaultMaturity(AssetType assetType, LocalDate transactionDate, LocalDate maturityDate) {

        return switch (assetType) {
            case EQUITY, BOND, MUTUAL_FUND, FD, INSURANCE -> null;
            case GOLD_BOND -> maturityDate != null ? maturityDate : transactionDate.plusYears(8);
        };
    }
}
