package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.CorporateActionWrapper;
import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.Asset;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.repository.TransactionRepository;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
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

    public void processStockSplit(UserMail userMail, double quantity, CorporateActionWrapper actionWrapper) {

        String email = userMail.getEmail();
        String stockCode = actionWrapper.getStockCode();
        LocalDate recordDate = actionWrapper.getRecordDate();
        String[] splitRatio = actionWrapper.getSplitRatio().split(":");
        double multiplier = Integer.parseInt(splitRatio[0]);
        double ratio = Double.parseDouble(splitRatio[1]);

        double quantityMultiplier = multiplier / ratio;
        double priceMultiplier = 1 / quantityMultiplier;

        List<Transaction> transactions = transactionRepository.findByEmailAndStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(email, stockCode, recordDate);

        for (Transaction transaction : transactions) {
            if (quantity <= 0) {
                break;
            }

            double previousQuantity = transaction.getQuantity();
            transaction.setQuantity(previousQuantity * quantityMultiplier);
            transaction.setPrice(transaction.getPrice() * priceMultiplier);
            transaction.getCorporateActions().add(actionWrapper);
            quantity -= previousQuantity;
        }

        if (quantity > 0) {
            throw new IllegalArgumentException("Invalid transactions");
        }
        transactionRepository.saveAll(transactions);
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
