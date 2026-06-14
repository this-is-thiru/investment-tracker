package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.AssetType;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.dto.enums.CorporateActionType;
import com.thiru.investment_tracker.entity.LastlyPerformedCorporateAction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface LastlyPerformedCorporateActionRepo extends MongoRepository<LastlyPerformedCorporateAction, String> {
    @Query("{'email': ?0, 'stock_code': ?1 , 'asset_type':  ?2, 'action_type': ?3, 'broker_name': ?4 }")
    Optional<LastlyPerformedCorporateAction> findLastPerformedCA(String email, String stockCode, AssetType assetType, CorporateActionType actionType, BrokerName brokerName);

    void deleteByEmail(String email);
}
