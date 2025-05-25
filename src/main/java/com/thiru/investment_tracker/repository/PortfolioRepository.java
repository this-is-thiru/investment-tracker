package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.entity.Asset;
import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends MongoRepository<Asset, String> {

    List<Asset> findByEmail(String email);

    Optional<Asset> findByEmailAndStockCodeAndTransactionDate(String email, String stockCode,
                                                              LocalDate transactionDate);

    Optional<Asset> findByEmailAndStockCodeAndAccountHolderAndTransactionDate(String email, String stockCode,
                                                                              String accountHolder, LocalDate transactionDate);

    Optional<Asset> findByEmailAndStockCodeAndBrokerNameAndAccountHolderAndTransactionDate(String email,
                                                                                           String stockCode, BrokerName brokerName, String accountHolder, LocalDate transactionDate);

    List<Asset> findByEmailAndStockCodeAndBrokerNameAndAccountHolderOrderByTransactionDate(String email,
                                                                                           String stockCode, BrokerName brokerName, String accountHolder);

    List<Asset> findByStockCodeAndTransactionDateBefore(String stockCode,LocalDate transactionDate);

    List<Asset> findByEmailAndStockCodeOrderByTransactionDate(String email, String stockCode);

    List<Asset> findByEmailAndStockCodeIn(String email, Collection<String> stockCodes);

    List<Asset> findByEmailAndAssetType(String email, AssetType assetType);

    List<Asset> findByEmailAndTransactionDateBetween(String email, LocalDate startDate, LocalDate endDate);

    void deleteByEmail(String email);
}
