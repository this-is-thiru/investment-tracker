package com.thiru.investment_tracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.investment_tracker.entity.Asset;

public interface PortfolioRepository extends MongoRepository<Asset, String> {

	List<Asset> findByEmail(String email);

	Optional<Asset> findByEmailAndStockCodeAndTransactionDate(String email, String stockCode,
			LocalDate transactionDate);

	Optional<Asset> findByEmailAndStockCodeAndAccountHolderAndTransactionDate(String email, String stockCode,
			String accountHolder, LocalDate transactionDate);

	List<Asset> findByEmailAndStockCodeOrderByTransactionDate(String email, String productName);

	List<Asset> findByEmailAndTransactionDateBetween(String email, LocalDate startDate, LocalDate endDate);

	void deleteByEmail(String email);
}
