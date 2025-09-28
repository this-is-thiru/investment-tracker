package com.thiru.investment_tracker.core.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.investment_tracker.core.entity.ProfitAndLossEntity;

public interface ProfitAndLossRepository extends MongoRepository<ProfitAndLossEntity, String> {
    Optional<ProfitAndLossEntity> findByEmailAndFinancialYear(String email, String financialYear);

    void deleteByEmail(String email);
}
