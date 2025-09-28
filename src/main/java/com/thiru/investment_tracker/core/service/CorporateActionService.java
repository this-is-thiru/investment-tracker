package com.thiru.investment_tracker.core.service;

import com.thiru.investment_tracker.core.dto.CorporateActionDto;
import com.thiru.investment_tracker.core.dto.enums.AssetType;
import com.thiru.investment_tracker.core.dto.enums.BrokerName;
import com.thiru.investment_tracker.core.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.core.dto.enums.TransactionType;
import com.thiru.investment_tracker.core.entity.*;
import com.thiru.investment_tracker.core.repository.CorporateActionRepository;
import com.thiru.investment_tracker.core.repository.LastlyPerformedCorporateActionRepo;
import com.thiru.investment_tracker.core.repository.TemporaryTransactionRepository;
import com.thiru.investment_tracker.core.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.core.util.collection.TObjectMapper;
import com.thiru.investment_tracker.core.util.time.TLocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CorporateActionService {

    private static final int ONE = 1;

    private final PortfolioService portfolioService;
    private final TransactionService transactionService;
    private final TemporaryTransactionRepository temporaryTransactionRepository;
    private final CorporateActionRepository corporateActionRepository;
    private final LastlyPerformedCorporateActionRepo lastlyPerformedCorporateActionRepo;


    public String addCorporateAction(CorporateActionDto actionWrapper) {

        CorporateActionEntity corporateActionEntity = actionWrapper.getAsEntity();
        List<CorporateActionEntity> actions = corporateActionRepository.findByStockCodeAndRecordDateAndOrderByPriorityAsc(actionWrapper.getStockCode(), actionWrapper.getRecordDate());
        CorporateActionEntity actionWithSamePriority = TCollectionUtil.findFirst(actions, action -> action.getPriority() == corporateActionEntity.getPriority());
        CorporateActionEntity actionForSameStock = TCollectionUtil.findFirst(actions, action -> action.getStockCode().equals(corporateActionEntity.getStockCode()));

        if (actionForSameStock != null) {
            throw new IllegalArgumentException("Corporate actions is already present id: " + actionWithSamePriority.getId());
        }

        if (actionWithSamePriority != null) {
            List<String> actionIds = TCollectionUtil.map(actions, CorporateActionEntity::getId);
            throw new IllegalArgumentException("Update the priorities for the corporate actions with Ids: " + actionIds);
        }

        corporateActionRepository.save(corporateActionEntity);
        return "Corporate action: " + actionWrapper.getType() + " added successfully for stock: " + actionWrapper.getStockCode();
    }

    public CorporateActionEntity getCorporateActionDetails(String id) {
        Optional<CorporateActionEntity> corporateAction = corporateActionRepository.findById(id);
        if (corporateAction.isEmpty()) {
            throw new IllegalArgumentException("No corporate action found with id: " + id);
        }
        return corporateAction.get();
    }

    public String updateCorporateActionPriority(String id, int priority) {

        Optional<CorporateActionEntity> existingAction = corporateActionRepository.findById(id);
        if (existingAction.isEmpty()) {
            throw new IllegalArgumentException("No corporate actions found with id: " + id);
        }
        CorporateActionEntity entity = existingAction.get();
        int existingPriority = entity.getPriority();
        if (existingPriority == priority) {
            throw new IllegalArgumentException("Existing priority and new priority is same.");
        }
        entity.setPriority(priority);
        corporateActionRepository.save(entity);
        return "Priority has been updated for the action: " + id + " stock: " + entity.getStockCode() + " from: " + existingPriority + " to: " + priority;
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
        log.info("Quarterly corporate action: {} noted successfully for stock: {}", action, corporateAction.getStockCode());
    }

    @Transactional
    public void deleteCorporateActions(String id) {
        corporateActionRepository.deleteById(id);
    }

    public List<CorporateActionEntity> getAllCorporateActions() {
        return corporateActionRepository.findAll();
    }

    @Transactional
    public void performPendingCorporateActions(String email) {

        LocalDate today = TLocalDate.today();
        Month quarterStart = today.getMonth().firstMonthOfQuarter();
        LocalDate fromDate = LocalDate.of(today.getYear(), quarterStart, ONE);
        LocalDate toDate = TLocalDate.today();
        List<CorporateActionEntity> corporateActions = corporateActionRepository.findByTypeInAndRecordDateBetween(CorporateActionType.FILTERABLE_CORPORATE_ACTIONS, fromDate, toDate);

        for (CorporateActionEntity corporateAction : corporateActions) {
            if (skipPendingCorporateAction(email, corporateAction)) {
                continue;
            }
            performPendingCorporateAction(email, corporateAction);
        }

        System.out.println(corporateActions);
    }

    private boolean skipPendingCorporateAction(String email, CorporateActionEntity corporateAction) {

        String stockCode = corporateAction.getStockCode();
        AssetType assetType = corporateAction.getAssetType();
        LocalDate recordDate = corporateAction.getRecordDate();

        List<TemporaryTransactionEntity> beforeTemporaryTransactions = temporaryTransactionRepository
                .findByEmailAndStockCodeAndAssetTypeAndTransactionDateBefore(email, stockCode, assetType, recordDate);

        return !beforeTemporaryTransactions.isEmpty();
    }

    public void performPendingCorporateAction(String email, CorporateActionEntity corporateAction) {

        String stockCode = corporateAction.getStockCode();
        CorporateActionType corporateActionType = corporateAction.getType();
        AssetType assetType = corporateAction.getAssetType();
        LocalDate recordDate = corporateAction.getRecordDate();

        Optional<LastlyPerformedCorporateAction> lastlyPerformedCA = lastlyPerformedCorporateActionRepo
                .findByEmailAndStockCodeAndAssetTypeAndActionType(email, stockCode, assetType, corporateActionType);

        if (lastlyPerformedCA.isPresent()) {
            LastlyPerformedCorporateAction lastlyPerformedCorporateAction = lastlyPerformedCA.get();
            if (lastlyPerformedCorporateAction.getActionDate().isAfter(corporateAction.getRecordDate().minusDays(ONE))) {
                log.warn("Corporate action: {} for stock: {} is already performed with record date: {}", corporateActionType, stockCode, recordDate);
                return;
            }
        }

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
    public String performCorporateAction(CorporateActionDto actionWrapper) {

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

        List<AssetEntity> stockEntities = portfolioService.stocksForCorporateActions(email, stockCode, recordDate);
        if (stockEntities.isEmpty()) {
            log.info("No stock found for corporate action: {} for stock: {} on record date: {}", corporateAction.getType(), stockCode, recordDate);
            updateLastlyPerformedCorporateAction(email, stockCode, corporateAction.getAssetType(), corporateAction.getType(), corporateAction.getExDate());
            return null;
        }

        Map<BrokerName, List<AssetEntity>> brokerNameAndStocksMap = TCollectionUtil.groupingBy(stockEntities, AssetEntity::getBrokerName);
        for (Map.Entry<BrokerName, List<AssetEntity>> entry : brokerNameAndStocksMap.entrySet()) {
            processBonusShares(email, corporateAction, entry.getKey(), entry.getValue());
            updateLastlyPerformedCorporateAction(email, stockCode, corporateAction.getAssetType(), corporateAction.getType(), corporateAction.getExDate());
        }

        return "Success";
    }

    public void processBonusShares(String email, CorporateActionEntity corporateAction, BrokerName brokerName, List<AssetEntity> stockEntities) {

        String stockCode = corporateAction.getStockCode();

        int totalShares = TCollectionUtil.map(stockEntities, assetEntity -> assetEntity.getQuantity().intValue()).stream().reduce(0, Integer::sum);
        String[] splitRatio = corporateAction.getRatio().split(":");
        int numerator = Integer.parseInt(splitRatio[0]);
        int denominator = Integer.parseInt(splitRatio[1]);
        int bonusSharesCount = totalShares * numerator / denominator;

        List<String> newTransactionIds = processBonusSharesTxns(email, bonusSharesCount, corporateAction, brokerName, stockEntities);
        if (newTransactionIds.size() != 1) {
            log.warn("Bonus shares txns: {} added for symbol: {} in Broker: {} with multiple transactions", newTransactionIds, stockCode, brokerName);
            throw new IllegalArgumentException("Multiple transactions added for bonus shares: " + newTransactionIds);
        }

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

        log.info("Bonus shares: {} added for symbol: {} in Broker: {}", newTransactionIds.getFirst(), stockCode, brokerName);
    }

    public List<String> processBonusSharesTxns(String email, int bonusSharesCount, CorporateActionEntity corporateAction, BrokerName brokerName, List<AssetEntity> stockEntities) {

        String stockCode = corporateAction.getStockCode();
        LocalDate recordDate = corporateAction.getRecordDate();

        // Transaction update
        List<TransactionEntity> transactionEntities = transactionService.testTransactionsForCorporateActions(email, stockCode, brokerName, recordDate);
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

        return newTransactionIds;
    }

    private void updateLastlyPerformedCorporateAction(String email, String stockCode, AssetType assetType, CorporateActionType actionType, LocalDate exDate) {

        Optional<LastlyPerformedCorporateAction> lpcaOptional = lastlyPerformedCorporateActionRepo
                .findByEmailAndStockCodeAndAssetTypeAndActionType(email, stockCode, assetType, actionType);
        LastlyPerformedCorporateAction lastlyPerformedCorporateAction = lpcaOptional.orElse(LastlyPerformedCorporateAction.builder()
                .email(email).stockCode(stockCode).actionType(actionType).actionDate(exDate).build());

        lastlyPerformedCorporateAction.setActionDate(exDate);
        lastlyPerformedCorporateActionRepo.save(lastlyPerformedCorporateAction);
    }
}
