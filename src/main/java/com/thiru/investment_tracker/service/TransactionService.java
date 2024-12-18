package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.user.UserMail;

import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.repository.TransactionRepository;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
import com.thiru.investment_tracker.util.db.QueryFilter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Transaction transaction = TObjectMapper.copy(assetRequest, Transaction.class);
        transaction.setTotalValue(transaction.getPrice() * transaction.getQuantity());

        log.info("Transaction: {}, Stock: '{}' on '{}' noted successfully", transaction.getTransactionType(),
                transaction.getStockName(), transaction.getTransactionDate());
        Transaction savedTransaction = transactionRepository.save(transaction);
        return savedTransaction.getId();
    }

    public List<Transaction> transactionsForCorporateActions(UserMail userMail, double quantity, String stockCode, LocalDate recordDate) {

        String email = userMail.getEmail();
        List<Transaction> transactions = transactionRepository.findByEmailAndStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(email, stockCode, recordDate);

        List<Transaction> transactionsToConsider = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (quantity <= 0) {
                break;
            }

            transactionsToConsider.add(transaction);
            quantity -= transaction.getQuantity();
        }

        if (quantity > 0) {
            throw new IllegalArgumentException("Invalid transactions");
        }
        return transactionsToConsider;
    }

    public void saveCorporateActionProcessedTransactions(List<Transaction> transactions) {
        transactionRepository.saveAll(transactions);
    }

    public List<Transaction> getUserTransactions(UserMail userMail, List<QueryFilter> queryFilters) {
        return mongoTemplateService.getDocuments(userMail, queryFilters, Transaction.class);
    }


    public List<Transaction> testMethod(UserMail userMail, String stockCode, LocalDate recordDate) {

        String email = userMail.getEmail();
        return transactionRepository.findByEmailAndStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(email, stockCode, recordDate);
    }

    public void updateTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();

        transactions.forEach(transaction -> {
                    AssetType assetType = transaction.getAssetType();
                    transaction.setAssetType(assetType == null ? AssetType.MUTUAL_FUND : assetType);
                }
        );
        transactionRepository.saveAll(transactions);
    }

    public void deleteTransactions(UserMail userMail) {
        transactionRepository.deleteByEmail(userMail.getEmail());
    }

    public List<Transaction> allTransactions() {
        return transactionRepository.findAll();
    }
}
