package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.TransactionStatus;
import com.thiru.investment_tracker.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends MongoRepository<TransactionEntity, String> {

    List<TransactionEntity> findByEmail(String email);

    List<TransactionEntity> findByEmailAndStockCodeAndTransactionDateBefore(String email, String stockCode, LocalDate transactionDate);

    List<TransactionEntity> findByStockCodeAndTransactionDateBeforeOrderByTransactionDateDesc(String stockCode, LocalDate transactionDate);

    List<TransactionEntity> findByEmailAndStockCodeAndBrokerNameAndTransactionDateBeforeOrderByTransactionDateDesc(String email, String stockCode, BrokerName brokerName, LocalDate transactionDate);

    void deleteByEmail(String email);

    Optional<TransactionEntity> findByEmailAndSourceTempTransactionId(String email, String sourceTempTransactionId);

    List<TransactionEntity> findByEmailAndStatus(String email, TransactionStatus status);

    List<TransactionEntity> findByEmailAndStatusAndStockCodeAndAssetTypeAndTransactionDateBefore(
            String email, TransactionStatus status, String stockCode, AssetType assetType, LocalDate date);

    List<TransactionEntity> findByEmailAndStatusAndStockCodeAndAssetTypeAndTransactionDateAfterOrderByTransactionDateAsc(
            String email, TransactionStatus status, String stockCode, AssetType assetType, LocalDate date);

    void deleteByEmailAndStatus(String email, TransactionStatus status);
}
