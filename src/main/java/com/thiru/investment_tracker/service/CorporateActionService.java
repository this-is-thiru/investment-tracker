package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.CorporateActionDto;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.dto.enums.TransactionType;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.entity.CorporateActionEntity;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.repository.CorporateActionRepository;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import com.thiru.investment_tracker.util.time.TLocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CorporateActionService {

    private static final int ONE = 1;

    private final CorporateActionRepository corporateActionRepository;
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;

    public String addCorporateAction(CorporateActionDto actionWrapper) {

        CorporateActionEntity corporateActionEntity = actionWrapper.getAsEntity();
        List<CorporateActionEntity> actions = corporateActionRepository.findByStockCodeAndRecordDateAndOrderByPriorityAsc(actionWrapper.getStockCode(), actionWrapper.getRecordDate());
        CorporateActionEntity actionWithSamePriority = TCollectionUtil.findFirst(actions, action -> action.getPriority() == corporateActionEntity.getPriority());
        if (!actions.isEmpty() && actionWithSamePriority != null) {
            List<String> actionIds = TCollectionUtil.map(actions, CorporateActionEntity::getId);
            throw new IllegalArgumentException("Update the priorities for the corporate actions with Ids: " + actionIds);
        }
        corporateActionRepository.save(corporateActionEntity);
        return "Corporate action: " + actionWrapper.getType() + " added successfully for stock: " + actionWrapper.getStockCode();
    }

    public List<CorporateActionDto> getCorporateActions(List<String> ids) {
        List<CorporateActionEntity> actions = corporateActionRepository.findAllById(ids);
        return TCollectionUtil.map(actions, corporateAction -> TObjectMapper.copy(corporateAction, CorporateActionDto.class));
    }

    public void performQuarterlyCorporateActions(String email, int year, int quarter) {

        Month quarterStart = TLocalDate.firstMonthOfQuarter(quarter);
        LocalDate fromDate = LocalDate.of(year, quarterStart, ONE);
        LocalDate toDate = LocalDate.of(year, quarterStart.plus(3), ONE).minusDays(ONE);
        List<CorporateActionEntity> corporateActions = corporateActionRepository.findByTypeInAndRecordDateBetween(CorporateActionType.FILTERABLE_CORPORATE_ACTIONS, fromDate, toDate);
        for (CorporateActionEntity corporateAction : corporateActions) {
            performQuarterlyCorporateAction(email, corporateAction);
        }
        System.out.println(corporateActions);
    }

    public void performQuarterlyCorporateAction(String email, CorporateActionEntity corporateAction) {

        CorporateActionType action = corporateAction.getType();
        if (Objects.requireNonNull(action) == CorporateActionType.BONUS) {
            processBonusShares(email, corporateAction);
        } else {
            throw new IllegalArgumentException("Invalid action type" + action);
        }
        log.info("Corporate action: {} noted successfully for stock: {}", action, corporateAction.getStockCode());
    }

    private List<CorporateActionEntity> getNameOrSymbolChangeCorporateActions() {
        return corporateActionRepository.findByType(CorporateActionType.NAME_OR_SYMBOL_CHANGE);
    }

    @Transactional
    public String updateCorporateAction(CorporateActionDto actionWrapper) {

        CorporateActionType action = actionWrapper.getType();
        switch (action) {
            case STOCK_SPLIT -> processStockSplit(actionWrapper);
            case NAME_OR_SYMBOL_CHANGE -> processNameOrSymbolChange(actionWrapper);
            default -> throw new IllegalArgumentException("Invalid action type" + action);
        }

        return "Corporate action: " + action + " noted successfully for stock: " + actionWrapper.getStockCode();
    }

    // TODO: Remove - Doesn't work, need to fix
    @Deprecated
    private void processStockSplit(CorporateActionDto actionWrapper) {

        String stockCode = actionWrapper.getStockCode();
        LocalDate recordDate = actionWrapper.getRecordDate();
        String[] splitRatio = actionWrapper.getRatio().split(":");
        double multiplier = Integer.parseInt(splitRatio[0]);
        double ratio = Double.parseDouble(splitRatio[1]);

        double quantityMultiplier = multiplier / ratio;
        double priceMultiplier = 1 / quantityMultiplier;

        List<AssetEntity> stockEntities = portfolioService.stocksForCorporateActions(stockCode, recordDate);

        double quantity = 0;
        for (AssetEntity assetEntity : stockEntities) {
            double previousQuantity = assetEntity.getQuantity();

            assetEntity.setQuantity(previousQuantity * quantityMultiplier);
            assetEntity.setPrice(assetEntity.getPrice() * priceMultiplier);

            CorporateActionEntity corporateActionDto = actionWrapper.getAsEntity();
            assetEntity.getCorporateActions().add(corporateActionDto);
            quantity += previousQuantity;
        }
        portfolioService.saveCorporateActionProcessedStocks(stockEntities);

        List<TransactionEntity> transactionEntities = transactionService.transactionsForCorporateActions(quantity, stockCode, recordDate);

        for (TransactionEntity transactionEntity : transactionEntities) {
            double previousQuantity = transactionEntity.getQuantity();

            transactionEntity.setQuantity(previousQuantity * quantityMultiplier);
            transactionEntity.setPrice(transactionEntity.getPrice() * priceMultiplier);

            CorporateActionEntity corporateActionDto = actionWrapper.getAsEntity();
            transactionEntity.getCorporateActions().add(corporateActionDto);
        }
        transactionService.saveCorporateActionProcessedTransactions(transactionEntities);
    }

    private void processNameOrSymbolChange(CorporateActionDto actionWrapper) {

        String stockCode = actionWrapper.getStockCode();
        LocalDate recordDate = actionWrapper.getRecordDate();

        List<AssetEntity> stockEntities = portfolioService.stocksForCorporateActions(stockCode, recordDate);

        for (AssetEntity assetEntity : stockEntities) {

            assetEntity.setStockCode(actionWrapper.getToStockCode());
            assetEntity.setStockName(actionWrapper.getToStockName());
            CorporateActionEntity corporateActionDto = actionWrapper.getAsEntity();
            assetEntity.getCorporateActions().add(corporateActionDto);
        }
        portfolioService.saveCorporateActionProcessedStocks(stockEntities);

        List<TransactionEntity> transactionEntities = transactionService.transactionsForCorporateActions(stockCode, recordDate);

        for (TransactionEntity transactionEntity : transactionEntities) {

            transactionEntity.setStockCode(actionWrapper.getToStockCode());
            transactionEntity.setStockName(actionWrapper.getToStockName());
            CorporateActionEntity corporateActionDto = actionWrapper.getAsEntity();
            transactionEntity.getCorporateActions().add(corporateActionDto);
        }
        transactionService.saveCorporateActionProcessedTransactions(transactionEntities);
        log.info("Processed name or symbol change for stock: {} to: {}", stockCode, actionWrapper.getToStockCode());
    }

    public String processBonusShares(String email, CorporateActionDto actionWrapper) {
        return processBonusShares(email, actionWrapper.getAsEntity());
    }

    public String processBonusShares(String email, CorporateActionEntity corporateAction) {

        String stockCode = corporateAction.getStockCode();
        LocalDate recordDate = corporateAction.getRecordDate();

        List<AssetEntity> stockEntities = portfolioService.testStocksForCorporateActions(email, stockCode, recordDate);
        int totalShares = TCollectionUtil.map(stockEntities, assetEntity -> assetEntity.getQuantity().intValue()).stream().reduce(0, Integer::sum);
        String[] splitRatio = corporateAction.getRatio().split(":");
        int numerator = Integer.parseInt(splitRatio[0]);
        int denominator = Integer.parseInt(splitRatio[1]);
        int bonusSharesCount = totalShares * numerator / denominator;

        // Transaction update
        List<TransactionEntity> transactionEntities = transactionService.testTransactionsForCorporateActions(email, stockCode, recordDate);
        List<String> existingTransactionIds = TCollectionUtil.map(transactionEntities, TransactionEntity::getId);

        TransactionEntity first = TCollectionUtil.filter(transactionEntities, transaction -> TransactionType.BUY.equals(transaction.getTransactionType())).getFirst();
        TransactionEntity bonusSharesTransaction = TObjectMapper.copy(first, TransactionEntity.class);
        bonusSharesTransaction.setId(null);
        bonusSharesTransaction.setPrice(0);
        bonusSharesTransaction.setQuantity((double) bonusSharesCount);
        bonusSharesTransaction.setTotalValue(0);
        bonusSharesTransaction.setTransactionDate(corporateAction.getExDate());
        bonusSharesTransaction.setCorporateActionType(CorporateActionType.BONUS);
        transactionEntities.add(bonusSharesTransaction);

        for (TransactionEntity transactionEntity : transactionEntities) {
            CorporateActionEntity action = TObjectMapper.copy(corporateAction, CorporateActionEntity.class);
            transactionEntity.getCorporateActions().add(action);
        }
        List<String> newTransactionIds = transactionService.saveCorporateActionProcessedTransactions(transactionEntities);
        newTransactionIds.removeAll(existingTransactionIds);

        // Portfolio stock
        AssetEntity firstAsset = stockEntities.getFirst();
        AssetEntity bonusSharesEntity = TObjectMapper.copy(firstAsset, AssetEntity.class);
        bonusSharesEntity.setId(null);
        bonusSharesEntity.setQuantity((double) bonusSharesCount);
        bonusSharesEntity.setPrice(0);
        bonusSharesEntity.setTotalValue(0);
        bonusSharesEntity.setTransactionDate(corporateAction.getExDate());
        bonusSharesEntity.setOrderTimeQuantities(List.of());
        bonusSharesEntity.setCorporateActionType(CorporateActionType.BONUS);
        bonusSharesEntity.setBuyTransactionIds(newTransactionIds);
        stockEntities.add(bonusSharesEntity);

        for (AssetEntity assetEntity : stockEntities) {

            CorporateActionEntity action = TObjectMapper.copy(corporateAction, CorporateActionEntity.class);
            assetEntity.getCorporateActions().add(action);
        }
        portfolioService.saveCorporateActionProcessedStocks(stockEntities);

        log.info("Bonus shares: {} added for symbol: {}", bonusSharesTransaction, stockCode);
        return "Success";
    }
}
