package com.thiru.investment_tracker.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.investment_tracker.entity.Transaction;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
}
