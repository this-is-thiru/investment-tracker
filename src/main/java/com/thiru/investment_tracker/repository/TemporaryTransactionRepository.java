package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.entity.TemporaryTransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface TemporaryTransactionRepository extends MongoRepository<TemporaryTransactionEntity, String> {

    List<TemporaryTransactionEntity> findByEmail(String email);

    List<TemporaryTransactionEntity> findByEmailAndStockCodeAndAssetTypeAndTransactionDateAfterOrderByTransactionDateAsc(String email, String stockCode, AssetType assetType, LocalDate transactionDate);

    List<TemporaryTransactionEntity> findByEmailAndStockCodeAndAssetTypeAndTransactionDateBefore(String email, String stockCode, AssetType assetType, LocalDate transactionDate);


    void deleteByEmail(String email);
}
