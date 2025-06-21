package com.thiru.investment_tracker.service.export.writer;

import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.service.export.writer.model.AbstractExcelWorkbookWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TransactionUploadTemplateWriter extends AbstractExcelWorkbookWriter<TransactionEntity> {

    private static final String EMAIL = "email";
    private static final String STOCK_CODE = "stockCode";
    private static final String STOCK_NAME = "stockName";
    private static final String ASSET_TYPE = "assetType";
    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String BROKER_NAME = "brokerName";
    private static final String TRANSACTION_TYPE = "transactionType";
    private static final String STOCK_QUANTITY = "quantity";
    private static final String STOCK_PRICE = "price";
    private static final String MATURITY_DATE = "maturityDate";
    private static final String BROKER_CHARGES = "brokerCharges";
    private static final String MISC_CHARGES = "miscCharges";
    private static final String TRANSACTION_DATE = "transactionDate";
    private static final String COMMENTS = "comments";


    public TransactionUploadTemplateWriter(String sheetName, List<String> columnFields) {
        super(sheetName, columnFields);
    }

    @Override
    protected List<String> orderedColumns() {
        return List.of(EMAIL, STOCK_CODE, STOCK_NAME, ASSET_TYPE, EXCHANGE_NAME, BROKER_NAME, TRANSACTION_TYPE,
                STOCK_QUANTITY, STOCK_PRICE, TRANSACTION_DATE, MATURITY_DATE, BROKER_CHARGES, MISC_CHARGES, COMMENTS);
    }

    @Override
    protected Map<String, String> simpleColumnHeaders() {
        Map<String, String> simpleColumnHeaders = new HashMap<>();
        simpleColumnHeaders.put(EMAIL, "Email");
        simpleColumnHeaders.put(STOCK_CODE, "Stock Code");
        simpleColumnHeaders.put(STOCK_NAME, "Stock Name");
        simpleColumnHeaders.put(ASSET_TYPE, "Asset Type");
        simpleColumnHeaders.put(EXCHANGE_NAME, "Exchange Name");
        simpleColumnHeaders.put(BROKER_NAME, "Broker Name");
        simpleColumnHeaders.put(TRANSACTION_TYPE, "Transaction Type");
        simpleColumnHeaders.put(STOCK_QUANTITY, "Quantity");
        simpleColumnHeaders.put(STOCK_PRICE, "Price");
        simpleColumnHeaders.put(TRANSACTION_DATE, "Transaction Date");
        simpleColumnHeaders.put(MATURITY_DATE, "Maturity Date");
        simpleColumnHeaders.put(BROKER_CHARGES, "Broker Charges");
        simpleColumnHeaders.put(MISC_CHARGES, "Misc Charges");
        simpleColumnHeaders.put(COMMENTS, "Comments");
        return simpleColumnHeaders;
    }

    @Override
    protected Map<String, Function<TransactionEntity, Object>> simpleColumnValueMap() {

        Map<String, Function<TransactionEntity, Object>> simpleColumnValueMap = new HashMap<>();
        simpleColumnValueMap.put(EMAIL, TransactionEntity::getEmail);
        simpleColumnValueMap.put(STOCK_CODE, TransactionEntity::getStockCode);
        simpleColumnValueMap.put(STOCK_NAME, TransactionEntity::getStockName);
        simpleColumnValueMap.put(ASSET_TYPE, (transaction) -> transaction.getAssetType().name());
        simpleColumnValueMap.put(EXCHANGE_NAME, TransactionEntity::getExchangeName);
        simpleColumnValueMap.put(BROKER_NAME, (transaction) -> transaction.getBrokerName().name());
        simpleColumnValueMap.put(TRANSACTION_TYPE, (transaction) -> transaction.getTransactionType().name());
        simpleColumnValueMap.put(STOCK_QUANTITY, TransactionEntity::getQuantity);
        simpleColumnValueMap.put(STOCK_PRICE, TransactionEntity::getPrice);
        simpleColumnValueMap.put(MATURITY_DATE, TransactionEntity::getMaturityDate);
        simpleColumnValueMap.put(BROKER_CHARGES, TransactionEntity::getBrokerCharges);
        simpleColumnValueMap.put(MISC_CHARGES, TransactionEntity::getMiscCharges);
        simpleColumnValueMap.put(TRANSACTION_DATE, TransactionEntity::getTransactionDate);
        simpleColumnValueMap.put(COMMENTS, (transaction) -> "comments");
        return simpleColumnValueMap;
    }
}
