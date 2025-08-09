package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.entity.LastlyPerformedCorporateAction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LastlyPerformedCorporateActionRepo extends MongoRepository<LastlyPerformedCorporateAction, String> {
    Optional<LastlyPerformedCorporateAction> findByEmailAndStockCodeAndAssetTypeAndActionType(String email, String stockCode, AssetType assetType, CorporateActionType actionType);

    void deleteByEmail(String email);
}
