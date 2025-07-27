package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends MongoRepository<TransactionEntity, String> {

    List<TransactionEntity> findByEmail(String email);

    List<TransactionEntity> findByEmailAndStockCodeAndTransactionDateBefore(String email, String stockCode, LocalDate transactionDate);

    List<TransactionEntity> findByStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(String stockCode, LocalDate transactionDate);

    List<TransactionEntity> findByEmailAndStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(String email, String stockCode, LocalDate transactionDate);

    void deleteByEmail(String email);
}
