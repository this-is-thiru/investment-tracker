package com.thiru.investment_tracker.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.investment_tracker.entity.Asset;

public interface PortfolioRepository extends MongoRepository<Asset, String> {

	List<Asset> findByEmail(String email);

	Optional<Asset> findByEmailAndStockCodeAndTransactionDate(String email, String stockCode, Date transactionDate);

	List<Asset> findByEmailAndStockCodeOrderByTransactionDate(String email, String productName);

	List<Asset> findByEmailAndTransactionDateBetween(String email, Date startDate, Date endDate);

}
