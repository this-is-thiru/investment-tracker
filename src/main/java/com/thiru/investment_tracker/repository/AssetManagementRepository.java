package com.thiru.investment_tracker.repository;

import com.thiru.investment_tracker.entity.AssetManagementDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface AssetManagementRepository extends MongoRepository<AssetManagementDetails, String> {

    List<AssetManagementDetails> findByLastAmcChargesDeductedOnBefore(LocalDate date);
}
