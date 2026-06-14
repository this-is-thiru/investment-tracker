package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.dto.enums.AmcChargeFrequency;
import com.thiru.investment_tracker.dto.enums.BrokerName;
import com.thiru.investment_tracker.entity.AssetManagementDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssetManagementRepository extends MongoRepository<AssetManagementDetails, String> {

    List<AssetManagementDetails> findByLastAmcChargesDeductedOnBefore(LocalDate date);

    List<AssetManagementDetails> findByEmail(String email);

    Optional<AssetManagementDetails> findByEmailAndBrokerName(String email, BrokerName brokerName);

    @Query("{amc_charges_frequency: ?0, last_amc_charges_deducted_on: {$lte: ?1}}")
    List<AssetManagementDetails> findEntriesToUpdateAmcCharges(AmcChargeFrequency frequency, LocalDate date);
}
