package com.thiru.investment_tracker.service.export.writer;

import com.thiru.investment_tracker.dto.enums.ExcelDataType;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.service.export.writer.model.AbstractExcelWorkbookWriter;
import com.thiru.investment_tracker.service.export.writer.model.ExcelDataTypePair;

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
    protected Map<String, Function<TransactionEntity, ExcelDataTypePair>> simpleColumnValueMap() {

        Map<String, Function<TransactionEntity, ExcelDataTypePair>> simpleColumnValueMap = new HashMap<>();
        simpleColumnValueMap.put(EMAIL, (transaction) -> dataTypePair(transaction.getEmail(), ExcelDataType.STRING));
        simpleColumnValueMap.put(STOCK_CODE, (transaction) -> dataTypePair(transaction.getStockCode(), ExcelDataType.STRING));
        simpleColumnValueMap.put(STOCK_NAME, (transaction) -> dataTypePair(transaction.getStockName(), ExcelDataType.STRING));
        simpleColumnValueMap.put(ASSET_TYPE, (transaction) -> dataTypePair(transaction.getAssetType().name(), ExcelDataType.STRING));
        simpleColumnValueMap.put(EXCHANGE_NAME, (transaction) -> dataTypePair(transaction.getExchangeName(), ExcelDataType.STRING));
        simpleColumnValueMap.put(BROKER_NAME, (transaction) -> dataTypePair(transaction.getBrokerName().name(), ExcelDataType.STRING));
        simpleColumnValueMap.put(TRANSACTION_TYPE, (transaction) -> dataTypePair(transaction.getTransactionType().name(), ExcelDataType.STRING));
        simpleColumnValueMap.put(STOCK_QUANTITY, (transaction) -> dataTypePair(transaction.getQuantity(), ExcelDataType.DOUBLE));
        simpleColumnValueMap.put(STOCK_PRICE, (transaction) -> dataTypePair(transaction.getPrice(), ExcelDataType.DOUBLE));
        simpleColumnValueMap.put(MATURITY_DATE, (transaction) -> dataTypePair(transaction.getMaturityDate(), ExcelDataType.LOCAL_DATE));
        simpleColumnValueMap.put(BROKER_CHARGES, (transaction) -> dataTypePair(transaction.getBrokerCharges(), ExcelDataType.DOUBLE));
        simpleColumnValueMap.put(MISC_CHARGES, (transaction) -> dataTypePair(transaction.getMiscCharges(), ExcelDataType.DOUBLE));
        simpleColumnValueMap.put(TRANSACTION_DATE, (transaction) -> dataTypePair(transaction.getTransactionDate(), ExcelDataType.LOCAL_DATE));
        simpleColumnValueMap.put(COMMENTS, (transaction) -> dataTypePair("comments", ExcelDataType.STRING));
        return simpleColumnValueMap;
    }
}
