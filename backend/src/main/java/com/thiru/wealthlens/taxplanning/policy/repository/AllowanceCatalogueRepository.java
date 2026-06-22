package com.thiru.wealthlens.taxplanning.policy.repository;

import com.thiru.wealthlens.shared.dto.enums.EntityStatus;
import com.thiru.wealthlens.taxplanning.policy.entity.AllowanceCatalogueEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AllowanceCatalogueRepository extends MongoRepository<AllowanceCatalogueEntity, String> {

    List<AllowanceCatalogueEntity> findByTaxYearAndStatus(String taxYear, EntityStatus status);

    Optional<AllowanceCatalogueEntity> findByCodeAndTaxYear(String code, String taxYear);

    Optional<AllowanceCatalogueEntity> findByCodeAndStatus(String code, EntityStatus status);
}