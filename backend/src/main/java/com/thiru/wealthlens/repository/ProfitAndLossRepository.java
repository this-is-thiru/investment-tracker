package com.thiru.wealthlens.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.thiru.wealthlens.entity.ProfitAndLossEntity;

public interface ProfitAndLossRepository extends MongoRepository<ProfitAndLossEntity, String> {
    Optional<ProfitAndLossEntity> findByEmailAndFinancialYear(String email, String financialYear);

    List<ProfitAndLossEntity> findAllByEmail(String email);

    void deleteByEmail(String email);
}
