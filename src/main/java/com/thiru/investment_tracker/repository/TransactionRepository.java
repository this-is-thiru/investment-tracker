package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.entity.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByEmail(String email);
    List<Transaction> findByEmailAndStockCodeAndTransactionDateBefore(String email, String stockCode, LocalDate transactionDate);
    List<Transaction> findByEmailAndStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(String email, String stockCode, LocalDate transactionDate);

    void deleteByEmail(String email);
}
