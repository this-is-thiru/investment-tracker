package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.TransactionResponse;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.TransactionEntity;
import com.thiru.investment_tracker.repository.TransactionRepository;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import com.thiru.investment_tracker.util.db.QueryFilter;
import com.thiru.investment_tracker.util.parser.ExcelBuilder;
import com.thiru.investment_tracker.util.parser.ExcelParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final MongoTemplateService mongoTemplateService;

    public String addTransaction(AssetRequest assetRequest) {

        if (assetRequest.getTransactionDate() == null) {
            assetRequest.setTransactionDate(LocalDate.now());
        }
        TransactionEntity transactionEntity = assetRequest.getTransaction();

        log.info("Transaction: {}, Stock: '{}' on '{}' noted successfully", transactionEntity.getTransactionType(),
                transactionEntity.getStockName(), transactionEntity.getTransactionDate());
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

    public void saveCorporateActionProcessedTransactions(List<TransactionEntity> transactionEntities) {
        transactionRepository.saveAll(transactionEntities);
    }

    public List<TransactionEntity> getUserTransactions(UserMail userMail, List<QueryFilter> queryFilters) {
        return mongoTemplateService.getDocuments(userMail, queryFilters, TransactionEntity.class);
    }

    public Pair<InputStreamResource, String> downloadAllTransactions(UserMail userMail) {
        List<TransactionResponse> userTransactions = getAllTransactions(userMail);

        String fileName = ExcelParser.TRANSACTION_FILE_NAME;
        ByteArrayInputStream inputStream = ExcelBuilder.downloadTransactions(userTransactions);
        InputStreamResource resource = new InputStreamResource(inputStream);

        return Pair.of(resource, fileName);
    }

    private List<TransactionResponse> getAllTransactions(UserMail userMail) {
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
