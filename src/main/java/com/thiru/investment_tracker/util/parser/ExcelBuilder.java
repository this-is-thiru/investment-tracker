package com.thiru.investment_tracker.util.parser;

import com.thiru.investment_tracker.dto.AssetResponse;
import com.thiru.investment_tracker.dto.TransactionResponse;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.transaction.ExcelHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelBuilder {

    private static final int DEFAULT_DECIMAL_PLACES = 2;

    public static ByteArrayInputStream downloadAssets(List<AssetResponse> userStocks, boolean isTermSpecific) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ExcelParser.initialiseExcelSheet(workbook, ExcelHeaders.getPortfolioHeaders(), ExcelParser.ASSETS);
            Sheet sheet0 = workbook.getSheetAt(0);
            Sheet sheet1 = null;
            if (isTermSpecific) {
                ExcelParser.initialiseExcelSheet(workbook, ExcelHeaders.getTransactionQuantityHeaders(), ExcelParser.TRANSACTIONS);
                sheet1 = workbook.getSheetAt(1);
            }

            int rowCount = 1;
            int transactionRowCount = 1;
            for (AssetResponse assetResponse : userStocks) {

                Row row = sheet0.createRow(rowCount);
                row.createCell(0).setCellValue(assetResponse.getEmail());
                row.createCell(1).setCellValue(assetResponse.getStockName());
                row.createCell(2).setCellValue(assetResponse.getStockCode());
                row.createCell(3).setCellValue(getRoundedValue(assetResponse.getQuantity()));
                row.createCell(4).setCellValue(getRoundedValue(assetResponse.getTotalQuantity()));
                row.createCell(5).setCellValue(getRoundedValue(assetResponse.getPrice()));
                row.createCell(6).setCellValue(getRoundedValue(assetResponse.getTotalValue()));
                row.createCell(7).setCellValue(assetResponse.getExchangeName());
                row.createCell(8).setCellValue(assetResponse.getBrokerName().name());
                row.createCell(9).setCellValue(assetResponse.getAssetType().name());
                setDateField(row.createCell(10), assetResponse.getMaturityDate());
                row.createCell(11).setCellValue(getRoundedValue(assetResponse.getBrokerCharges()));
                row.createCell(12).setCellValue(getRoundedValue(assetResponse.getMiscCharges()));


                if (isTermSpecific) {
                    transactionRowCount = updateTransactionSheet(sheet1, transactionRowCount, assetResponse);
                }
                rowCount++;
            }

            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int updateTransactionSheet(Sheet transactionsSheet, int rowCount, AssetResponse assetResponse) {

        Map<String, Double> transactionQuantities = assetResponse.getTransactionQuantities();
        for (Map.Entry<String, Double> entry : transactionQuantities.entrySet()) {

            Row row = transactionsSheet.createRow(rowCount);
            row.createCell(0).setCellValue(assetResponse.getStockCode());
            row.createCell(1).setCellValue(assetResponse.getBrokerName().name());
            row.createCell(2).setCellValue(entry.getKey());
            row.createCell(3).setCellValue(getRoundedValue(entry.getValue()));
            rowCount++;
        }
        return rowCount;
    }


    public ByteArrayInputStream downloadTransactions(List<TransactionResponse> userTransactions) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ExcelParser.initialiseExcelSheet(workbook, ExcelHeaders.getTransactionHeaders(), ExcelParser.TRANSACTIONS);
            Sheet sheet = workbook.getSheetAt(0);

            int rowCount = 1;
            for (TransactionResponse transaction : userTransactions) {

                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(transaction.getEmail());
                row.createCell(1).setCellValue(transaction.getStockCode());
                row.createCell(2).setCellValue(transaction.getStockName());
                row.createCell(3).setCellValue(transaction.getExchangeName());
                row.createCell(4).setCellValue(transaction.getBrokerName().name());
                row.createCell(5).setCellValue(transaction.getAssetType().name());
                setDateField(row.createCell(6), transaction.getMaturityDate());
                row.createCell(7).setCellValue(getRoundedValue(transaction.getPrice()));
                row.createCell(8).setCellValue(getRoundedValue(transaction.getQuantity()));
                row.createCell(9).setCellValue(transaction.getTransactionType().name());
                row.createCell(10).setCellValue(transaction.getActor());
                setDateField(row.createCell(11), transaction.getTransactionDate());
                row.createCell(12).setCellValue(getRoundedValue(transaction.getBrokerCharges()));
                row.createCell(13).setCellValue(getRoundedValue(transaction.getMiscCharges()));
                row.createCell(14).setCellValue(transaction.getComment());
                rowCount++;
            }

            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteArrayInputStream downloadTemplate() {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ExcelParser.initialiseExcelSheet(workbook, ExcelHeaders.getTransactionHeaders(), ExcelParser.TRANSACTIONS);
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
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat(TCollectionUtil.DATE_FORMAT));
        cell.setCellStyle(dateStyle);
        cell.setCellValue(date);
    }

    private static double getRoundedValue(double doubleValue) {

        double scale = Math.pow(10, DEFAULT_DECIMAL_PLACES);
        return Math.round(doubleValue * scale) / scale;
    }
}
