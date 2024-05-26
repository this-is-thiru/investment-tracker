package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.entity.ProfitAndLossEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProfitAndLossRepository extends MongoRepository<ProfitAndLossEntity, String> {
    Optional<ProfitAndLossEntity> findByFinancialYear(String financialYear);
    Optional<ProfitAndLossEntity> findByEmailAndFinancialYear(String email, String financialYear);
}
