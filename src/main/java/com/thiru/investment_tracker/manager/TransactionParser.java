package com.thiru.investment_tracker.manager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.thiru.investment_tracker.common.CommonUtil;
import com.thiru.investment_tracker.common.enums.AssetType;
import com.thiru.investment_tracker.common.enums.TransactionType;
import com.thiru.investment_tracker.common.parser.ParserUtil;
import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.InputRecord;
import com.thiru.investment_tracker.dto.InputRecords;
import com.thiru.investment_tracker.util.TransactionHeaders;

public class TransactionParser {

    public static List<AssetRequest> getTransactionRecords(InputRecords records) {

        return null;
    }

    private static AssetRequest getAssetRequest(InputRecord inputRecord) {

        Map<String, String> record = inputRecord.getRecord();

        AssetRequest assetRequest = new AssetRequest();

        assetRequest.setStockCode(record.get(TransactionHeaders.STOCK_CODE));
        assetRequest.setStockName(record.get(TransactionHeaders.STOCK_NAME));
        assetRequest.setExchangeName(record.get(TransactionHeaders.EXCHANGE_NAME));
        assetRequest.setBrokerName(record.get(TransactionHeaders.BROKER_NAME));
        assetRequest.setActorName(record.get(TransactionHeaders.ACTOR_NAME));

        setAssetType(assetRequest, record);
        setMaturityDate(assetRequest, record);
        setPrice(assetRequest, record);
        setQuantity(assetRequest, record);
        setTransactionType(assetRequest, record);
        setTransactionDate(assetRequest, record);

        return null;
    }

    private static void setAssetType(AssetRequest assetRequest, Map<String, String> record) {
        String assetType = record.get(TransactionHeaders.ASSET_TYPE);

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
            case "FD":
                assetRequest.setAssetType(AssetType.FD);
                break;
            case "INSURANCE":
                assetRequest.setAssetType(AssetType.INSURANCE);
                break;
            default:
                break;
        }
    }

    private static void setMaturityDate(AssetRequest assetRequest, Map<String, String> record) {
        String maturityDateStr = record.get(TransactionHeaders.MATURITY_DATE);
        LocalDate maturityDate = CommonUtil.copy(maturityDateStr, LocalDate.class);
        assetRequest.setMaturityDate(maturityDate);
    }

    private static void setPrice(AssetRequest assetRequest, Map<String, String> record) {
        Double price = Double.parseDouble(record.get(TransactionHeaders.PRICE));
        assetRequest.setPrice(price);
    }

    private static void setQuantity(AssetRequest assetRequest, Map<String, String> record) {

        Long quantity = Long.parseLong(record.get(TransactionHeaders.QUANTITY));
        assetRequest.setQuantity(quantity);
    }

    private static void setTransactionDate(AssetRequest assetRequest, Map<String, String> record) {
        String transactionDateStr = record.get(TransactionHeaders.TRANSACTION_DATE);
        assetRequest.setTransactionDate(ParserUtil.convertToDate(transactionDateStr));
    }

    private static void setTransactionType(AssetRequest assetRequest, Map<String, String> record) {
        String transactionType = record.get(TransactionHeaders.TRANSACTION_TYPE);

        switch (transactionType) {
            case "BUY":
                assetRequest.setTransactionType(TransactionType.BUY);
                break;
            case "SELL":
                assetRequest.setTransactionType(TransactionType.SELL);
                break;
            default:
                break;
        }
    }
}
