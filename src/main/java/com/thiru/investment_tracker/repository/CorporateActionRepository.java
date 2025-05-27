package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.entity.CorporateActionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CorporateActionRepository extends MongoRepository<CorporateActionEntity, String> {
    List<CorporateActionEntity> findByType(CorporateActionType type);
}
