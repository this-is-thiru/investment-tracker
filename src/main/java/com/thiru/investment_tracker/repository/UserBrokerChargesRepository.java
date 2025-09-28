package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.entity.UserBrokerCharges;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface UserBrokerChargesRepository extends MongoRepository<UserBrokerCharges, String> {

    @Query("{ 'broker_name': ?0, 'stock_code': ?1, 'transaction_date': ?2, 'type': 'SELL' }")
    Optional<UserBrokerCharges> findFirstSellTxnByBrokerNameAndStockCodeAndTransactionDate(
            BrokerName brokerName, String stockCode, LocalDate transactionDate
    );
}
