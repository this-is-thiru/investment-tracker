package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends MongoRepository<Asset, String> {

//    Page<Asset> findAll(org.springframework.data.mongodb.core.query.Query query, Pageable pageable);

//    List<Asset> findAll(org.springframework.data.mongodb.core.query.Query query);

    List<Asset> findByEmail(String email);

    Optional<Asset> findByEmailAndStockCodeAndTransactionDate(String email, String stockCode, Date transactionDate);

    List<Asset> findByEmailAndStockCodeOrderByTransactionDate(String email, String productName);

    List<Asset> findByEmailAndTransactionDateBetween(String email, Date startDate, Date endDate);

    List<Asset> searchProductByStockNameContainingIgnoreCase(String productName);

    //    @Query("{'product_name': ?0,'product_seller_details.seller_email': ?1}")
    @Query("{$and: [{'product_name': ?0}, {'product_seller_details.seller_email': ?1}] }")
    Optional<Asset> findByProductNameAndSellerEmail(String productName, String sellerEmail);

    @Query("{'sku_code': {$in: ?0}}")
    List<Asset> findBySkuCodes(List<String> skuCodes);

    Optional<Asset> findByStockName(String skuCode);
}
