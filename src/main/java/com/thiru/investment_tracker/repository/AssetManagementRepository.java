package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.entity.AssetManagementDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssetManagementRepository extends MongoRepository<AssetManagementDetails, String> {

    List<AssetManagementDetails> findByLastAmcChargesDeductedOnBefore(LocalDate date);

    Optional<AssetManagementDetails> findByEmailAndBrokerName(String email, BrokerName brokerName);
}
