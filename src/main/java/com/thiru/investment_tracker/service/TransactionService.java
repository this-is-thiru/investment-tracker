package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.TransactionResponse;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.entity.query.QueryFilter;
import com.thiru.investment_tracker.repository.TransactionRepository;
import com.thiru.investment_tracker.util.collection.TCollectionUtil;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final MongoTemplateService mongoTemplateService;

    public String addTransaction(UserMail userMail, AssetRequest assetRequest) {

        if (assetRequest.getTransactionDate() == null) {
            assetRequest.setTransactionDate(LocalDate.now());
        }
        TransactionEntity transactionEntity = assetRequest.getTransaction();
        transactionEntity.setEmail(userMail.getEmail());

        log.info("Transaction: {}, Stock: '{}' on '{}' noted successfully for: {}", transactionEntity.getTransactionType(),
                transactionEntity.getStockName(), transactionEntity.getTransactionDate(), userMail.getEmail());
        TransactionEntity savedTransactionEntity = transactionRepository.save(transactionEntity);
        return savedTransactionEntity.getId();
    }

    public List<TransactionEntity> transactionsForCorporateActions(double quantity, String stockCode, LocalDate recordDate) {

        List<TransactionEntity> transactionEntities = transactionRepository.findByStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(stockCode, recordDate);

        List<TransactionEntity> transactionsToConsider = new ArrayList<>();
        for (TransactionEntity transactionEntity : transactionEntities) {
            if (quantity <= 0) {
                break;
            }

            transactionsToConsider.add(transactionEntity);
            quantity -= transactionEntity.getQuantity();
        }

        if (quantity > 0) {
            throw new IllegalArgumentException("Invalid transactions");
        }
        return transactionsToConsider;
    }

    public List<TransactionEntity> transactionsForCorporateActions(String stockCode, LocalDate recordDate) {
        return transactionRepository.findByStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(stockCode, recordDate);
    }

    public List<TransactionEntity> testTransactionsForCorporateActions(String email, String stockCode, LocalDate recordDate) {
        return transactionRepository.findByEmailAndStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(email, stockCode, recordDate);
    }

    public List<String> saveCorporateActionProcessedTransactions(List<TransactionEntity> transactionEntities) {
        List<TransactionEntity> savedTransactions = transactionRepository.saveAll(transactionEntities);
        return TCollectionUtil.map(savedTransactions, TransactionEntity::getId);
    }

    public List<TransactionEntity> getUserTransactions(UserMail userMail, List<QueryFilter> queryFilters) {
        return mongoTemplateService.getDocuments(userMail, queryFilters, TransactionEntity.class);
    }

    public List<TransactionEntity> getUserTransactions(UserMail userMail) {
        return transactionRepository.findByEmail(userMail.getEmail());
    }

    public List<TransactionResponse> getAllUserTransactions(UserMail userMail) {
        List<TransactionEntity> transactionEntities = transactionRepository.findByEmail(userMail.getEmail());
        return transactionEntities.stream().map(transaction -> TObjectMapper.copy(transaction, TransactionResponse.class)).toList();
    }


//    public List<Transaction> testMethod(UserMail userMail, String stockCode, LocalDate recordDate) {
//
//        String email = userMail.getEmail();
//        return transactionRepository.findByEmailAndStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(email, stockCode, recordDate);
//    }

    public void updateTransactions() {
        List<TransactionEntity> transactionEntities = transactionRepository.findAll();

        transactionEntities.forEach(transaction -> {
                    AssetType assetType = transaction.getAssetType();
                    transaction.setAssetType(assetType == null ? AssetType.MUTUAL_FUND : assetType);
                }
        );
        transactionRepository.saveAll(transactionEntities);
    }

    public void deleteTransactions(UserMail userMail) {
        transactionRepository.deleteByEmail(userMail.getEmail());
    }

    public List<TransactionEntity> allTransactions() {
        return transactionRepository.findAll();
    }
}
