package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.auth.config.SecurityAuditorAware;
import com.thiru.investment_tracker.dto.CorporateActionDto;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.entity.AssetEntity;
import com.thiru.investment_tracker.entity.CorporateActionEntity;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.repository.CorporateActionRepository;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import com.thiru.investment_tracker.util.time.TLocalDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class CorporateActionService {
    private final CorporateActionRepository corporateActionRepository;
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;
    private final SecurityAuditorAware securityAuditorAware;

    public String addCorporateAction(CorporateActionDto actionWrapper) {

        CorporateActionEntity corporateActionEntity = TObjectMapper.copy(actionWrapper, CorporateActionEntity.class);
        corporateActionRepository.save(corporateActionEntity);
        return "Corporate action: " + actionWrapper.getType() + " added successfully for stock: " + actionWrapper.getStockCode();
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

            CorporateActionDto corporateActionDto = TObjectMapper.copy(actionWrapper, CorporateActionDto.class);
            assetEntity.getCorporateActions().add(corporateActionDto);
            quantity += previousQuantity;
        }
        portfolioService.saveCorporateActionProcessedStocks(stockEntities);

        List<TransactionEntity> transactionEntities = transactionService.transactionsForCorporateActions(quantity, stockCode, recordDate);

        for (TransactionEntity transactionEntity : transactionEntities) {
            double previousQuantity = transactionEntity.getQuantity();

            transactionEntity.setQuantity(previousQuantity * quantityMultiplier);
            transactionEntity.setPrice(transactionEntity.getPrice() * priceMultiplier);

            CorporateActionDto corporateActionDto = TObjectMapper.copy(actionWrapper, CorporateActionDto.class);
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
            CorporateActionDto corporateActionDto = TObjectMapper.copy(actionWrapper, CorporateActionDto.class);
            corporateActionDto.setCreatedBy(getAuditor());
            corporateActionDto.setCreatedAt(TLocalDateTime.now());
            assetEntity.getCorporateActions().add(corporateActionDto);
        }
        portfolioService.saveCorporateActionProcessedStocks(stockEntities);

        List<TransactionEntity> transactionEntities = transactionService.transactionsForCorporateActions(stockCode, recordDate);

        for (TransactionEntity transactionEntity : transactionEntities) {

            transactionEntity.setStockCode(actionWrapper.getToStockCode());
            transactionEntity.setStockName(actionWrapper.getToStockName());
            CorporateActionDto corporateActionDto = TObjectMapper.copy(actionWrapper, CorporateActionDto.class);
            corporateActionDto.setCreatedBy(getAuditor());
            corporateActionDto.setCreatedAt(TLocalDateTime.now());
            transactionEntity.getCorporateActions().add(corporateActionDto);
        }
        transactionService.saveCorporateActionProcessedTransactions(transactionEntities);
        log.info("Processed name or symbol change for stock: {} to: {}", stockCode, actionWrapper.getToStockCode());
    }

    private String getAuditor() {
        return securityAuditorAware.getAuditor();
    }
}
