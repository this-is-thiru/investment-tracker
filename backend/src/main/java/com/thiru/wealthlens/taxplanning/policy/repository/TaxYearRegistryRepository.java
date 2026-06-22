package com.thiru.wealthlens.taxplanning.policy.repository;

import com.thiru.wealthlens.taxplanning.policy.entity.TaxYearRegistryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TaxYearRegistryRepository extends MongoRepository<TaxYearRegistryEntity, String> {

    Optional<TaxYearRegistryEntity> findByTaxYear(String taxYear);
}