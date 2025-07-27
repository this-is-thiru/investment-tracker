package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.entity.TemporaryTransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TemporaryTransactionRepository extends MongoRepository<TemporaryTransactionEntity, String> {

    List<TemporaryTransactionEntity> findByEmail(String email);
}
