package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.CorporateActionWrapper;
import com.thiru.investment_tracker.dto.enums.CorporateAction;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Service
@Transactional
public class CorporateActionService {
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;

    @Transactional
    public String updateCorporateAction(UserMail userMail, CorporateActionWrapper actionWrapper) {

        CorporateAction action = actionWrapper.getAction();
        if (Objects.requireNonNull(action) == CorporateAction.STOCK_SPLIT) {
            processStockSplit(userMail, actionWrapper);
        } else {
            throw new IllegalArgumentException("Invalid action type" + action);
        }

        return "Corporate action: " + action + " noted successfully for stock: " + actionWrapper.getStockCode();
    }

    private void processStockSplit(UserMail userMail, CorporateActionWrapper actionWrapper) {

        String stockCode = actionWrapper.getStockCode();
        LocalDate recordDate = actionWrapper.getRecordDate();
        String[] splitRatio = actionWrapper.getSplitRatio().split(":");
        double multiplier = Integer.parseInt(splitRatio[0]);
        double ratio = Double.parseDouble(splitRatio[1]);

        double quantityMultiplier = multiplier / ratio;
        double priceMultiplier = 1 / quantityMultiplier;

        List<Asset> stockEntities = portfolioService.stocksForCorporateActions(userMail, stockCode, recordDate);

        double quantity = 0;
        for (Asset asset : stockEntities) {
            double previousQuantity = asset.getQuantity();

            asset.setQuantity(previousQuantity * quantityMultiplier);
            asset.setPrice(asset.getPrice() * priceMultiplier);

            CorporateActionWrapper corporateActionWrapper = TObjectMapper.copy(actionWrapper, CorporateActionWrapper.class);
            asset.getCorporateActions().add(corporateActionWrapper);
            quantity += previousQuantity;
        }
        portfolioService.saveCorporateActionProcessedStocks(stockEntities);

        List<Transaction> transactions = transactionService.transactionsForCorporateActions(userMail, quantity, stockCode, recordDate);

        for (Transaction transaction : transactions) {
            double previousQuantity = transaction.getQuantity();

            transaction.setQuantity(previousQuantity * quantityMultiplier);
            transaction.setPrice(transaction.getPrice() * priceMultiplier);

            CorporateActionWrapper corporateActionWrapper = TObjectMapper.copy(actionWrapper, CorporateActionWrapper.class);
            transaction.getCorporateActions().add(corporateActionWrapper);
        }
        transactionService.saveCorporateActionProcessedTransactions(transactions);
    }
}
