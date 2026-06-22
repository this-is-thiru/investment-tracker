package com.thiru.wealthlens.taxplanning.salary.repository;

import com.thiru.wealthlens.taxplanning.salary.entity.TaxComputationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaxComputationRepository extends MongoRepository<TaxComputationEntity, String> {

    List<TaxComputationEntity> findBySalaryProfileId(String salaryProfileId);
}