package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.entity.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
}
