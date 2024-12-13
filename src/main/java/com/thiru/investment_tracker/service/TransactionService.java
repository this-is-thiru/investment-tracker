package com.thiru.investment_tracker.service;

import com.thiru.investment_tracker.dto.AssetRequest;
import com.thiru.investment_tracker.dto.user.UserMail;
import com.thiru.investment_tracker.entity.Transaction;
import com.thiru.investment_tracker.repository.TransactionRepository;
import com.thiru.investment_tracker.util.collection.TObjectMapper;
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
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public void addTransaction(AssetRequest assetRequest) {

        if (assetRequest.getTransactionDate() == null) {
            assetRequest.setTransactionDate(LocalDate.now());
        }
        Transaction transaction = TObjectMapper.copy(assetRequest, Transaction.class);
        transaction.setTotalValue(transaction.getPrice() * transaction.getQuantity());

        log.info("Transaction: {}, Stock: '{}' on '{}' noted successfully", transaction.getTransactionType(),
                transaction.getStockName(), transaction.getTransactionDate());
        transactionRepository.save(transaction);
    }

    public void deleteTransactions(UserMail userMail) {
        transactionRepository.deleteByEmail(userMail.getEmail());
    }

    public List<Transaction> allTransactions() {
        return transactionRepository.findAll();
    }
}
