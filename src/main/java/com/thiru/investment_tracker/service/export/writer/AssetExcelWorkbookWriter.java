package com.thiru.investment_tracker.service.export.writer;

import com.thiru.investment_tracker.dto.AssetResponse;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.service.export.writer.model.AbstractExcelWorkbookWriter;
import com.thiru.investment_tracker.util.collection.TOptional;
import com.thiru.investment_tracker.util.transaction.ExcelHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AssetExcelWorkbookWriter extends AbstractExcelWorkbookWriter<AssetResponse> {

    private static final String EMAIL = "email";
    private static final String STOCK_CODE = "stockCode";
    private static final String STOCK_NAME = "stockName";
    private static final String ASSET_TYPE = "assetType";
    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String BROKER_NAME = "brokerName";
    private static final String STOCK_QUANTITY = "quantity";
    private static final String STOCK_TOTAL_QUANTITY = "totalQuantity";
    private static final String STOCK_PRICE = "price";
    private static final String STOCK_TOTAL_VALUE = "totalValue";
    private static final String MATURITY_DATE = "maturityDate";
    private static final String BROKER_CHARGES = "brokerCharges";
    private static final String MISC_CHARGES = "miscCharges";

    public AssetExcelWorkbookWriter(String sheetName, List<String> columnFields) {
        super(sheetName, columnFields);
    }

    @Override
    protected List<String> orderedColumns() {
        return List.of(EMAIL, STOCK_CODE, STOCK_NAME, ASSET_TYPE, EXCHANGE_NAME, BROKER_NAME, STOCK_QUANTITY, STOCK_TOTAL_QUANTITY,
                STOCK_PRICE, STOCK_TOTAL_VALUE, MATURITY_DATE, BROKER_CHARGES, MISC_CHARGES);
    }

    @Override
    protected Map<String, String> simpleColumnHeaders() {

        Map<String, String> simpleColumnHeaders = new HashMap<>();
        simpleColumnHeaders.put(EMAIL, ExcelHeaders.EMAIL);
        simpleColumnHeaders.put(STOCK_CODE, ExcelHeaders.STOCK_CODE);
        simpleColumnHeaders.put(STOCK_NAME, ExcelHeaders.STOCK_NAME);
        simpleColumnHeaders.put(ASSET_TYPE, ExcelHeaders.ASSET_TYPE);
        simpleColumnHeaders.put(EXCHANGE_NAME, ExcelHeaders.EXCHANGE_NAME);
        simpleColumnHeaders.put(BROKER_NAME, ExcelHeaders.BROKER_NAME);
        simpleColumnHeaders.put(STOCK_QUANTITY, ExcelHeaders.QUANTITY);
        simpleColumnHeaders.put(STOCK_TOTAL_QUANTITY, ExcelHeaders.TOTAL_VALUE);
        simpleColumnHeaders.put(STOCK_PRICE, ExcelHeaders.PRICE);
        simpleColumnHeaders.put(STOCK_TOTAL_VALUE, ExcelHeaders.TOTAL_VALUE);
        simpleColumnHeaders.put(MATURITY_DATE, ExcelHeaders.MATURITY_DATE);
        simpleColumnHeaders.put(BROKER_CHARGES, ExcelHeaders.BROKER_CHARGES);
        simpleColumnHeaders.put(MISC_CHARGES, ExcelHeaders.MISC_CHARGES);
        return simpleColumnHeaders;
    }

    @Override
    protected Map<String, Function<AssetResponse, Object>> simpleColumnValueMap() {

        Map<String, Function<AssetResponse, Object>> simpleColumnValueMap = new HashMap<>();
        simpleColumnValueMap.put(EMAIL, AssetResponse::getEmail);
        simpleColumnValueMap.put(STOCK_CODE, AssetResponse::getStockCode);
        simpleColumnValueMap.put(STOCK_NAME, AssetResponse::getStockName);
        simpleColumnValueMap.put(ASSET_TYPE, assetResponse -> TOptional.map2(assetResponse, AssetResponse::getAssetType, AssetType::name));
        simpleColumnValueMap.put(EXCHANGE_NAME, AssetResponse::getExchangeName);
        simpleColumnValueMap.put(BROKER_NAME, assetResponse -> TOptional.map2(assetResponse, AssetResponse::getBrokerName, BrokerName::name));
        simpleColumnValueMap.put(STOCK_QUANTITY, AssetResponse::getQuantity);
        simpleColumnValueMap.put(STOCK_TOTAL_QUANTITY, AssetResponse::getTotalQuantity);
        simpleColumnValueMap.put(STOCK_PRICE, AssetResponse::getPrice);
        simpleColumnValueMap.put(STOCK_TOTAL_VALUE, AssetResponse::getTotalValue);
        simpleColumnValueMap.put(MATURITY_DATE, AssetResponse::getMaturityDate);
        simpleColumnValueMap.put(BROKER_CHARGES, AssetResponse::getBrokerCharges);
        simpleColumnValueMap.put(MISC_CHARGES, AssetResponse::getMiscCharges);
        return simpleColumnValueMap;
    }
}
