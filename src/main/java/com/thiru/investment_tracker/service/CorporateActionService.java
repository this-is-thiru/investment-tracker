package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.CorporateActionWrapper;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.entity.CorporateAction;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.repository.CorporateActionRepository;
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
    private final CorporateActionRepository corporateActionRepository;
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;

    public String addCorporateAction(CorporateActionWrapper actionWrapper) {

        CorporateAction corporateAction = TObjectMapper.copy(actionWrapper, CorporateAction.class);
        corporateActionRepository.save(corporateAction);
        return "Corporate action: " + actionWrapper.getType() + " added successfully for stock: " + actionWrapper.getStockCode();
    }

    private List<CorporateAction> getNameOrSymbolChangeCorporateActions() {
        return corporateActionRepository.findByType(CorporateActionType.NAME_OR_SYMBOL_CHANGE);
    }

    @Transactional
    public String updateCorporateAction(UserMail userMail, CorporateActionWrapper actionWrapper) {

        CorporateActionType action = actionWrapper.getType();
        if (Objects.requireNonNull(action) == CorporateActionType.STOCK_SPLIT) {
            processStockSplit(userMail, actionWrapper);
        } else {
            throw new IllegalArgumentException("Invalid action type" + action);
        }

        return "Corporate action: " + action + " noted successfully for stock: " + actionWrapper.getStockCode();
    }

    @Transactional
    public String updateCorporateAction1(UserMail userMail, CorporateActionWrapper actionWrapper) {

        CorporateActionType action = actionWrapper.getType();
        switch (action) {
            case STOCK_SPLIT -> processStockSplit(userMail, actionWrapper);
            case NAME_OR_SYMBOL_CHANGE ->processNameOrSymbolChange(actionWrapper);
            default -> throw new IllegalArgumentException("Invalid action type" + action);
        }

        return "Corporate action: " + action + " noted successfully for stock: " + actionWrapper.getStockCode();
    }

    // TODO: Remove - Doesn't work, need to fix
    @Deprecated
    private void processStockSplit(UserMail userMail, CorporateActionWrapper actionWrapper) {

        String stockCode = actionWrapper.getStockCode();
        LocalDate recordDate = actionWrapper.getRecordDate();
        String[] splitRatio = actionWrapper.getRatio().split(":");
        double multiplier = Integer.parseInt(splitRatio[0]);
        double ratio = Double.parseDouble(splitRatio[1]);

        double quantityMultiplier = multiplier / ratio;
        double priceMultiplier = 1 / quantityMultiplier;

        List<Asset> stockEntities = portfolioService.stocksForCorporateActions(stockCode, recordDate);

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

        List<Transaction> transactions = transactionService.transactionsForCorporateActions(quantity, stockCode, recordDate);

        for (Transaction transaction : transactions) {
            double previousQuantity = transaction.getQuantity();

            transaction.setQuantity(previousQuantity * quantityMultiplier);
            transaction.setPrice(transaction.getPrice() * priceMultiplier);

            CorporateActionWrapper corporateActionWrapper = TObjectMapper.copy(actionWrapper, CorporateActionWrapper.class);
            transaction.getCorporateActions().add(corporateActionWrapper);
        }
        transactionService.saveCorporateActionProcessedTransactions(transactions);
    }

    private void processNameOrSymbolChange(CorporateActionWrapper actionWrapper) {

        String stockCode = actionWrapper.getStockCode();
        LocalDate recordDate = actionWrapper.getRecordDate();

        List<Asset> stockEntities = portfolioService.stocksForCorporateActions(stockCode, recordDate);

        for (Asset asset : stockEntities) {

            asset.setStockCode(actionWrapper.getToStockCode());
            asset.setStockName(actionWrapper.getToStockName());
            CorporateActionWrapper corporateActionWrapper = TObjectMapper.copy(actionWrapper, CorporateActionWrapper.class);
            asset.getCorporateActions().add(corporateActionWrapper);
        }
        portfolioService.saveCorporateActionProcessedStocks(stockEntities);

        List<Transaction> transactions = transactionService.transactionsForCorporateActions(stockCode, recordDate);

        for (Transaction transaction : transactions) {

            transaction.setStockCode(actionWrapper.getToStockCode());
            transaction.setStockName(actionWrapper.getToStockName());
            CorporateActionWrapper corporateActionWrapper = TObjectMapper.copy(actionWrapper, CorporateActionWrapper.class);
            transaction.getCorporateActions().add(corporateActionWrapper);
        }
        transactionService.saveCorporateActionProcessedTransactions(transactions);
    }
}
